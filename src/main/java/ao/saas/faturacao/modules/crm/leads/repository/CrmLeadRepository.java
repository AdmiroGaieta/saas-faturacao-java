package ao.saas.faturacao.modules.crm.leads.repository;
import ao.saas.faturacao.common.enums.LeadStatus;
import ao.saas.faturacao.modules.crm.leads.entity.CrmLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface CrmLeadRepository extends JpaRepository<CrmLead,UUID>, JpaSpecificationExecutor<CrmLead> {
    Optional<CrmLead> findByIdAndCompanyId(UUID id, UUID companyId);
    long countByCompanyIdAndStatus(UUID companyId, LeadStatus status);
    @Query("SELECT COALESCE(SUM(l.value),0) FROM CrmLead l WHERE l.company.id=:cid AND l.status='WON'")
    BigDecimal sumWonValue(@Param("cid") UUID companyId);
    List<CrmLead> findByCompanyIdAndPipelineIdOrderByCreatedAtDesc(UUID companyId, UUID pipelineId);
}
