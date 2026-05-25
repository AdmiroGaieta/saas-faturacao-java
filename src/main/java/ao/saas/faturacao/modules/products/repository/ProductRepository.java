package ao.saas.faturacao.modules.products.repository;
import ao.saas.faturacao.modules.products.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID companyId);
    @Query("SELECT p FROM Product p WHERE p.company.id = :cid AND p.deletedAt IS NULL AND p.isActive = true " +
           "AND (:s IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:s,'%')))")
    Page<Product> searchByCompany(@Param("cid") UUID companyId, @Param("s") String search, Pageable pageable);
    long countByCompanyIdAndDeletedAtIsNull(UUID companyId);
}
