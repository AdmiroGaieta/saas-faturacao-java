package ao.saas.faturacao.modules.customers.service;

import ao.saas.faturacao.common.enums.AuditAction;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.audit.service.AuditService;
import ao.saas.faturacao.modules.customers.entity.Customer;
import ao.saas.faturacao.modules.customers.repository.CustomerRepository;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repo;
    private final CompanyRepository  companyRepo;
    private final AuditService       auditService;

    public Page<Customer> list(UUID companyId, String search, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "companyName", "firstName"));
        return repo.searchByCompany(companyId, search == null || search.isBlank() ? null : search, pageable);
    }

    public Customer getOne(UUID companyId, UUID id) {
        return repo.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId)
                .orElseThrow(() -> BusinessException.notFound("Cliente não encontrado"));
    }

    @Transactional
    public Customer create(UUID companyId, UUID userId, Customer dto) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        dto.setCompany(company);
        Customer saved = repo.save(dto);
        auditService.log(companyId, userId, AuditAction.CREATE, "Customer", saved.getId());
        return saved;
    }

    @Transactional
    public Customer update(UUID companyId, UUID id, UUID userId, Customer dto) {
        Customer existing = getOne(companyId, id);
        existing.setType(dto.getType());
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setCompanyName(dto.getCompanyName());
        existing.setTradeName(dto.getTradeName());
        existing.setNif(dto.getNif());
        existing.setAddress(dto.getAddress());
        existing.setCity(dto.getCity());
        existing.setProvince(dto.getProvince());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setPhone2(dto.getPhone2());
        existing.setPaymentTerms(dto.getPaymentTerms());
        existing.setCreditLimit(dto.getCreditLimit());
        existing.setNotes(dto.getNotes());
        auditService.log(companyId, userId, AuditAction.UPDATE, "Customer", id);
        return repo.save(existing);
    }

    @Transactional
    public void delete(UUID companyId, UUID id, UUID userId) {
        Customer c = getOne(companyId, id);
        c.setDeletedAt(LocalDateTime.now());
        c.setIsActive(false);
        repo.save(c);
        auditService.log(companyId, userId, AuditAction.DELETE, "Customer", id);
    }
}
