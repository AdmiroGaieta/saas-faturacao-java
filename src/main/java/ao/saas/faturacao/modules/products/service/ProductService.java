package ao.saas.faturacao.modules.products.service;

import ao.saas.faturacao.common.enums.AuditAction;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.audit.service.AuditService;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.products.entity.Product;
import ao.saas.faturacao.modules.products.repository.ProductRepository;
import ao.saas.faturacao.modules.taxrates.repository.TaxRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepo;
    private final CompanyRepository companyRepo;
    private final TaxRateRepository taxRateRepo;
    private final AuditService      auditService;

    public Page<Product> list(UUID companyId, String search, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("name"));
        return productRepo.searchByCompany(companyId,
                search == null || search.isBlank() ? null : search, pageable);
    }

    public Product getOne(UUID companyId, UUID id) {
        return productRepo.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId)
                .orElseThrow(() -> BusinessException.notFound("Produto não encontrado"));
    }

    @Transactional
    public Product create(UUID companyId, UUID userId, Product dto) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        dto.setCompany(company);
        Product saved = productRepo.save(dto);
        auditService.log(companyId, userId, AuditAction.CREATE, "Product", saved.getId());
        return saved;
    }

    @Transactional
    public Product update(UUID companyId, UUID id, UUID userId, Product dto) {
        Product p = getOne(companyId, id);
        p.setCode(dto.getCode());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setType(dto.getType());
        p.setUnit(dto.getUnit());
        p.setPrice(dto.getPrice());
        p.setManageStock(dto.getManageStock());
        p.setStockQuantity(dto.getStockQuantity());
        p.setMinStock(dto.getMinStock());
        p.setIsActive(dto.getIsActive());
        p.setNotes(dto.getNotes());
        if (dto.getTaxRate() != null && dto.getTaxRate().getId() != null) {
            taxRateRepo.findById(dto.getTaxRate().getId()).ifPresent(p::setTaxRate);
        }
        auditService.log(companyId, userId, AuditAction.UPDATE, "Product", id);
        return productRepo.save(p);
    }

    @Transactional
    public void delete(UUID companyId, UUID id, UUID userId) {
        Product p = getOne(companyId, id);
        p.setDeletedAt(LocalDateTime.now());
        p.setIsActive(false);
        productRepo.save(p);
        auditService.log(companyId, userId, AuditAction.DELETE, "Product", id);
    }
}
