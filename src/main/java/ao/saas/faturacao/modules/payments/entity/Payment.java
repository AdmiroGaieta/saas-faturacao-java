package ao.saas.faturacao.modules.payments.entity;

import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.PaymentMethod;
import ao.saas.faturacao.modules.invoices.entity.Invoice;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 5)
    private String currency = "AOA";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "payment_method")
    private PaymentMethod method = PaymentMethod.BANK_TRANSFER;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(length = 100)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
