package ao.saas.faturacao.modules.crm.pipelines.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.modules.companies.entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="crm_pipelines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmPipeline extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @Column(nullable=false,length=100) private String name;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="is_default",nullable=false) private Boolean isDefault=false;
    @Column(name="is_active",nullable=false)  private Boolean isActive=true;
    @Column(name="sort_order",nullable=false)  private Integer sortOrder=0;
    @OneToMany(mappedBy="pipeline",cascade=CascadeType.ALL,fetch=FetchType.EAGER,orphanRemoval=true)
    @OrderBy("sortOrder ASC")
    private List<CrmStage> stages=new ArrayList<>();
}
