package ao.saas.faturacao.modules.expenses.cost_centers.repository;
import ao.saas.faturacao.modules.expenses.cost_centers.entity.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface CostCenterRepository extends JpaRepository<CostCenter,UUID> {
    List<CostCenter> findByCompanyIdAndIsActiveTrueOrderByNameAsc(UUID companyId);
}
