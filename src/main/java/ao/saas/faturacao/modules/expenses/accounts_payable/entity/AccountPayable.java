package ao.saas.faturacao.modules.expenses.accounts_payable.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.PayableStatus;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.expenses.expenses.entity.Expense;
import ao.saas.faturacao.modules.expenses.suppliers.entity.Supplier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="accounts_payable")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountPayable extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="expense_id")
    private Expense expense;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="supplier_id")
    private Supplier supplier;
    @Column(nullable=false) private String description;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal amount;
    @Column(name="amount_paid",nullable=false,precision=15,scale=2) private BigDecimal amountPaid=BigDecimal.ZERO;
    @Column(name="amount_due",nullable=false,precision=15,scale=2) private BigDecimal amountDue;
    @Column(length=5) private String currency="AOA";
    @Column(name="issue_date",nullable=false) private LocalDate issueDate;
    @Column(name="due_date",nullable=false) private LocalDate dueDate;
    @Column(name="paid_at") private LocalDateTime paidAt;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="payable_status")
    private PayableStatus status=PayableStatus.PENDING;
    @Column(length=100) private String reference;
    @Column(columnDefinition="TEXT") private String notes;
}
