package ao.saas.faturacao.modules.hr.departments.repository;
import ao.saas.faturacao.modules.hr.departments.entity.HrPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface HrPositionRepository extends JpaRepository<HrPosition,UUID> {
    List<HrPosition> findByCompanyIdAndIsActiveTrueOrderByNameAsc(UUID companyId);
    List<HrPosition> findByDepartmentIdAndIsActiveTrue(UUID deptId);
}
