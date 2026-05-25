package ao.saas.faturacao.modules.invoices.repository;
import ao.saas.faturacao.common.enums.InvoiceStatus;
import ao.saas.faturacao.modules.invoices.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {
    Optional<Invoice> findByIdAndCompanyId(UUID id, UUID companyId);
    @Query("SELECT COALESCE(SUM(i.total),0) FROM Invoice i WHERE i.company.id=:cid AND i.status=:st AND i.issueDate>=:f AND i.issueDate<=:t")
    BigDecimal sumTotal(@Param("cid") UUID cid, @Param("st") InvoiceStatus st, @Param("f") LocalDate from, @Param("t") LocalDate to);
    @Query("SELECT COALESCE(SUM(i.amountDue),0) FROM Invoice i WHERE i.company.id=:cid AND i.status IN :statuses")
    BigDecimal sumAmountDue(@Param("cid") UUID cid, @Param("statuses") List<InvoiceStatus> statuses);
    long countByCompanyId(UUID companyId);
    long countByCompanyIdAndStatus(UUID companyId, InvoiceStatus status);
    @Modifying
    @Query("UPDATE Invoice i SET i.status = 'OVERDUE' WHERE i.status IN ('SENT','PARTIALLY_PAID') AND i.dueDate < CURRENT_DATE")
    int markOverdue();
}
