package ao.saas.faturacao.modules.crm.contracts.repository;
import ao.saas.faturacao.modules.crm.contracts.entity.CrmContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface CrmContractRepository extends JpaRepository<CrmContract,UUID> {
    Optional<CrmContract> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<CrmContract> findByCompanyIdOrderByCreatedAtDesc(UUID companyId, Pageable pageable);
}
