package ao.saas.faturacao.modules.expenses.categories.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.ExpenseCategoryType;
import ao.saas.faturacao.modules.companies.entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;

@Entity @Table(name="expense_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExpenseCategory extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_id")
    private ExpenseCategory parent;
    @Column(nullable=false,length=100) private String name;
    @Column(length=20) private String code;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="expense_category_type")
    private ExpenseCategoryType type=ExpenseCategoryType.OPERATIONAL;
    @Column(name="is_active",nullable=false) private Boolean isActive=true;
}
