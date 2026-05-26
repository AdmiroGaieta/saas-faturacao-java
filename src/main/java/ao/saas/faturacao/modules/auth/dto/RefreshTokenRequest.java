package ao.saas.faturacao.modules.auth.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data public class RefreshTokenRequest {
    @NotBlank private String refreshToken;
}