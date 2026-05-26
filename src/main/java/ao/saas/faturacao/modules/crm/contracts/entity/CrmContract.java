package ao.saas.faturacao.modules.crm.contracts.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.CrmContractStatus;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.crm.leads.entity.CrmLead;
import ao.saas.faturacao.modules.customers.entity.Customer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="crm_contracts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmContract extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="customer_id",nullable=false)
    private Customer customer;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="lead_id")
    private CrmLead lead;
    @Column(nullable=false,unique=true,length=50) private String number;
    @Column(nullable=false) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="contract_status")
    private CrmContractStatus status=CrmContractStatus.DRAFT;
    @Column(name="start_date",nullable=false) private LocalDate startDate;
    @Column(name="end_date") private LocalDate endDate;
    @Column(name="renewal_date") private LocalDate renewalDate;
    @Column(precision=15,scale=2) private BigDecimal value;
    @Column(length=5) private String currency="AOA";
    @Column(name="billing_cycle",length=20) private String billingCycle;
    @Column(name="auto_renew",nullable=false) private Boolean autoRenew=false;
    @Column(columnDefinition="TEXT") private String description;
    @Column(columnDefinition="TEXT") private String terms;
    @Column(name="signed_at") private LocalDateTime signedAt;
    @Column(name="terminated_at") private LocalDateTime terminatedAt;
    @Column(name="termination_reason",columnDefinition="TEXT") private String terminationReason;
}
