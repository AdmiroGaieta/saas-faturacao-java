package ao.saas.faturacao.modules.auth.dto;
import ao.saas.faturacao.common.enums.UserRole;
import lombok.*;

import java.util.UUID;

// ── Request DTOs ──────────────────────────────────────────────────



// ── Response DTOs ─────────────────────────────────────────────────

@Data @Builder public class CompanyInfo {
    private UUID id;
    private String name;
    private UserRole role;
    private boolean isDefault;
}
