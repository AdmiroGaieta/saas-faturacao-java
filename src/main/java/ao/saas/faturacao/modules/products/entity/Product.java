package ao.saas.faturacao.modules.products.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.ProductType;
import ao.saas.faturacao.common.enums.ProductUnit;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.taxrates.entity.TaxRate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnore
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_rate_id")
    private TaxRate taxRate;

    @Column(length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "product_type")
    private ProductType type = ProductType.SERVICE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "product_unit")
    private ProductUnit unit = ProductUnit.UNIT;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(length = 5)
    private String currency = "AOA";

    @Column(name = "manage_stock", nullable = false)
    private Boolean manageStock = false;

    @Column(name = "stock_quantity", precision = 10, scale = 2)
    private BigDecimal stockQuantity;

    @Column(name = "min_stock", precision = 10, scale = 2)
    private BigDecimal minStock;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
