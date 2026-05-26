package ao.saas.faturacao.modules.crm.activities.repository;
import ao.saas.faturacao.modules.crm.activities.entity.CrmActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface CrmActivityRepository extends JpaRepository<CrmActivity,UUID> {
    Page<CrmActivity> findByCompanyIdOrderByScheduledAtDesc(UUID companyId, Pageable pageable);
    Page<CrmActivity> findByLeadIdOrderByCreatedAtDesc(UUID leadId, Pageable pageable);
}
