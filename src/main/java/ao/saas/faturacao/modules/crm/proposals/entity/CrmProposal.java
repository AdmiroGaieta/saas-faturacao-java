package ao.saas.faturacao.modules.crm.proposals.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.ProposalStatus;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.crm.leads.entity.CrmLead;
import ao.saas.faturacao.modules.customers.entity.Customer;
import ao.saas.faturacao.modules.invoices.entity.Invoice;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="crm_proposals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmProposal extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="lead_id")
    private CrmLead lead;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="customer_id",nullable=false)
    private Customer customer;
    @Column(nullable=false,unique=true,length=50) private String number;
    @Column(nullable=false) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="proposal_status")
    private ProposalStatus status=ProposalStatus.DRAFT;
    @Column(name="issue_date",nullable=false) private LocalDate issueDate;
    @Column(name="expiry_date") private LocalDate expiryDate;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal subtotal=BigDecimal.ZERO;
    @Column(name="tax_amount",nullable=false,precision=15,scale=2) private BigDecimal taxAmount=BigDecimal.ZERO;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal discount=BigDecimal.ZERO;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal total=BigDecimal.ZERO;
    @Column(length=5) private String currency="AOA";
    @Column(columnDefinition="TEXT") private String notes;
    @Column(columnDefinition="TEXT") private String terms;
    @Column(name="sent_at") private LocalDateTime sentAt;
    @Column(name="viewed_at") private LocalDateTime viewedAt;
    @Column(name="accepted_at") private LocalDateTime acceptedAt;
    @Column(name="rejected_at") private LocalDateTime rejectedAt;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="invoice_id")
    private Invoice invoice;
    @OneToMany(mappedBy="proposal",cascade=CascadeType.ALL,fetch=FetchType.EAGER,orphanRemoval=true)
    @OrderBy("sortOrder ASC")
    private List<CrmProposalItem> items=new ArrayList<>();
}
