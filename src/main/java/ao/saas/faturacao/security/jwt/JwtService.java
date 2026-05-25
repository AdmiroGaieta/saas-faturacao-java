package ao.saas.faturacao.security.jwt;

import ao.saas.faturacao.config.JwtConfig;
import ao.saas.faturacao.modules.users.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

    // ── Geração ────────────────────────────────────────────────

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role",  user.getRole().name());
        return buildToken(claims, user.getId().toString(), jwtConfig.getExpirationMs());
    }

    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user.getId().toString(), jwtConfig.getRefreshExpirationMs());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Validação ──────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String subject = extractSubject(token);
            return subject.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.warn("JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Extracção ──────────────────────────────────────────────

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractSubject(token));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
