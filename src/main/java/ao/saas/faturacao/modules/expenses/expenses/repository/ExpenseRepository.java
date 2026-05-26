package ao.saas.faturacao.modules.expenses.expenses.repository;
import ao.saas.faturacao.common.enums.ExpenseStatus;
import ao.saas.faturacao.modules.expenses.expenses.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
public interface ExpenseRepository extends JpaRepository<Expense,UUID>, JpaSpecificationExecutor<Expense> {
    Optional<Expense> findByIdAndCompanyId(UUID id, UUID companyId);
    @Query("SELECT COALESCE(SUM(e.total),0) FROM Expense e WHERE e.company.id=:cid AND e.status=:st AND e.expenseDate>=:f AND e.expenseDate<=:t")
    BigDecimal sumByStatus(@Param("cid") UUID cid, @Param("st") ExpenseStatus st, @Param("f") LocalDate from, @Param("t") LocalDate to);
}
