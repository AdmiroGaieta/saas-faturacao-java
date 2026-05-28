package ao.saas.faturacao.modules.companies.service;

import ao.saas.faturacao.common.enums.AuditAction;
import ao.saas.faturacao.common.enums.CompanyStatus;
import ao.saas.faturacao.common.enums.InvoiceStatus;
import ao.saas.faturacao.common.enums.SubscriptionPlan;
import ao.saas.faturacao.common.enums.SubscriptionStatus;
import ao.saas.faturacao.common.enums.UserRole;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.audit.service.AuditService;
import ao.saas.faturacao.modules.companies.entity.*;
import ao.saas.faturacao.modules.companies.repository.*;
import ao.saas.faturacao.modules.customers.repository.CustomerRepository;
import ao.saas.faturacao.modules.invoices.repository.InvoiceRepository;
import ao.saas.faturacao.modules.products.repository.ProductRepository;
import ao.saas.faturacao.modules.subscriptions.entity.Subscription;
import ao.saas.faturacao.modules.subscriptions.repository.SubscriptionRepository;
import ao.saas.faturacao.modules.users.entity.User;
import ao.saas.faturacao.modules.users.repository.UserRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {
        private final CompanyRepository companyRepo;
        private final CompanyUserRepository companyUserRepo;
        private final UserRepository userRepo;
        private final SubscriptionRepository subscriptionRepo;
        private final InvoiceRepository invoiceRepo;
        private final CustomerRepository customerRepo;
        private final ProductRepository productRepo;
        private final AuditService auditService;

        public List<Company> listForUser(UUID userId) {
                return companyUserRepo.findByUserId(userId).stream()
                                .map(CompanyUser::getCompany)
                                .filter(c -> c.getDeletedAt() == null)
                                .collect(Collectors.toList());
        }

        public Company getOne(UUID companyId) {
                return companyRepo.findByIdAndDeletedAtIsNull(companyId)
                                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        }

        @Transactional
        public Company create(UUID userId, Company dto) {

                if (companyRepo.existsByNif(dto.getNif())) {
                        throw BusinessException.conflict("Já existe uma empresa com este NIF");
                }

                User user = userRepo.findByIdAndDeletedAtIsNull(userId)
                                .orElseThrow(() -> BusinessException.notFound("Utilizador não encontrado"));

                dto.setStatus(CompanyStatus.TRIAL);

                log.info("[COMPANY-CREATE] 1. A guardar a empresa: {}", dto.getName());

                // Salva a empresa e limpa o cascade temporário desvinculando relações inline
                Company company = companyRepo.save(dto);

                log.info("[COMPANY-CREATE] 2. Empresa guardada fisicamente. ID gerado: {}", company.getId());

                // Criar a relação utilizador-empresa
                CompanyUser cu = CompanyUser.builder()
                                .user(user)
                                .company(company)
                                .role(UserRole.ADMIN)
                                .isDefault(true)
                                .build();
                companyUserRepo.saveAndFlush(cu);

                log.info("[COMPANY-CREATE] 3. Relação CompanyUser criada para a empresa.");

                // Montar a subscrição completa
                Subscription sub = Subscription.builder()
                                .plan(SubscriptionPlan.FREE)
                                .status(SubscriptionStatus.TRIALING)
                                .trialEndsAt(LocalDateTime.now().plusDays(14))
                                .currentPeriodStart(LocalDateTime.now())
                                .currentPeriodEnd(LocalDateTime.now().plusDays(14))
                                .invoicesThisMonth(0)
                                .maxUsers(2)
                                .maxInvoices(10)
                                .maxCustomers(50)
                                .maxProducts(100)
                                .externalId("SUB-" + company.getNif())
                                .build();

                // Força a injeção do objeto de forma isolada via setter antes do flush
                sub.setCompany(company);

                log.info("[COMPANY-CREATE] 4. A tentar persistir a subscrição isolada no Banco...");

                // Salva a subscrição de forma isolada forcando a gravação
                subscriptionRepo.saveAndFlush(sub);

                // Sincroniza o lado da empresa apenas na memória para o retorno da API
                company.setSubscription(sub);

                log.info("[COMPANY-CREATE] 5. Todo o ecossistema guardado. A gerar Log de Auditoria.");

                auditService.log(
                                company.getId(),
                                userId,
                                AuditAction.CREATE,
                                "Company",
                                company.getId());

                return company;
        }

        @Transactional
        public Company update(UUID companyId, UUID userId, Company dto) {
                Company existing = getOne(companyId);
                if (!existing.getNif().equals(dto.getNif()) &&
                                companyRepo.existsByNifAndIdNot(dto.getNif(), companyId)) {
                        throw BusinessException.conflict("Já existe uma empresa com este NIF");
                }
                existing.setName(dto.getName());
                existing.setTradeName(dto.getTradeName());
                existing.setNif(dto.getNif());
                existing.setType(dto.getType());
                existing.setTaxRegime(dto.getTaxRegime());
                existing.setAddress(dto.getAddress());
                existing.setCity(dto.getCity());
                existing.setProvince(dto.getProvince());
                existing.setPhone(dto.getPhone());
                existing.setEmail(dto.getEmail());
                existing.setWebsite(dto.getWebsite());
                existing.setBankName(dto.getBankName());
                existing.setBankAccount(dto.getBankAccount());
                existing.setBankIban(dto.getBankIban());
                existing.setInvoicePrefix(dto.getInvoicePrefix());
                existing.setInvoiceSeries(dto.getInvoiceSeries());
                existing.setDefaultDueDays(dto.getDefaultDueDays());
                existing.setDefaultNotes(dto.getDefaultNotes());
                existing.setTermsConditions(dto.getTermsConditions());
                auditService.log(companyId, userId, AuditAction.UPDATE, "Company", companyId);
                return companyRepo.save(existing);
        }

        // ── Dashboard stats ────────────────────────────────────────────
        public DashboardStats getDashboard(UUID companyId) {
                LocalDate today = LocalDate.now();
                LocalDate monthStart = today.withDayOfMonth(1);
                LocalDate yearStart = today.withDayOfYear(1);

                BigDecimal monthRevenue = Optional.ofNullable(
                                invoiceRepo.sumTotal(companyId, InvoiceStatus.PAID, monthStart, today))
                                .orElse(BigDecimal.ZERO);
                BigDecimal yearRevenue = Optional.ofNullable(
                                invoiceRepo.sumTotal(companyId, InvoiceStatus.PAID, yearStart, today))
                                .orElse(BigDecimal.ZERO);
                BigDecimal totalDue = Optional.ofNullable(
                                invoiceRepo.sumAmountDue(companyId, Arrays.asList(
                                                InvoiceStatus.PENDING, InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID,
                                                InvoiceStatus.OVERDUE)))
                                .orElse(BigDecimal.ZERO);

                long totalInvoices = invoiceRepo.countByCompanyId(companyId);
                long totalCustomers = customerRepo.countByCompanyIdAndDeletedAtIsNull(companyId);
                long totalProducts = productRepo.countByCompanyIdAndDeletedAtIsNull(companyId);
                long overdueCount = invoiceRepo.countByCompanyIdAndStatus(companyId, InvoiceStatus.OVERDUE);
                long draftCount = invoiceRepo.countByCompanyIdAndStatus(companyId, InvoiceStatus.DRAFT);

                return DashboardStats.builder()
                                .monthRevenue(monthRevenue).yearRevenue(yearRevenue)
                                .totalAmountDue(totalDue).totalInvoices(totalInvoices)
                                .totalCustomers(totalCustomers).totalProducts(totalProducts)
                                .overdueCount(overdueCount).draftCount(draftCount)
                                .build();
        }

        @Data
        @Builder
        public static class DashboardStats {
                private BigDecimal monthRevenue;
                private BigDecimal yearRevenue;
                private BigDecimal totalAmountDue;
                private long totalInvoices;
                private long totalCustomers;
                private long totalProducts;
                private long overdueCount;
                private long draftCount;
        }
}
