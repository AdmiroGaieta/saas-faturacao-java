package ao.saas.faturacao.modules.crm.pipelines.entity;
import ao.saas.faturacao.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="crm_stages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmStage extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pipeline_id",nullable=false) @JsonIgnore
    private CrmPipeline pipeline;
    @Column(nullable=false,length=100) private String name;
    @Column(nullable=false,precision=5,scale=2) private BigDecimal probability=BigDecimal.ZERO;
    @Column(name="sort_order",nullable=false) private Integer sortOrder=0;
    @Column(length=7) private String color="#3b82f6";
    @Column(name="is_won",nullable=false) private Boolean isWon=false;
    @Column(name="is_lost",nullable=false) private Boolean isLost=false;
}
