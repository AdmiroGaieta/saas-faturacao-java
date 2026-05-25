package ao.saas.faturacao.modules.audit.entity;

import ao.saas.faturacao.common.enums.AuditAction;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "company_id", columnDefinition = "uuid")
    private UUID companyId;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "audit_action")
    private AuditAction action;

    @Column(nullable = false, length = 100)
    private String entity;

    @Column(name = "entity_id", columnDefinition = "uuid")
    private UUID entityId;

    @Type(type = "jsonb")
    @Column(name = "old_values", columnDefinition = "jsonb")
    private Map<String, Object> oldValues;

    @Type(type = "jsonb")
    @Column(name = "new_values", columnDefinition = "jsonb")
    private Map<String, Object> newValues;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
