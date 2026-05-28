package ao.saas.faturacao.modules.auth.controller;

import ao.saas.faturacao.common.response.ApiResponse;
import ao.saas.faturacao.modules.auth.dto.AuthResponse;
import ao.saas.faturacao.modules.auth.dto.ChangePasswordRequest;
import ao.saas.faturacao.modules.auth.dto.LoginRequest;
import ao.saas.faturacao.modules.auth.dto.RefreshTokenRequest;
import ao.saas.faturacao.modules.auth.dto.RegisterRequest;
import ao.saas.faturacao.modules.auth.dto.UserDTO;
import ao.saas.faturacao.modules.auth.service.AuthService;
import ao.saas.faturacao.modules.users.entity.User;
import ao.saas.faturacao.modules.users.repository.UserRepository;
import ao.saas.faturacao.security.jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@Tag(name = "Auth", description = "Autenticação e autorização")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepo;

    @PostMapping("/register")
    @Operation(summary = "Registar novo utilizador")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(authService.register(dto)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login com email e password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest dto) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(dto)));
    }

    /*
     * @PostMapping("/refresh")
     * 
     * @Operation(summary = "Renovar access token")
     * public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody
     * RefreshTokenRequest dto) {
     * return
     * ResponseEntity.ok(ApiResponse.ok(authService.refresh(dto.getRefreshToken())))
     * ;
     * }
     */

    @PostMapping("/change-password")
    @Operation(summary = "Alterar password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest dto) {
        UUID userId = UUID.fromString(principal.getUsername());
        authService.changePassword(userId, dto);
        return ResponseEntity.ok(ApiResponse.ok("Password alterada com sucesso"));
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil do utilizador autenticado")
    public ResponseEntity<ApiResponse<UserDTO>> me(@AuthenticationPrincipal UserDetails principal) {
        UUID userId = UUID.fromString(principal.getUsername());
        User user = userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> ao.saas.faturacao.common.exceptions.BusinessException
                        .notFound("Utilizador não encontrado"));
        return ResponseEntity.ok(ApiResponse.ok(UserDTO.from(user)));
    }
}
