package ao.saas.faturacao.modules.expenses.accounts_payable.repository;
import ao.saas.faturacao.common.enums.PayableStatus;
import ao.saas.faturacao.modules.expenses.accounts_payable.entity.AccountPayable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;
public interface AccountPayableRepository extends JpaRepository<AccountPayable,UUID> {
    Page<AccountPayable> findByCompanyIdOrderByDueDateAsc(UUID companyId, Pageable pageable);
    long countByCompanyIdAndStatus(UUID companyId, PayableStatus status);
    @Modifying @Query("UPDATE AccountPayable a SET a.status='OVERDUE' WHERE a.status='PENDING' AND a.dueDate < CURRENT_DATE")
    int markOverdue();
}
