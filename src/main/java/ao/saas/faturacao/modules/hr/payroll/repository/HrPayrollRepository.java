package ao.saas.faturacao.modules.hr.payroll.repository;
import ao.saas.faturacao.modules.hr.payroll.entity.HrPayroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface HrPayrollRepository extends JpaRepository<HrPayroll,UUID> {
    Optional<HrPayroll> findByCompanyIdAndPeriodYearAndPeriodMonth(UUID cid, int year, int month);
    Optional<HrPayroll> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<HrPayroll> findByCompanyIdOrderByPeriodYearDescPeriodMonthDesc(UUID companyId, Pageable pageable);
}
