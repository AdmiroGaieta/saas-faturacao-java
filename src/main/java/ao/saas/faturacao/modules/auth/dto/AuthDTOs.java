package ao.saas.faturacao.modules.auth.dto;

import ao.saas.faturacao.common.enums.UserRole;
import ao.saas.faturacao.modules.users.entity.User;
import lombok.*;
import javax.validation.constraints.*;
import java.util.List;
import java.util.UUID;

// ── Request DTOs ──────────────────────────────────────────────────

@Data public static class LoginRequest {
    @NotBlank @Email(message = "Email inválido")
    private String email;
    @NotBlank @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    private String password;
}

@Data public static class RegisterRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, message = "Password deve ter pelo menos 8 caracteres")
    private String password;
    private String phone;
}

@Data public static class RefreshTokenRequest {
    @NotBlank private String refreshToken;
}

@Data public static class ChangePasswordRequest {
    @NotBlank private String currentPassword;
    @NotBlank @Size(min = 8) private String newPassword;
}

// ── Response DTOs ─────────────────────────────────────────────────

@Data @Builder public static class AuthResponse {
    private UserDTO user;
    private List<CompanyInfo> companies;
    private String accessToken;
    private String refreshToken;
}

@Data @Builder public static class UserDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private String status;

    public static UserDTO from(User u) {
        return UserDTO.builder()
            .id(u.getId()).email(u.getEmail())
            .firstName(u.getFirstName()).lastName(u.getLastName())
            .phone(u.getPhone()).role(u.getRole()).status(u.getStatus().name())
            .build();
    }
}

@Data @Builder public static class CompanyInfo {
    private UUID id;
    private String name;
    private UserRole role;
    private boolean isDefault;
}
