package ao.saas.faturacao.modules.hr.departments.repository;
import ao.saas.faturacao.modules.hr.departments.entity.HrDepartment;
import ao.saas.faturacao.modules.hr.departments.entity.HrPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface HrDepartmentRepository extends JpaRepository<HrDepartment,UUID> {
    List<HrDepartment> findByCompanyIdAndIsActiveTrueOrderByNameAsc(UUID companyId);
}
