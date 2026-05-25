package ao.saas.faturacao.modules.customers.repository;
import ao.saas.faturacao.modules.customers.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID companyId);
    @Query("SELECT c FROM Customer c WHERE c.company.id = :cid AND c.deletedAt IS NULL " +
           "AND (:s IS NULL OR LOWER(COALESCE(c.companyName,'')) LIKE LOWER(CONCAT('%',:s,'%')) " +
           "OR LOWER(COALESCE(c.firstName,'')) LIKE LOWER(CONCAT('%',:s,'%')) " +
           "OR LOWER(COALESCE(c.nif,'')) LIKE LOWER(CONCAT('%',:s,'%')))")
    Page<Customer> searchByCompany(@Param("cid") UUID companyId, @Param("s") String search, Pageable pageable);
    long countByCompanyIdAndDeletedAtIsNull(UUID companyId);
}
