package ao.saas.faturacao.modules.hr.leaves.repository;
import ao.saas.faturacao.common.enums.LeaveStatus;
import ao.saas.faturacao.modules.hr.leaves.entity.HrLeave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface HrLeaveRepository extends JpaRepository<HrLeave,UUID> {
    Page<HrLeave> findByCompanyIdOrderByCreatedAtDesc(UUID companyId, Pageable pageable);
    List<HrLeave> findByEmployeeIdAndStatus(UUID employeeId, LeaveStatus status);
    long countByCompanyIdAndStatus(UUID companyId, LeaveStatus status);
}
