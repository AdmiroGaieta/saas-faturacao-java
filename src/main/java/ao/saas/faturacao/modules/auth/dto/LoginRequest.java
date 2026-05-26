package ao.saas.faturacao.modules.auth.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data public   class LoginRequest {
    @NotBlank @Email(message = "Email inválido")
    private String email;
    @NotBlank @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    private String password;
}