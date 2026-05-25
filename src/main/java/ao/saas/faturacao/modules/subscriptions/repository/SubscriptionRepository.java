package ao.saas.faturacao.modules.subscriptions.repository;
import ao.saas.faturacao.modules.subscriptions.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByCompanyId(UUID companyId);
    @Modifying @Query("UPDATE Subscription s SET s.invoicesThisMonth = 0")
    void resetMonthlyCounters();
    @Modifying @Query("UPDATE Subscription s SET s.status = 'EXPIRED' WHERE s.status = 'TRIALING' AND s.trialEndsAt < CURRENT_TIMESTAMP")
    int expireTrials();
}
