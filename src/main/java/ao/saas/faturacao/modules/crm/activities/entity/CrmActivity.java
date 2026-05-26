package ao.saas.faturacao.modules.crm.activities.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.ActivityStatus;
import ao.saas.faturacao.common.enums.ActivityType;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.crm.leads.entity.CrmLead;
import ao.saas.faturacao.modules.customers.entity.Customer;
import ao.saas.faturacao.modules.users.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="crm_activities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmActivity extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="lead_id")
    private CrmLead lead;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="customer_id")
    private Customer customer;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false)
    private User user;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="activity_type")
    private ActivityType type=ActivityType.TASK;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="activity_status")
    private ActivityStatus status=ActivityStatus.PENDING;
    @Column(nullable=false) private String title;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="scheduled_at") private LocalDateTime scheduledAt;
    @Column(name="completed_at") private LocalDateTime completedAt;
    @Column(name="duration_min") private Integer durationMin;
    @Column(columnDefinition="TEXT") private String outcome;
}
