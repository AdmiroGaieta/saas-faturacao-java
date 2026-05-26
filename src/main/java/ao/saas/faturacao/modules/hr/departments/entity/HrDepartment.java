package ao.saas.faturacao.modules.hr.departments.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.modules.companies.entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.util.UUID;

@Entity @Table(name="hr_departments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HrDepartment extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_id")
    private HrDepartment parent;
    @Column(nullable=false,length=100) private String name;
    @Column(length=20) private String code;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="manager_id",columnDefinition="uuid") private UUID managerId;
    @Column(name="is_active",nullable=false) private Boolean isActive=true;
}
