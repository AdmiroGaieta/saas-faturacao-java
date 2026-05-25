package ao.saas.faturacao.modules.companies.repository;
import ao.saas.faturacao.modules.companies.entity.CompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface CompanyUserRepository extends JpaRepository<CompanyUser, UUID> {
    Optional<CompanyUser> findByUserIdAndCompanyId(UUID userId, UUID companyId);
    boolean existsByUserIdAndCompanyId(UUID userId, UUID companyId);
    List<CompanyUser> findByUserId(UUID userId);
    List<CompanyUser> findByCompanyId(UUID companyId);
}
