package ao.saas.faturacao.modules.hr.leaves.service;
import ao.saas.faturacao.common.enums.LeaveStatus;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.hr.employees.entity.HrEmployee;
import ao.saas.faturacao.modules.hr.employees.repository.HrEmployeeRepository;
import ao.saas.faturacao.modules.hr.leaves.entity.HrLeave;
import ao.saas.faturacao.modules.hr.leaves.repository.HrLeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class HrLeaveService {
    private final HrLeaveRepository leaveRepo;
    private final HrEmployeeRepository empRepo;
    private final CompanyRepository companyRepo;

    public Page<HrLeave> list(UUID companyId, int page, int limit) {
        return leaveRepo.findByCompanyIdOrderByCreatedAtDesc(companyId, PageRequest.of(page-1, limit));
    }

    @Transactional
    public HrLeave create(UUID companyId, HrLeave dto) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        dto.setCompany(company);
        // Calcular número de dias
        long days = java.time.temporal.ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        dto.setDays(BigDecimal.valueOf(days));
        return leaveRepo.save(dto);
    }

    @Transactional
    public HrLeave approve(UUID companyId, UUID leaveId, UUID approverId) {
        HrLeave leave = leaveRepo.findById(leaveId)
                .orElseThrow(() -> BusinessException.notFound("Pedido não encontrado"));
        if (leave.getStatus() != LeaveStatus.PENDING) throw BusinessException.badRequest("Apenas pedidos pendentes podem ser aprovados");
        HrEmployee approver = empRepo.findByIdAndCompanyIdAndDeletedAtIsNull(approverId, companyId).orElse(null);
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approver);
        leave.setApprovedAt(LocalDateTime.now());
        return leaveRepo.save(leave);
    }

    @Transactional
    public HrLeave reject(UUID companyId, UUID leaveId, String reason) {
        HrLeave leave = leaveRepo.findById(leaveId)
                .orElseThrow(() -> BusinessException.notFound("Pedido não encontrado"));
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRejectedReason(reason);
        return leaveRepo.save(leave);
    }
}
