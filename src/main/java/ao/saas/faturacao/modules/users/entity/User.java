package ao.saas.faturacao.modules.users.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.UserRole;
import ao.saas.faturacao.common.enums.UserStatus;
import ao.saas.faturacao.modules.companies.entity.CompanyUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @Column(length = 500)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.EnumType")
    @Column(name = "role", columnDefinition = "user_role")
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.EnumType")
    @Column(name = "status", columnDefinition = "user_status")
    private UserStatus status;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @JsonIgnore
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CompanyUser> companyUsers = new ArrayList<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
