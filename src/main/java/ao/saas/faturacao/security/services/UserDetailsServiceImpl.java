package ao.saas.faturacao.security.services;

import ao.saas.faturacao.modules.users.entity.User;
import ao.saas.faturacao.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carrega por UUID (usado pelo JwtAuthFilter)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user;
        try {
            user = userRepository.findByIdAndDeletedAtIsNull(UUID.fromString(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + userId));
        } catch (IllegalArgumentException e) {
            // Se não é UUID, tenta por email
            user = userRepository.findByEmailAndDeletedAtIsNull(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + userId));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountExpired(false)
                .accountLocked(user.getStatus().name().equals("SUSPENDED"))
                .credentialsExpired(false)
                .disabled(user.getStatus().name().equals("INACTIVE"))
                .build();
    }
}
