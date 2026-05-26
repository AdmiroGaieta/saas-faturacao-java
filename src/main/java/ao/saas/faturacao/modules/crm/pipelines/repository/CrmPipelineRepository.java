package ao.saas.faturacao.modules.crm.pipelines.repository;
import ao.saas.faturacao.modules.crm.pipelines.entity.CrmPipeline;
import ao.saas.faturacao.modules.crm.pipelines.entity.CrmStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface CrmPipelineRepository extends JpaRepository<CrmPipeline,UUID> {
    List<CrmPipeline> findByCompanyIdAndIsActiveTrueOrderBySortOrderAsc(UUID companyId);
    Optional<CrmPipeline> findByCompanyIdAndIsDefaultTrue(UUID companyId);
}
