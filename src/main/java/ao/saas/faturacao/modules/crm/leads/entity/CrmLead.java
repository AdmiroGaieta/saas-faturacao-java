package ao.saas.faturacao.modules.crm.leads.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.LeadSource;
import ao.saas.faturacao.common.enums.LeadStatus;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.crm.pipelines.entity.CrmPipeline;
import ao.saas.faturacao.modules.crm.pipelines.entity.CrmStage;
import ao.saas.faturacao.modules.customers.entity.Customer;
import ao.saas.faturacao.modules.users.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Type;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="crm_leads")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmLead extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pipeline_id")
    private CrmPipeline pipeline;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="stage_id")
    private CrmStage stage;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="customer_id")
    private Customer customer;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assigned_to")
    private User assignedTo;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by")
    private User createdBy;
    @Column(nullable=false) private String title;
    @Column(columnDefinition="TEXT") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="lead_status")
    private LeadStatus status=LeadStatus.NEW;
    @Enumerated(EnumType.STRING) @Column(columnDefinition="lead_source")
    private LeadSource source=LeadSource.OTHER;
    @Column(precision=15,scale=2) private BigDecimal value;
    @Column(length=5) private String currency="AOA";
    @Column(precision=5,scale=2) private BigDecimal probability=BigDecimal.ZERO;
    @Column(name="expected_close") private LocalDate expectedClose;
    @Column(name="closed_at") private LocalDateTime closedAt;
    @Column(name="lost_reason",columnDefinition="TEXT") private String lostReason;
    @Column(name="contact_name",length=255) private String contactName;
    @Column(name="contact_email",length=255) private String contactEmail;
    @Column(name="contact_phone",length=30) private String contactPhone;
    @Column(name="contact_company",length=255) private String contactCompany;
    @Column(name="contact_nif",length=20) private String contactNif;
    @Column(columnDefinition="TEXT") private String notes;
}
