package ao.saas.faturacao.modules.companies.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.*;
import ao.saas.faturacao.modules.subscriptions.entity.Subscription;
import ao.saas.faturacao.modules.taxrates.entity.TaxRate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Company extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(nullable = false, unique = true, length = 20)
    private String nif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "company_type")
    private CompanyType type = CompanyType.LDA;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_regime", nullable = false, columnDefinition = "tax_regime")
    private TaxRegime taxRegime = TaxRegime.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "company_status")
    private CompanyStatus status = CompanyStatus.TRIAL;

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

    @Column(length = 30)
    private String phone;

    @Column(length = 30)
    private String phone2;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String website;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account", length = 100)
    private String bankAccount;

    @Column(name = "bank_iban", length = 50)
    private String bankIban;

    @Column(name = "invoice_prefix", nullable = false, length = 5)
    private String invoicePrefix = "FT";

    @Column(name = "invoice_next_number", nullable = false)
    private Integer invoiceNextNumber = 1;

    @Column(name = "invoice_series", nullable = false, length = 5)
    private String invoiceSeries = "A";

    @Column(name = "default_due_days", nullable = false)
    private Integer defaultDueDays = 30;

    @Column(name = "default_currency", nullable = false, length = 5)
    private String defaultCurrency = "AOA";

    @Column(name = "default_notes", columnDefinition = "TEXT")
    private String defaultNotes;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    @Column(length = 500)
    private String logo;

    @Column(name = "agt_credentials", columnDefinition = "TEXT")
    @JsonIgnore
    private String agtCredentials;

    @Column(name = "agt_registered", nullable = false)
    private Boolean agtRegistered = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ── Relações ──────────────────────────────────────────────

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CompanyUser> companyUsers = new ArrayList<>();

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Subscription subscription;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TaxRate> taxRates = new ArrayList<>();
}
