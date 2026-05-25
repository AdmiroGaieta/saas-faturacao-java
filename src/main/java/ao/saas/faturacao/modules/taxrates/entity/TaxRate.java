package ao.saas.faturacao.modules.taxrates.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.modules.companies.entity.Company;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tax_rates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaxRate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
