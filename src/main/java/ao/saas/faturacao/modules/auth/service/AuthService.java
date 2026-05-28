package ao.saas.faturacao.modules.auth.service;

import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.auth.dto.AuthResponse;
import ao.saas.faturacao.modules.auth.dto.ChangePasswordRequest;
import ao.saas.faturacao.modules.auth.dto.CompanyInfo;
import ao.saas.faturacao.modules.auth.dto.LoginRequest;
import ao.saas.faturacao.modules.auth.dto.RegisterRequest;
import ao.saas.faturacao.modules.auth.dto.UserDTO;
import ao.saas.faturacao.modules.companies.entity.CompanyUser;
import ao.saas.faturacao.modules.companies.repository.CompanyUserRepository;
import ao.saas.faturacao.modules.users.entity.User;
import ao.saas.faturacao.modules.users.repository.UserRepository;
import ao.saas.faturacao.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ao.saas.faturacao.common.enums.UserRole.ADMIN;
import static ao.saas.faturacao.common.enums.UserStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final CompanyUserRepository companyUserRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    // ── Registo ───────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest dto) {
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw BusinessException.conflict("Email já registado");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .role(ADMIN)
                .status(ACTIVE)
                .emailVerifiedAt(LocalDateTime.now())
                .build();
        userRepo.save(user);

        return buildAuthResponse(user, List.of());
    }

    // ── Login ─────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest dto) {

        User user = userRepo.findByEmailAndDeletedAtIsNull(dto.getEmail())
                .orElseThrow(() -> new BusinessException("Utilizador não encontrado", HttpStatus.NOT_FOUND));

        log.info("Login tentativa para email: {}", dto.getEmail());
        log.info("Hash na BD: {}", user.getPassword());

        boolean passwordMatches = passwordEncoder.matches(dto.getPassword(), user.getPassword());

        log.info("Password bate? {}", passwordMatches);

        if (!passwordMatches) {
            log.warn("Password inválida para email: {}", dto.getEmail());
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Email ou password incorrectos");
        }

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        List<CompanyUser> companyUsers = companyUserRepo.findByUserId(user.getId());

        List<CompanyInfo> companies = companyUsers.stream()
                .map(cu -> CompanyInfo.builder()
                        .id(cu.getCompany().getId())
                        .name(cu.getCompany().getName())
                        .role(cu.getRole())
                        .isDefault(cu.getIsDefault())
                        .build())
                .collect(Collectors.toList());

        return buildAuthResponse(user, companies);
    }
    // ── Change Password ───────────────────────────────────────

    @Transactional
    public void changePassword(java.util.UUID userId, ChangePasswordRequest dto) {
        User user = userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilizador não encontrado"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw BusinessException.badRequest("Password actual incorrecta");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepo.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, List<CompanyInfo> companies) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .user(UserDTO.from(user))
                .companies(companies)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
