package ao.saas.faturacao.modules.expenses.suppliers.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.modules.companies.entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="suppliers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Supplier extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @Column(nullable=false) private String name;
    @Column(name="trade_name") private String tradeName;
    @Column(length=20) private String nif;
    @Column(length=255) private String email;
    @Column(length=30) private String phone;
    @Column(length=500) private String address;
    @Column(length=100) private String city;
    @Column(length=100) private String province;
    @Column(name="bank_name",length=100) private String bankName;
    @Column(name="bank_account",length=100) private String bankAccount;
    @Column(name="bank_iban",length=50) private String bankIban;
    @Column(name="payment_terms",nullable=false) private Integer paymentTerms=30;
    @Column(name="credit_limit",precision=15,scale=2) private BigDecimal creditLimit;
    @Column(columnDefinition="TEXT") private String notes;
    @Column(name="is_active",nullable=false) private Boolean isActive=true;
    @Column(name="deleted_at") private LocalDateTime deletedAt;
}
