package ao.saas.faturacao.modules.hr.payroll.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.PayrollStatus;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.hr.payslips.entity.HrPayslip;
import ao.saas.faturacao.modules.users.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="hr_payrolls",uniqueConstraints=@UniqueConstraint(columnNames={"company_id","period_year","period_month"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HrPayroll extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @Column(name="period_year",nullable=false) private Integer periodYear;
    @Column(name="period_month",nullable=false) private Integer periodMonth;
    @Column private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="payroll_status")
    private PayrollStatus status=PayrollStatus.DRAFT;
    @Column(name="total_gross",nullable=false,precision=15,scale=2) private BigDecimal totalGross=BigDecimal.ZERO;
    @Column(name="total_inss_emp",nullable=false,precision=15,scale=2) private BigDecimal totalInssEmployee=BigDecimal.ZERO;
    @Column(name="total_inss_emp2",nullable=false,precision=15,scale=2) private BigDecimal totalInssEmployer=BigDecimal.ZERO;
    @Column(name="total_irt",nullable=false,precision=15,scale=2) private BigDecimal totalIrt=BigDecimal.ZERO;
    @Column(name="total_deductions",nullable=false,precision=15,scale=2) private BigDecimal totalDeductions=BigDecimal.ZERO;
    @Column(name="total_net",nullable=false,precision=15,scale=2) private BigDecimal totalNet=BigDecimal.ZERO;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="approved_by") private User approvedBy;
    @Column(name="approved_at") private LocalDateTime approvedAt;
    @Column(name="paid_at") private LocalDateTime paidAt;
    @Column(columnDefinition="TEXT") private String notes;
    @OneToMany(mappedBy="payroll",cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    @JsonIgnore private List<HrPayslip> payslips=new ArrayList<>();
    public String getPeriodLabel() {
        String[] months={"Janeiro","Fevereiro","Março","Abril","Maio","Junho",
            "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro","13º Mês"};
        return months[periodMonth-1] + " " + periodYear;
    }
}
