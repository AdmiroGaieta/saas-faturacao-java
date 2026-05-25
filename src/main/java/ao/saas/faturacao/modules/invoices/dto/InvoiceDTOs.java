package ao.saas.faturacao.modules.invoices.dto;

import ao.saas.faturacao.common.enums.*;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class InvoiceDTOs {

    @Data
    public static class InvoiceFilterRequest {
        private InvoiceStatus status;
        private InvoiceType   type;
        private UUID          customerId;
        private LocalDate     dateFrom;
        private LocalDate     dateTo;
        private String        search;
        private Integer       page  = 1;
        private Integer       limit = 20;
    }

    @Data
    public static class CreateInvoiceRequest {
        @NotNull(message = "Cliente obrigatório")
        private UUID          customerId;
        private InvoiceType   type;
        private LocalDate     issueDate;
        private LocalDate     dueDate;
        private String        currency;
        private String        referenceNumber;
        private String        purchaseOrder;
        private String        notes;
        private String        termsConditions;
        private boolean       draft = false;

        @NotEmpty(message = "A factura deve ter pelo menos uma linha")
        @Valid
        private List<ItemRequest> items;
    }

    @Data
    public static class ItemRequest {
        private UUID          productId;
        private UUID          taxRateId;

        @Size(max = 500)
        private String        description;

        @NotNull @DecimalMin("0.01")
        private BigDecimal    quantity;

        private ProductUnit   unit;

        @DecimalMin("0")
        private BigDecimal    unitPrice;

        @DecimalMin("0") @DecimalMax("100")
        private BigDecimal    discountPct;

        // campo interno (não vem do frontend directamente)
        BigDecimal taxRateOverride;
    }

    @Data
    public static class PaymentRequest {
        @NotNull @DecimalMin("0.01")
        private BigDecimal    amount;
        private PaymentMethod method;
        private LocalDateTime paidAt;
        private String        reference;
        private String        notes;
    }

    @Data
    public static class CancelRequest {
        private String reason;
    }
}
