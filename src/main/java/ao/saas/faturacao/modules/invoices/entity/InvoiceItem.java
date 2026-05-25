package ao.saas.faturacao.modules.invoices.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.ProductUnit;
import ao.saas.faturacao.modules.products.entity.Product;
import ao.saas.faturacao.modules.taxrates.entity.TaxRate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_rate_id")
    private TaxRate taxRateEntity;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "product_unit")
    private ProductUnit unit = ProductUnit.UNIT;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPct = BigDecimal.ZERO;

    @Column(name = "discount_amt", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmt = BigDecimal.ZERO;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
