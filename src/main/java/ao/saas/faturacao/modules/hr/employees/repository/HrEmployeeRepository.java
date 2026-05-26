package ao.saas.faturacao.modules.hr.employees.repository;
import ao.saas.faturacao.common.enums.EmployeeStatus;
import ao.saas.faturacao.modules.hr.employees.entity.HrEmployee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface HrEmployeeRepository extends JpaRepository<HrEmployee,UUID> {
    Optional<HrEmployee> findByIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID companyId);
    @Query("SELECT e FROM HrEmployee e WHERE e.company.id=:cid AND e.deletedAt IS NULL AND (:q IS NULL OR LOWER(CONCAT(e.firstName,' ',e.lastName)) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(COALESCE(e.employeeNumber,'')) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<HrEmployee> search(@Param("cid") UUID companyId, @Param("q") String q, Pageable pageable);
    List<HrEmployee> findByCompanyIdAndStatusAndDeletedAtIsNull(UUID companyId, EmployeeStatus status);
    long countByCompanyIdAndStatusAndDeletedAtIsNull(UUID companyId, EmployeeStatus status);
}
