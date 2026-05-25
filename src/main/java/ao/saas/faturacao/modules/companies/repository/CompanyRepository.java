package ao.saas.faturacao.modules.companies.repository;
import ao.saas.faturacao.modules.companies.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByNif(String nif);
    boolean existsByNifAndIdNot(String nif, UUID id);
    Optional<Company> findByIdAndDeletedAtIsNull(UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Company c WHERE c.id = :id")
    Optional<Company> findByIdForUpdate(UUID id);
}
