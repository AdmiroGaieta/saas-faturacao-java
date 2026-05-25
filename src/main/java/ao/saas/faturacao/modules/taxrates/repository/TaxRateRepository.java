package ao.saas.faturacao.modules.taxrates.repository;
import ao.saas.faturacao.modules.taxrates.entity.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface TaxRateRepository extends JpaRepository<TaxRate, UUID> {
    List<TaxRate> findByCompanyIdAndIsActiveTrue(UUID companyId);
    Optional<TaxRate> findByCompanyIdAndIsDefaultTrue(UUID companyId);
}
