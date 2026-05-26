package ao.saas.faturacao.modules.expenses.suppliers.repository;
import ao.saas.faturacao.modules.expenses.suppliers.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
public interface SupplierRepository extends JpaRepository<Supplier,UUID> {
    Optional<Supplier> findByIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID companyId);
    @Query("SELECT s FROM Supplier s WHERE s.company.id=:cid AND s.deletedAt IS NULL AND s.isActive=true AND (:q IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Supplier> search(@Param("cid") UUID companyId, @Param("q") String q, Pageable pageable);
}
