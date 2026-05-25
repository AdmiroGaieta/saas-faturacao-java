package ao.saas.faturacao.modules.subscriptions.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.SubscriptionPlan;
import ao.saas.faturacao.common.enums.SubscriptionStatus;
import ao.saas.faturacao.modules.companies.entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    @JsonIgnore
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "subscription_plan")
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "subscription_status")
    private SubscriptionStatus status = SubscriptionStatus.TRIALING;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers = 2;

    @Column(name = "max_invoices", nullable = false)
    private Integer maxInvoices = 10;

    @Column(name = "max_customers", nullable = false)
    private Integer maxCustomers = 50;

    @Column(name = "max_products", nullable = false)
    private Integer maxProducts = 100;

    @Column(name = "invoices_this_month", nullable = false)
    private Integer invoicesThisMonth = 0;

    @Column(name = "external_id", length = 100)
    private String externalId;
}
