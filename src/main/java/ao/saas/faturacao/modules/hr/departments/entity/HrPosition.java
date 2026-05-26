package ao.saas.faturacao.modules.hr.departments.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.modules.companies.entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="hr_positions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HrPosition extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="department_id")
    private HrDepartment department;
    @Column(nullable=false,length=100) private String name;
    @Column(length=20) private String code;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="min_salary",precision=15,scale=2) private BigDecimal minSalary;
    @Column(name="max_salary",precision=15,scale=2) private BigDecimal maxSalary;
    @Column(name="is_active",nullable=false) private Boolean isActive=true;
}
