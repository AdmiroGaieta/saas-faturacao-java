package ao.saas.faturacao.modules.taxrates.service;

import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.taxrates.entity.TaxRate;
import ao.saas.faturacao.modules.taxrates.repository.TaxRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxRateService {
    private final TaxRateRepository repo;
    private final CompanyRepository companyRepo;

    public List<TaxRate> list(UUID companyId) {
        return repo.findByCompanyIdAndIsActiveTrue(companyId);
    }

    @Transactional
    public TaxRate create(UUID companyId, TaxRate dto) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            repo.findByCompanyIdAndIsDefaultTrue(companyId)
                    .ifPresent(t -> { t.setIsDefault(false); repo.save(t); });
        }
        dto.setCompany(company);
        return repo.save(dto);
    }
}
