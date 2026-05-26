package ao.saas.faturacao.modules.expenses.expenses.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.ExpenseStatus;
import ao.saas.faturacao.common.enums.PaymentMethod;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.expenses.categories.entity.ExpenseCategory;
import ao.saas.faturacao.modules.expenses.cost_centers.entity.CostCenter;
import ao.saas.faturacao.modules.expenses.suppliers.entity.Supplier;
import ao.saas.faturacao.modules.users.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="expenses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Expense extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="category_id")
    private ExpenseCategory category;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="cost_center_id")
    private CostCenter costCenter;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="supplier_id")
    private Supplier supplier;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="submitted_by",nullable=false)
    private User submittedBy;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="approved_by")
    private User approvedBy;
    @Column(nullable=false) private String title;
    @Column(columnDefinition="TEXT") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="expense_status")
    private ExpenseStatus status=ExpenseStatus.DRAFT;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal amount;
    @Column(name="tax_amount",nullable=false,precision=15,scale=2) private BigDecimal taxAmount=BigDecimal.ZERO;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal total;
    @Column(length=5) private String currency="AOA";
    @Column(name="expense_date",nullable=false) private LocalDate expenseDate;
    @Column(name="due_date") private LocalDate dueDate;
    @Column(name="paid_at") private LocalDateTime paidAt;
    @Enumerated(EnumType.STRING) @Column(name="payment_method",columnDefinition="payment_method")
    private PaymentMethod paymentMethod;
    @Column(length=100) private String reference;
    @Column(name="receipt_url",length=500) private String receiptUrl;
    @Column(columnDefinition="TEXT") private String notes;
    @Column(name="rejected_reason",columnDefinition="TEXT") private String rejectedReason;
}
