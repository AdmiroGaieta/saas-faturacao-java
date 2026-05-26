package ao.saas.faturacao.modules.crm.proposals.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.modules.products.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="crm_proposal_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CrmProposalItem extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="proposal_id",nullable=false) @JsonIgnore
    private CrmProposal proposal;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id")
    private Product product;
    @Column(nullable=false,length=500) private String description;
    @Column(nullable=false,precision=10,scale=2) private BigDecimal quantity;
    @Column(name="unit_price",nullable=false,precision=15,scale=2) private BigDecimal unitPrice;
    @Column(name="discount_pct",nullable=false,precision=5,scale=2) private BigDecimal discountPct=BigDecimal.ZERO;
    @Column(name="tax_rate",nullable=false,precision=5,scale=2) private BigDecimal taxRate=BigDecimal.ZERO;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal total;
    @Column(name="sort_order",nullable=false) private Integer sortOrder=0;
}
