package ao.saas.faturacao.modules.hr.payroll.service;

import ao.saas.faturacao.common.enums.AuditAction;
import ao.saas.faturacao.common.enums.EmployeeStatus;
import ao.saas.faturacao.common.enums.PayrollStatus;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.audit.service.AuditService;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.hr.attendance.repository.HrAttendanceRepository;
import ao.saas.faturacao.modules.hr.employees.entity.HrEmployee;
import ao.saas.faturacao.modules.hr.employees.repository.HrEmployeeRepository;
import ao.saas.faturacao.modules.hr.payroll.entity.HrPayroll;
import ao.saas.faturacao.modules.hr.payroll.repository.HrPayrollRepository;
import ao.saas.faturacao.modules.hr.payslips.entity.HrPayslip;
import ao.saas.faturacao.modules.hr.payslips.repository.HrPayslipRepository;
import ao.saas.faturacao.modules.users.entity.User;
import ao.saas.faturacao.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollService {

    private final HrPayrollRepository  payrollRepo;
    private final HrPayslipRepository  payslipRepo;
    private final HrEmployeeRepository employeeRepo;
    private final CompanyRepository    companyRepo;
    private final UserRepository       userRepo;
    private final HrAttendanceRepository attendanceRepo;
    private final IrtCalculator        irtCalc;
    private final AuditService         auditService;

    private static final int DEFAULT_WORKING_DAYS = 22;

    // ── Listar folhas ──────────────────────────────────────────────

    public Page<HrPayroll> list(UUID companyId, int page, int limit) {
        return payrollRepo.findByCompanyIdOrderByPeriodYearDescPeriodMonthDesc(
                companyId, PageRequest.of(page - 1, limit));
    }

    public HrPayroll getOne(UUID companyId, UUID payrollId) {
        return payrollRepo.findByIdAndCompanyId(payrollId, companyId)
                .orElseThrow(() -> BusinessException.notFound("Folha de pagamento não encontrada"));
    }

    // ── Criar folha ────────────────────────────────────────────────

    @Transactional
    public HrPayroll create(UUID companyId, UUID userId, int year, int month, String description) {
        // Verificar se já existe
        if (payrollRepo.findByCompanyIdAndPeriodYearAndPeriodMonth(companyId, year, month).isPresent()) {
            throw BusinessException.conflict(
                "Já existe uma folha de pagamento para " + month + "/" + year);
        }

        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));

        HrPayroll payroll = HrPayroll.builder()
                .company(company)
                .periodYear(year)
                .periodMonth(month)
                .description(description != null ? description : buildDescription(year, month))
                .status(PayrollStatus.DRAFT)
                .totalGross(BigDecimal.ZERO)
                .totalInssEmployee(BigDecimal.ZERO)
                .totalInssEmployer(BigDecimal.ZERO)
                .totalIrt(BigDecimal.ZERO)
                .totalDeductions(BigDecimal.ZERO)
                .totalNet(BigDecimal.ZERO)
                .build();

        HrPayroll saved = payrollRepo.save(payroll);
        auditService.log(companyId, userId, AuditAction.CREATE, "HrPayroll", saved.getId());
        return saved;
    }

    // ── Calcular (gerar recibos por colaborador) ───────────────────

    @Transactional
    public HrPayroll calculate(UUID companyId, UUID payrollId, UUID userId) {
        HrPayroll payroll = getOne(companyId, payrollId);

        if (payroll.getStatus() == PayrollStatus.APPROVED ||
                payroll.getStatus() == PayrollStatus.PAID) {
            throw BusinessException.badRequest("Folha já aprovada/paga — não é possível recalcular");
        }

        // Apagar recibos anteriores se recalcular
        payslipRepo.deleteAll(payslipRepo.findByPayrollIdOrderByEmployeeLastNameAsc(payrollId));

        // Buscar todos os colaboradores activos
        List<HrEmployee> employees = employeeRepo.findByCompanyIdAndStatusAndDeletedAtIsNull(
                companyId, EmployeeStatus.ACTIVE);

        if (employees.isEmpty()) {
            throw BusinessException.badRequest("Não existem colaboradores activos para processar");
        }

        LocalDate periodStart = LocalDate.of(payroll.getPeriodYear(), payroll.getPeriodMonth(), 1);
        LocalDate periodEnd   = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

        BigDecimal totalGross    = BigDecimal.ZERO;
        BigDecimal totalInssEmp  = BigDecimal.ZERO;
        BigDecimal totalInssEmp2 = BigDecimal.ZERO;
        BigDecimal totalIrt      = BigDecimal.ZERO;
        BigDecimal totalDed      = BigDecimal.ZERO;
        BigDecimal totalNet      = BigDecimal.ZERO;

        for (HrEmployee emp : employees) {
            HrPayslip slip = calculatePayslip(payroll, emp, periodStart, periodEnd);
            payslipRepo.save(slip);

            totalGross    = totalGross.add(slip.getGrossSalary());
            totalInssEmp  = totalInssEmp.add(slip.getInssEmployee());
            totalInssEmp2 = totalInssEmp2.add(slip.getInssEmployer());
            totalIrt      = totalIrt.add(slip.getIrtAmount());
            totalDed      = totalDed.add(slip.getTotalDeductions());
            totalNet      = totalNet.add(slip.getNetSalary());
        }

        payroll.setTotalGross(totalGross);
        payroll.setTotalInssEmployee(totalInssEmp);
        payroll.setTotalInssEmployer(totalInssEmp2);
        payroll.setTotalIrt(totalIrt);
        payroll.setTotalDeductions(totalDed);
        payroll.setTotalNet(totalNet);
        payroll.setStatus(PayrollStatus.CALCULATED);

        auditService.log(companyId, userId, AuditAction.UPDATE, "HrPayroll", payrollId);
        log.info("Folha {} calculada: {} colaboradores | Líquido total: {}", payrollId, employees.size(), totalNet);
        return payrollRepo.save(payroll);
    }

    // ── Aprovar ────────────────────────────────────────────────────

    @Transactional
    public HrPayroll approve(UUID companyId, UUID payrollId, UUID userId) {
        HrPayroll payroll = getOne(companyId, payrollId);
        if (payroll.getStatus() != PayrollStatus.CALCULATED) {
            throw BusinessException.badRequest("Apenas folhas calculadas podem ser aprovadas");
        }
        User approver = userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilizador não encontrado"));
        payroll.setStatus(PayrollStatus.APPROVED);
        payroll.setApprovedBy(approver);
        payroll.setApprovedAt(LocalDateTime.now());
        auditService.log(companyId, userId, AuditAction.UPDATE, "HrPayroll", payrollId);
        return payrollRepo.save(payroll);
    }

    // ── Marcar como Pago ───────────────────────────────────────────

    @Transactional
    public HrPayroll markPaid(UUID companyId, UUID payrollId, UUID userId) {
        HrPayroll payroll = getOne(companyId, payrollId);
        if (payroll.getStatus() != PayrollStatus.APPROVED) {
            throw BusinessException.badRequest("Apenas folhas aprovadas podem ser marcadas como pagas");
        }
        payroll.setStatus(PayrollStatus.PAID);
        payroll.setPaidAt(LocalDateTime.now());
        // Marcar cada recibo como pago
        payslipRepo.findByPayrollIdOrderByEmployeeLastNameAsc(payrollId).forEach(slip -> {
            slip.setIsPaid(true);
            slip.setPaidAt(LocalDateTime.now());
        });
        auditService.log(companyId, userId, AuditAction.PAY, "HrPayroll", payrollId);
        return payrollRepo.save(payroll);
    }

    // ── Listar recibos de uma folha ────────────────────────────────

    public List<HrPayslip> listPayslips(UUID companyId, UUID payrollId) {
        getOne(companyId, payrollId); // valida acesso
        return payslipRepo.findByPayrollIdOrderByEmployeeLastNameAsc(payrollId);
    }

    public HrPayslip getPayslip(UUID companyId, UUID payslipId) {
        return payslipRepo.findByIdAndCompanyId(payslipId, companyId)
                .orElseThrow(() -> BusinessException.notFound("Recibo não encontrado"));
    }

    public List<HrPayslip> getEmployeePayslips(UUID companyId, UUID employeeId) {
        employeeRepo.findByIdAndCompanyIdAndDeletedAtIsNull(employeeId, companyId)
                .orElseThrow(() -> BusinessException.notFound("Colaborador não encontrado"));
        return payslipRepo.findByEmployeeIdOrderByPeriodYearDescPeriodMonthDesc(employeeId);
    }

    // ── Cálculo de um recibo individual ───────────────────────────

    private HrPayslip calculatePayslip(HrPayroll payroll, HrEmployee emp,
                                        LocalDate periodStart, LocalDate periodEnd) {
        BigDecimal baseSalary = emp.getBaseSalary();

        // Assiduidade
        int attendedDays = (int) attendanceRepo.countPresentDays(
                emp.getId(), periodStart, periodEnd);
        if (attendedDays == 0) attendedDays = DEFAULT_WORKING_DAYS; // default se sem registos

        // Calcular salário proporcional ao número de dias trabalhados
        BigDecimal proportionalBase = attendedDays == DEFAULT_WORKING_DAYS ? baseSalary
                : baseSalary.multiply(BigDecimal.valueOf(attendedDays))
                            .divide(BigDecimal.valueOf(DEFAULT_WORKING_DAYS), 2, RoundingMode.HALF_UP);

        // Subsídios padrão (personalizáveis por empresa / colaborador)
        BigDecimal foodAllowance      = BigDecimal.valueOf(5_000);   // Padrão Angola
        BigDecimal transportAllowance = BigDecimal.valueOf(3_000);

        // Calcular gross (base proporcional + subsídios — subsídios não entram no IRT)
        BigDecimal grossSalary  = proportionalBase
                .add(foodAllowance).add(transportAllowance);
        BigDecimal totalAllowances = foodAllowance.add(transportAllowance);

        // INSS (incide sobre salário base, não sobre subsídios)
        BigDecimal inssEmployee = irtCalc.calcInssEmployee(proportionalBase);
        BigDecimal inssEmployer = irtCalc.calcInssEmployer(proportionalBase);

        // IRT (incide sobre gross − inss empregado)
        BigDecimal irtAmount = irtCalc.calcIrt(grossSalary, inssEmployee);

        // Descontos totais
        BigDecimal totalDeductions = inssEmployee.add(irtAmount);

        // Salário líquido
        BigDecimal netSalary = grossSalary.subtract(totalDeductions)
                .setScale(2, RoundingMode.HALF_UP);

        return HrPayslip.builder()
                .payroll(payroll)
                .employee(emp)
                .company(emp.getCompany())
                .periodYear(payroll.getPeriodYear())
                .periodMonth(payroll.getPeriodMonth())
                .workingDays(DEFAULT_WORKING_DAYS)
                .attendedDays(attendedDays)
                .absentDays(DEFAULT_WORKING_DAYS - attendedDays)
                .baseSalary(proportionalBase)
                .foodAllowance(foodAllowance)
                .transportAllowance(transportAllowance)
                .housingAllowance(BigDecimal.ZERO)
                .familyAllowance(BigDecimal.ZERO)
                .productionBonus(BigDecimal.ZERO)
                .overtimePay(BigDecimal.ZERO)
                .otherAllowances(BigDecimal.ZERO)
                .grossSalary(grossSalary)
                .inssEmployee(inssEmployee)
                .inssEmployer(inssEmployer)
                .irtAmount(irtAmount)
                .advanceDeduction(BigDecimal.ZERO)
                .loanDeduction(BigDecimal.ZERO)
                .otherDeductions(BigDecimal.ZERO)
                .totalAllowances(totalAllowances)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .isPaid(false)
                .build();
    }

    private String buildDescription(int year, int month) {
        String[] months = {"Janeiro","Fevereiro","Março","Abril","Maio","Junho",
            "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro","13º Mês"};
        return "Folha de Pagamento — " + months[month - 1] + " " + year;
    }
}
