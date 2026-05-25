package ao.saas.faturacao.modules.companies.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.UserRole;
import ao.saas.faturacao.modules.users.entity.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "company_users",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "company_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanyUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "user_role")
    private UserRole role = UserRole.VIEWER;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
