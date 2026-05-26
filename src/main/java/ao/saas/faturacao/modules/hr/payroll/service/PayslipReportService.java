package ao.saas.faturacao.modules.hr.payroll.service;

import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.hr.payroll.entity.HrPayroll;
import ao.saas.faturacao.modules.hr.payslips.entity.HrPayslip;
import ao.saas.faturacao.modules.hr.payslips.repository.HrPayslipRepository;
import ao.saas.faturacao.modules.reports.service.JasperReportService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayslipReportService {

    private final JasperReportService  jasperService;
    private final HrPayslipRepository  payslipRepo;
    private final CompanyRepository    companyRepo;
    private final IrtCalculator        irtCalc;

    private static final NumberFormat AOA_FMT;
    private static final String[] MONTH_NAMES = {
        "Janeiro","Fevereiro","Março","Abril","Maio","Junho",
        "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro","13º Mês"
    };

    static {
        AOA_FMT = NumberFormat.getInstance(new Locale("pt","AO"));
        AOA_FMT.setMinimumFractionDigits(2);
        AOA_FMT.setMaximumFractionDigits(2);
    }

    // ── Recibo Individual PDF ──────────────────────────────────────

    public byte[] generatePayslipPdf(UUID companyId, UUID payslipId) {
        HrPayslip slip = payslipRepo.findByIdAndCompanyId(payslipId, companyId)
                .orElseThrow(() -> BusinessException.notFound("Recibo não encontrado"));
        return jasperService.generatePdf("hr/payslip", buildPayslipParams(slip), List.of(buildPayslipRow(slip)));
    }

    // ── Folha de Pagamento completa PDF ───────────────────────────

    public byte[] generatePayrollSummaryPdf(UUID companyId, UUID payrollId) {
        List<HrPayslip> slips = payslipRepo.findByPayrollIdOrderByEmployeeLastNameAsc(payrollId);
        if (slips.isEmpty()) throw BusinessException.badRequest("Folha sem recibos calculados");

        HrPayroll payroll = slips.get(0).getPayroll();
        Company   company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));

        Map<String, Object> params = buildSummaryParams(company, payroll);
        List<PayrollSummaryRow> rows = slips.stream()
                .map(this::buildSummaryRow)
                .collect(Collectors.toList());

        return jasperService.generatePdf("hr/payroll_summary", params, rows);
    }

    public byte[] generatePayrollSummaryXlsx(UUID companyId, UUID payrollId) {
        List<HrPayslip> slips = payslipRepo.findByPayrollIdOrderByEmployeeLastNameAsc(payrollId);
        if (slips.isEmpty()) throw BusinessException.badRequest("Folha sem recibos calculados");
        HrPayroll payroll = slips.get(0).getPayroll();
        Company   company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        Map<String, Object> params = buildSummaryParams(company, payroll);
        List<PayrollSummaryRow> rows = slips.stream().map(this::buildSummaryRow).collect(Collectors.toList());
        return jasperService.generateXlsx("hr/payroll_summary", params, rows);
    }

    // ── Builders de parâmetros ─────────────────────────────────────

    private Map<String, Object> buildPayslipParams(HrPayslip slip) {
        HrPayroll payroll = slip.getPayroll();
        Company   company = slip.getCompany();

        Map<String, Object> p = new HashMap<>();
        // Empresa
        p.put("COMPANY_NAME",    company.getName());
        p.put("COMPANY_NIF",     company.getNif());
        p.put("COMPANY_ADDRESS", buildAddress(company));
        // Colaborador
        p.put("EMP_NUMBER",      nvl(slip.getEmployee().getEmployeeNumber(), "—"));
        p.put("EMP_NAME",        slip.getEmployee().getFullName());
        p.put("EMP_POSITION",    slip.getEmployee().getPosition() != null
                                    ? slip.getEmployee().getPosition().getName() : "—");
        p.put("EMP_DEPARTMENT",  slip.getEmployee().getDepartment() != null
                                    ? slip.getEmployee().getDepartment().getName() : "—");
        p.put("EMP_NIF",         nvl(slip.getEmployee().getNif(), "—"));
        p.put("EMP_INSS",        nvl(slip.getEmployee().getInssNumber(), "—"));
        p.put("EMP_BANK",        nvl(slip.getEmployee().getBankName(), "—"));
        p.put("EMP_ACCOUNT",     nvl(slip.getEmployee().getBankAccount(), "—"));
        // Período
        p.put("PERIOD",          MONTH_NAMES[slip.getPeriodMonth()-1] + " " + slip.getPeriodYear());
        p.put("WORKING_DAYS",    slip.getWorkingDays());
        p.put("ATTENDED_DAYS",   slip.getAttendedDays());
        p.put("ABSENT_DAYS",     slip.getAbsentDays());
        // Remuneração base
        p.put("BASE_SALARY",     fmt(slip.getBaseSalary()));
        // Subsídios
        p.put("FOOD_ALLOWANCE",       fmt(slip.getFoodAllowance()));
        p.put("TRANSPORT_ALLOWANCE",  fmt(slip.getTransportAllowance()));
        p.put("HOUSING_ALLOWANCE",    fmt(slip.getHousingAllowance()));
        p.put("FAMILY_ALLOWANCE",     fmt(slip.getFamilyAllowance()));
        p.put("PRODUCTION_BONUS",     fmt(slip.getProductionBonus()));
        p.put("OVERTIME_PAY",         fmt(slip.getOvertimePay()));
        p.put("OTHER_ALLOWANCES",     fmt(slip.getOtherAllowances()));
        p.put("TOTAL_ALLOWANCES",     fmt(slip.getTotalAllowances()));
        // Gross
        p.put("GROSS_SALARY",    fmt(slip.getGrossSalary()));
        // Descontos
        p.put("INSS_EMPLOYEE",   fmt(slip.getInssEmployee()));
        p.put("INSS_RATE",       "3%");
        p.put("IRT_AMOUNT",      fmt(slip.getIrtAmount()));
        p.put("IRT_RATE",        irtCalc.effectiveIrtRate(slip.getGrossSalary(), slip.getIrtAmount()) + "%");
        p.put("ADVANCE_DED",     fmt(slip.getAdvanceDeduction()));
        p.put("LOAN_DED",        fmt(slip.getLoanDeduction()));
        p.put("OTHER_DED",       fmt(slip.getOtherDeductions()));
        p.put("TOTAL_DEDUCTIONS",fmt(slip.getTotalDeductions()));
        // Líquido
        p.put("NET_SALARY",      fmt(slip.getNetSalary()));
        p.put("NET_WORDS",       amountInWords(slip.getNetSalary()));
        // INSS Entidade empregadora (informativo)
        p.put("INSS_EMPLOYER",   fmt(slip.getInssEmployer()));
        p.put("INSS_EMP_RATE",   "8%");
        // Rodapé
        p.put("GENERATED_AT",    java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        p.put("STATUS",          slip.getIsPaid() ? "PAGO" : "PENDENTE");
        return p;
    }

    private Map<String, Object> buildSummaryParams(Company company, HrPayroll payroll) {
        Map<String, Object> p = new HashMap<>();
        p.put("COMPANY_NAME",    company.getName());
        p.put("COMPANY_NIF",     company.getNif());
        p.put("PERIOD",          payroll.getPeriodLabel());
        p.put("TOTAL_EMPLOYEES", payroll.getPayslips().size());
        p.put("TOTAL_GROSS",     fmt(payroll.getTotalGross()));
        p.put("TOTAL_INSS_EMP",  fmt(payroll.getTotalInssEmployee()));
        p.put("TOTAL_INSS_EMP2", fmt(payroll.getTotalInssEmployer()));
        p.put("TOTAL_IRT",       fmt(payroll.getTotalIrt()));
        p.put("TOTAL_DED",       fmt(payroll.getTotalDeductions()));
        p.put("TOTAL_NET",       fmt(payroll.getTotalNet()));
        p.put("GENERATED_AT",    java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return p;
    }

    // ── Row DTOs ───────────────────────────────────────────────────

    private PayslipRow buildPayslipRow(HrPayslip slip) {
        return PayslipRow.builder().description("Recibo de Vencimento").build();
    }

    private PayrollSummaryRow buildSummaryRow(HrPayslip slip) {
        return PayrollSummaryRow.builder()
                .employeeNumber(nvl(slip.getEmployee().getEmployeeNumber(), "—"))
                .employeeName(slip.getEmployee().getFullName())
                .department(slip.getEmployee().getDepartment() != null
                        ? slip.getEmployee().getDepartment().getName() : "—")
                .position(slip.getEmployee().getPosition() != null
                        ? slip.getEmployee().getPosition().getName() : "—")
                .baseSalary(fmt(slip.getBaseSalary()))
                .totalAllowances(fmt(slip.getTotalAllowances()))
                .grossSalary(fmt(slip.getGrossSalary()))
                .inssEmployee(fmt(slip.getInssEmployee()))
                .irtAmount(fmt(slip.getIrtAmount()))
                .totalDeductions(fmt(slip.getTotalDeductions()))
                .netSalary(fmt(slip.getNetSalary()))
                .status(slip.getIsPaid() ? "Pago" : "Pendente")
                .build();
    }

    // ── Utilitários ────────────────────────────────────────────────

    private String fmt(BigDecimal v) {
        if (v == null) return "0,00 AOA";
        return AOA_FMT.format(v) + " AOA";
    }

    private String nvl(String s, String def) { return (s != null && !s.isBlank()) ? s : def; }

    private String buildAddress(Company c) {
        StringBuilder sb = new StringBuilder();
        if (c.getAddress() != null)  sb.append(c.getAddress());
        if (c.getCity()    != null)  sb.append(", ").append(c.getCity());
        if (c.getProvince()!= null)  sb.append(", ").append(c.getProvince());
        return sb.toString();
    }

    /** Converte valor para extenso (simplificado) */
    private String amountInWords(BigDecimal amount) {
        if (amount == null) return "Zero Kwanzas";
        long intPart = amount.longValue();
        long cents   = amount.remainder(BigDecimal.ONE)
                             .multiply(BigDecimal.valueOf(100))
                             .longValue();
        String result = numberToWords(intPart) + " Kwanza" + (intPart != 1 ? "s" : "");
        if (cents > 0) result += " e " + numberToWords(cents) + " Cêntimo" + (cents != 1 ? "s" : "");
        return result;
    }

    private String numberToWords(long n) {
        if (n == 0) return "Zero";
        String[] u = {"","Um","Dois","Três","Quatro","Cinco","Seis","Sete","Oito","Nove",
            "Dez","Onze","Doze","Treze","Catorze","Quinze","Dezasseis","Dezassete","Dezoito","Dezanove"};
        String[] d = {"","","Vinte","Trinta","Quarenta","Cinquenta","Sessenta","Setenta","Oitenta","Noventa"};
        if (n < 20)         return u[(int)n];
        if (n < 100)        return d[(int)(n/10)] + (n%10 > 0 ? " e " + u[(int)(n%10)] : "");
        if (n < 1_000)      return u[(int)(n/100)] + (n/100 == 1 ? "cem" : "centos") + (n%100 > 0 ? " e " + numberToWords(n%100) : "").replace("Umcent","Cent");
        if (n < 1_000_000)  return numberToWords(n/1_000) + " Mil" + (n%1_000 > 0 ? " e " + numberToWords(n%1_000) : "");
        return numberToWords(n/1_000_000) + " Milh" + (n/1_000_000 == 1 ? "ão" : "ões") + (n%1_000_000 > 0 ? " e " + numberToWords(n%1_000_000) : "");
    }

    // ── Data classes ───────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PayslipRow { private String description; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PayrollSummaryRow {
        private String employeeNumber, employeeName, department, position;
        private String baseSalary, totalAllowances, grossSalary;
        private String inssEmployee, irtAmount, totalDeductions, netSalary;
        private String status;
    }
}
