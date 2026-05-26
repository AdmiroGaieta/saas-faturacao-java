package ao.saas.faturacao.modules.expenses.cost_centers.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.modules.companies.entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;

@Entity @Table(name="cost_centers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CostCenter extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @Column(nullable=false,length=100) private String name;
    @Column(length=20) private String code;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="is_active",nullable=false) private Boolean isActive=true;
}
