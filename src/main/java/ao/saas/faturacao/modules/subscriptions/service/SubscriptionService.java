package ao.saas.faturacao.modules.subscriptions.service;

import ao.saas.faturacao.modules.subscriptions.entity.Subscription;
import ao.saas.faturacao.modules.subscriptions.repository.SubscriptionRepository;
import ao.saas.faturacao.common.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository repo;

    public Subscription getByCompany(UUID companyId) {
        return repo.findByCompanyId(companyId)
                .orElseThrow(() -> BusinessException.notFound("Subscrição não encontrada"));
    }

    @Scheduled(cron = "0 0 0 1 * ?") // 1º dia de cada mês, meia-noite
    @Transactional
    public void resetMonthlyCounters() {
        repo.resetMonthlyCounters();
    }

    @Scheduled(cron = "0 0 2 * * ?") // diariamente às 02:00
    @Transactional
    public void expireTrials() {
        int expired = repo.expireTrials();
        if (expired > 0) {
            org.slf4j.LoggerFactory.getLogger(getClass()).info("Trials expirados: {}", expired);
        }
    }
}
