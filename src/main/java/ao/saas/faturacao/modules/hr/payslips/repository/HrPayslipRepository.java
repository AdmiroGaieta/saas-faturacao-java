package ao.saas.faturacao.modules.hr.payslips.repository;
import ao.saas.faturacao.modules.hr.payslips.entity.HrPayslip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface HrPayslipRepository extends JpaRepository<HrPayslip,UUID> {
    Optional<HrPayslip> findByIdAndCompanyId(UUID id, UUID companyId);
    List<HrPayslip> findByPayrollIdOrderByEmployeeLastNameAsc(UUID payrollId);
    List<HrPayslip> findByEmployeeIdOrderByPeriodYearDescPeriodMonthDesc(UUID employeeId);
    Optional<HrPayslip> findByPayrollIdAndEmployeeId(UUID payrollId, UUID employeeId);
}
