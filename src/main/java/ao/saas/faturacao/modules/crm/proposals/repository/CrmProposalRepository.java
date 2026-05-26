package ao.saas.faturacao.modules.crm.proposals.repository;
import ao.saas.faturacao.modules.crm.proposals.entity.CrmProposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface CrmProposalRepository extends JpaRepository<CrmProposal,UUID> {
    Optional<CrmProposal> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<CrmProposal> findByCompanyIdOrderByCreatedAtDesc(UUID companyId, Pageable pageable);
}
