package ao.saas.faturacao.modules.invoices.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.InvoiceStatus;
import ao.saas.faturacao.common.enums.InvoiceType;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.customers.entity.Customer;
import ao.saas.faturacao.modules.payments.entity.Payment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnore
    private Company company;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "invoice_type")
    private InvoiceType type = InvoiceType.INVOICE;

    @Column(nullable = false, length = 5)
    private String series;

    @Column(nullable = false)
    private Integer number;

    @Column(name = "full_number", nullable = false, unique = true, length = 50)
    private String fullNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "invoice_status")
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(nullable = false, length = 5)
    private String currency = "AOA";

    @Column(name = "exchange_rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "amount_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "amount_due", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountDue = BigDecimal.ZERO;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "purchase_order", length = 100)
    private String purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_invoice_id")
    private Invoice originalInvoice;

    @OneToMany(mappedBy = "originalInvoice", fetch = FetchType.LAZY)
    private List<Invoice> creditNotes = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "pdf_generated_at")
    private LocalDateTime pdfGeneratedAt;

    @Column(name = "agt_hash", length = 255)
    private String agtHash;

    @Column(name = "agt_qr_code", columnDefinition = "TEXT")
    private String agtQrCode;

    @Column(name = "agt_submitted_at")
    private LocalDateTime agtSubmittedAt;

    @Column(name = "agt_status", length = 50)
    private String agtStatus;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.EAGER,
               orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("paidAt DESC")
    private List<Payment> payments = new ArrayList<>();
}
