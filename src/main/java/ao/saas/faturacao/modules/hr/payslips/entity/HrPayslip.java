package ao.saas.faturacao.modules.hr.payslips.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.hr.employees.entity.HrEmployee;
import ao.saas.faturacao.modules.hr.payroll.entity.HrPayroll;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="hr_payslips",uniqueConstraints=@UniqueConstraint(columnNames={"payroll_id","employee_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HrPayslip extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="payroll_id",nullable=false) @JsonIgnore
    private HrPayroll payroll;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="employee_id",nullable=false)
    private HrEmployee employee;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @Column(name="period_year",nullable=false) private Integer periodYear;
    @Column(name="period_month",nullable=false) private Integer periodMonth;
    @Column(name="working_days",nullable=false) private Integer workingDays=22;
    @Column(name="attended_days",nullable=false) private Integer attendedDays=22;
    @Column(name="absent_days",nullable=false) private Integer absentDays=0;
    // Remuneração base
    @Column(name="base_salary",nullable=false,precision=15,scale=2) private BigDecimal baseSalary;
    // Subsídios / Abonos
    @Column(name="food_allowance",nullable=false,precision=15,scale=2) private BigDecimal foodAllowance=BigDecimal.ZERO;
    @Column(name="transport_allowance",nullable=false,precision=15,scale=2) private BigDecimal transportAllowance=BigDecimal.ZERO;
    @Column(name="housing_allowance",nullable=false,precision=15,scale=2) private BigDecimal housingAllowance=BigDecimal.ZERO;
    @Column(name="family_allowance",nullable=false,precision=15,scale=2) private BigDecimal familyAllowance=BigDecimal.ZERO;
    @Column(name="production_bonus",nullable=false,precision=15,scale=2) private BigDecimal productionBonus=BigDecimal.ZERO;
    @Column(name="overtime_pay",nullable=false,precision=15,scale=2) private BigDecimal overtimePay=BigDecimal.ZERO;
    @Column(name="other_allowances",nullable=false,precision=15,scale=2) private BigDecimal otherAllowances=BigDecimal.ZERO;
    // Gross
    @Column(name="gross_salary",nullable=false,precision=15,scale=2) private BigDecimal grossSalary;
    // Descontos legais Angola
    @Column(name="inss_employee",nullable=false,precision=15,scale=2) private BigDecimal inssEmployee=BigDecimal.ZERO;
    @Column(name="inss_employer",nullable=false,precision=15,scale=2) private BigDecimal inssEmployer=BigDecimal.ZERO;
    @Column(name="irt_amount",nullable=false,precision=15,scale=2) private BigDecimal irtAmount=BigDecimal.ZERO;
    // Outros descontos
    @Column(name="advance_deduction",nullable=false,precision=15,scale=2) private BigDecimal advanceDeduction=BigDecimal.ZERO;
    @Column(name="loan_deduction",nullable=false,precision=15,scale=2) private BigDecimal loanDeduction=BigDecimal.ZERO;
    @Column(name="other_deductions",nullable=false,precision=15,scale=2) private BigDecimal otherDeductions=BigDecimal.ZERO;
    // Totais
    @Column(name="total_allowances",nullable=false,precision=15,scale=2) private BigDecimal totalAllowances=BigDecimal.ZERO;
    @Column(name="total_deductions",nullable=false,precision=15,scale=2) private BigDecimal totalDeductions=BigDecimal.ZERO;
    @Column(name="net_salary",nullable=false,precision=15,scale=2) private BigDecimal netSalary;
    // Estado
    @Column(name="is_paid",nullable=false) private Boolean isPaid=false;
    @Column(name="paid_at") private LocalDateTime paidAt;
    @Column(name="pdf_url",length=500) private String pdfUrl;
    @Column(columnDefinition="TEXT") private String notes;
}
