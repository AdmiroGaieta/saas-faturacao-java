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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDateTime trialEndsAt;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;

    private LocalDateTime cancelledAt;

    private Integer maxUsers = 2;
    private Integer maxInvoices = 10;
    private Integer maxCustomers = 50;
    private Integer maxProducts = 100;

    private Integer invoicesThisMonth = 0;

    private String externalId;
}