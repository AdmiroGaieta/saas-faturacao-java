package ao.saas.faturacao.modules.auth.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data @Builder public class AuthResponse {
    private UserDTO user;
    private List<CompanyInfo> companies;
    private String accessToken;
    private String refreshToken;
}