package ao.saas.faturacao.modules.expenses.categories.repository;
import ao.saas.faturacao.modules.expenses.categories.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory,UUID> {
    List<ExpenseCategory> findByCompanyIdAndIsActiveTrueOrderByNameAsc(UUID companyId);
}
