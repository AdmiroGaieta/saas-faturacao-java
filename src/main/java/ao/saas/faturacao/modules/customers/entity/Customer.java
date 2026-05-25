package ao.saas.faturacao.modules.customers.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.CustomerType;
import ao.saas.faturacao.modules.companies.entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnore
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "customer_type")
    private CustomerType type = CustomerType.COMPANY;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "trade_name", length = 255)
    private String tradeName;

    @Column(length = 20)
    private String nif;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String country = "Angola";

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 30)
    private String phone2;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(length = 5)
    private String currency = "AOA";

    @Column(name = "payment_terms", nullable = false)
    private Integer paymentTerms = 30;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public String getDisplayName() {
        if (type == CustomerType.COMPANY) {
            return companyName != null ? companyName : "";
        }
        String fn = firstName != null ? firstName : "";
        String ln = lastName  != null ? lastName  : "";
        return (fn + " " + ln).trim();
    }
}
