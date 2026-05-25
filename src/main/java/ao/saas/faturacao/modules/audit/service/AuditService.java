package ao.saas.faturacao.modules.audit.service;

import ao.saas.faturacao.common.enums.AuditAction;
import ao.saas.faturacao.modules.audit.entity.AuditLog;
import ao.saas.faturacao.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repo;

    @Async
    public void log(UUID companyId, UUID userId, AuditAction action, String entity, UUID entityId) {
        try {
            repo.save(AuditLog.builder()
                    .companyId(companyId).userId(userId)
                    .action(action).entity(entity).entityId(entityId)
                    .build());
        } catch (Exception e) {
            log.warn("Erro ao registar auditoria: {}", e.getMessage());
        }
    }
}
