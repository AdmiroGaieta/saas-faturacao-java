package ao.saas.faturacao.modules.expenses.expenses.service;
import ao.saas.faturacao.common.enums.AuditAction;
import ao.saas.faturacao.common.enums.ExpenseStatus;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.audit.service.AuditService;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.expenses.expenses.entity.Expense;
import ao.saas.faturacao.modules.expenses.expenses.repository.ExpenseRepository;
import ao.saas.faturacao.modules.expenses.accounts_payable.entity.AccountPayable;
import ao.saas.faturacao.modules.expenses.accounts_payable.repository.AccountPayableRepository;
import ao.saas.faturacao.modules.users.entity.User;
import ao.saas.faturacao.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expRepo;
    private final AccountPayableRepository apRepo;
    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final AuditService auditService;

    public Page<Expense> list(UUID companyId, String status, int page, int limit) {
        Specification<Expense> spec = (r,q,cb) -> cb.equal(r.get("company").get("id"), companyId);
        if (status != null) { try { ExpenseStatus s = ExpenseStatus.valueOf(status);
            spec = spec.and((r,q,cb) -> cb.equal(r.get("status"), s)); } catch (Exception ignored){} }
        return expRepo.findAll(spec, PageRequest.of(page-1,limit, Sort.by(Sort.Direction.DESC,"expenseDate")));
    }

    public Expense getOne(UUID companyId, UUID id) {
        return expRepo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> BusinessException.notFound("Despesa não encontrada"));
    }

    @Transactional
    public Expense create(UUID companyId, UUID userId, Expense dto) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        User submitter = userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilizador não encontrado"));
        dto.setCompany(company);
        dto.setSubmittedBy(submitter);
        dto.setTotal(dto.getAmount().add(dto.getTaxAmount() != null ? dto.getTaxAmount() : BigDecimal.ZERO));
        Expense saved = expRepo.save(dto);
        auditService.log(companyId, userId, AuditAction.CREATE, "Expense", saved.getId());
        return saved;
    }

    @Transactional
    public Expense approve(UUID companyId, UUID expId, UUID userId) {
        Expense exp = getOne(companyId, expId);
        if (exp.getStatus() != ExpenseStatus.SUBMITTED) throw BusinessException.badRequest("Apenas despesas submetidas podem ser aprovadas");
        User approver = userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilizador não encontrado"));
        exp.setStatus(ExpenseStatus.APPROVED);
        exp.setApprovedBy(approver);
        // Criar conta a pagar automaticamente
        AccountPayable ap = AccountPayable.builder()
                .company(exp.getCompany()).expense(exp).supplier(exp.getSupplier())
                .description(exp.getTitle()).amount(exp.getTotal())
                .amountDue(exp.getTotal()).amountPaid(BigDecimal.ZERO)
                .issueDate(exp.getExpenseDate()).dueDate(exp.getDueDate() != null ? exp.getDueDate() : exp.getExpenseDate().plusDays(30))
                .currency(exp.getCurrency())
                .build();
        apRepo.save(ap);
        auditService.log(companyId, userId, AuditAction.UPDATE, "Expense", expId);
        return expRepo.save(exp);
    }

    @Transactional
    public Expense reject(UUID companyId, UUID expId, UUID userId, String reason) {
        Expense exp = getOne(companyId, expId);
        if (exp.getStatus() != ExpenseStatus.SUBMITTED) throw BusinessException.badRequest("Apenas despesas submetidas podem ser rejeitadas");
        exp.setStatus(ExpenseStatus.REJECTED);
        exp.setRejectedReason(reason);
        auditService.log(companyId, userId, AuditAction.UPDATE, "Expense", expId);
        return expRepo.save(exp);
    }

    public Map<String,Object> getSummary(UUID companyId) {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("monthApproved", expRepo.sumByStatus(companyId, ExpenseStatus.APPROVED, monthStart, today));
        s.put("monthPaid",     expRepo.sumByStatus(companyId, ExpenseStatus.PAID,     monthStart, today));
        s.put("pendingCount",  expRepo.count());
        return s;
    }
}
