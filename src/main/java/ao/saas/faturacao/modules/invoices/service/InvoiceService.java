package ao.saas.faturacao.modules.invoices.service;

import ao.saas.faturacao.common.enums.AuditAction;
import ao.saas.faturacao.common.enums.InvoiceStatus;
import ao.saas.faturacao.common.enums.InvoiceType;
import ao.saas.faturacao.common.enums.PaymentMethod;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.audit.service.AuditService;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.customers.entity.Customer;
import ao.saas.faturacao.modules.customers.repository.CustomerRepository;
import ao.saas.faturacao.modules.invoices.dto.InvoiceDTOs.*;
import ao.saas.faturacao.modules.invoices.entity.Invoice;
import ao.saas.faturacao.modules.invoices.entity.InvoiceItem;
import ao.saas.faturacao.modules.invoices.repository.InvoiceRepository;
import ao.saas.faturacao.modules.payments.entity.Payment;
import ao.saas.faturacao.modules.products.entity.Product;
import ao.saas.faturacao.modules.products.repository.ProductRepository;
import ao.saas.faturacao.modules.subscriptions.repository.SubscriptionRepository;
import ao.saas.faturacao.modules.taxrates.entity.TaxRate;
import ao.saas.faturacao.modules.taxrates.repository.TaxRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository    invoiceRepo;
    private final CompanyRepository    companyRepo;
    private final CustomerRepository   customerRepo;
    private final ProductRepository    productRepo;
    private final TaxRateRepository    taxRateRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final AuditService         auditService;

    // ── Listar ─────────────────────────────────────────────────────

    public Page<Invoice> list(UUID companyId, InvoiceFilterRequest filter) {
        Specification<Invoice> spec = Specification.where(
            (root, q, cb) -> cb.equal(root.get("company").get("id"), companyId));

        if (filter.getStatus() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), filter.getStatus()));
        }
        if (filter.getType() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("type"), filter.getType()));
        }
        if (filter.getCustomerId() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("customer").get("id"), filter.getCustomerId()));
        }
        if (filter.getDateFrom() != null) {
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("issueDate"), filter.getDateFrom()));
        }
        if (filter.getDateTo() != null) {
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("issueDate"), filter.getDateTo()));
        }
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String like = "%" + filter.getSearch().toLowerCase() + "%";
            spec = spec.and((r, q, cb) -> cb.or(
                cb.like(cb.lower(r.get("fullNumber")), like),
                cb.like(cb.lower(r.get("customer").get("companyName")), like),
                cb.like(cb.lower(r.get("customer").get("firstName")), like)
            ));
        }

        int page  = filter.getPage()  != null ? filter.getPage()  - 1 : 0;
        int limit = filter.getLimit() != null ? filter.getLimit()     : 20;
        Sort sort = Sort.by(Sort.Direction.DESC, "issueDate");

        return invoiceRepo.findAll(spec, PageRequest.of(page, limit, sort));
    }

    // ── Obter uma ──────────────────────────────────────────────────

    public Invoice getOne(UUID companyId, UUID invoiceId) {
        return invoiceRepo.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> BusinessException.notFound("Factura não encontrada"));
    }

    // ── Criar ──────────────────────────────────────────────────────

    @Transactional
    public Invoice create(UUID companyId, UUID userId, CreateInvoiceRequest dto) {
        Company company = companyRepo.findByIdForUpdate(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));

        // Verificar limite de subscrição
        subscriptionRepo.findByCompanyId(companyId).ifPresent(sub -> {
            if (sub.getMaxInvoices() > 0 && sub.getInvoicesThisMonth() >= sub.getMaxInvoices()) {
                throw BusinessException.forbidden(
                    "Limite de facturas atingido para este mês (" + sub.getMaxInvoices() + "). Faça upgrade do plano.");
            }
        });

        Customer customer = customerRepo.findByIdAndCompanyIdAndDeletedAtIsNull(dto.getCustomerId(), companyId)
                .orElseThrow(() -> BusinessException.notFound("Cliente não encontrado"));

        // Gerar número sequencial (bloqueio pessimista)
        String series     = company.getInvoiceSeries();
        int    nextNumber = company.getInvoiceNextNumber();
        InvoiceType type  = dto.getType() != null ? dto.getType() : InvoiceType.INVOICE;
        String fullNumber = String.format("%s %d/%s/%06d",
                type.getPrefix(), LocalDate.now().getYear(), series, nextNumber);

        company.setInvoiceNextNumber(nextNumber + 1);
        companyRepo.save(company);

        // Construir a factura
        Invoice invoice = Invoice.builder()
                .company(company)
                .customer(customer)
                .type(type)
                .series(series)
                .number(nextNumber)
                .fullNumber(fullNumber)
                .status(dto.isDraft() ? InvoiceStatus.DRAFT : InvoiceStatus.PENDING)
                .issueDate(dto.getIssueDate() != null ? dto.getIssueDate() : LocalDate.now())
                .dueDate(dto.getDueDate() != null ? dto.getDueDate()
                        : LocalDate.now().plusDays(company.getDefaultDueDays()))
                .currency(dto.getCurrency() != null ? dto.getCurrency() : company.getDefaultCurrency())
                .referenceNumber(dto.getReferenceNumber())
                .purchaseOrder(dto.getPurchaseOrder())
                .notes(dto.getNotes() != null ? dto.getNotes() : company.getDefaultNotes())
                .termsConditions(dto.getTermsConditions() != null
                        ? dto.getTermsConditions() : company.getTermsConditions())
                .items(new ArrayList<>())
                .build();

        // Adicionar linhas
        int order = 0;
        for (ItemRequest itemDto : dto.getItems()) {
            InvoiceItem item = buildItem(invoice, itemDto, companyId, order++);
            invoice.getItems().add(item);
        }

        recalcTotals(invoice);

        Invoice saved = invoiceRepo.save(invoice);

        // Incrementar contador de subscrição
        subscriptionRepo.findByCompanyId(companyId).ifPresent(sub -> {
            sub.setInvoicesThisMonth(sub.getInvoicesThisMonth() + 1);
            subscriptionRepo.save(sub);
        });

        auditService.log(companyId, userId, AuditAction.CREATE, "Invoice", saved.getId());
        log.info("Factura criada: {} | Empresa: {}", fullNumber, companyId);

        return saved;
    }

    // ── Actualizar (apenas DRAFT) ──────────────────────────────────

    @Transactional
    public Invoice update(UUID companyId, UUID invoiceId, UUID userId, CreateInvoiceRequest dto) {
        Invoice invoice = getOne(companyId, invoiceId);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw BusinessException.badRequest("Só é possível editar facturas em Rascunho");
        }

        if (dto.getCustomerId() != null) {
            Customer customer = customerRepo.findByIdAndCompanyIdAndDeletedAtIsNull(dto.getCustomerId(), companyId)
                    .orElseThrow(() -> BusinessException.notFound("Cliente não encontrado"));
            invoice.setCustomer(customer);
        }

        if (dto.getIssueDate()       != null) invoice.setIssueDate(dto.getIssueDate());
        if (dto.getDueDate()         != null) invoice.setDueDate(dto.getDueDate());
        if (dto.getCurrency()        != null) invoice.setCurrency(dto.getCurrency());
        if (dto.getReferenceNumber() != null) invoice.setReferenceNumber(dto.getReferenceNumber());
        if (dto.getPurchaseOrder()   != null) invoice.setPurchaseOrder(dto.getPurchaseOrder());
        if (dto.getNotes()           != null) invoice.setNotes(dto.getNotes());
        if (dto.getTermsConditions() != null) invoice.setTermsConditions(dto.getTermsConditions());

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            invoice.getItems().clear();
            int order = 0;
            for (ItemRequest itemDto : dto.getItems()) {
                invoice.getItems().add(buildItem(invoice, itemDto, companyId, order++));
            }
        }

        recalcTotals(invoice);
        auditService.log(companyId, userId, AuditAction.UPDATE, "Invoice", invoiceId);
        return invoiceRepo.save(invoice);
    }

    // ── Finalizar (DRAFT → PENDING) ────────────────────────────────

    @Transactional
    public Invoice finalize(UUID companyId, UUID invoiceId, UUID userId) {
        Invoice invoice = getOne(companyId, invoiceId);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw BusinessException.badRequest("Factura já foi emitida");
        }
        if (invoice.getItems().isEmpty()) {
            throw BusinessException.badRequest("A factura não tem linhas");
        }
        invoice.setStatus(InvoiceStatus.PENDING);
        auditService.log(companyId, userId, AuditAction.UPDATE, "Invoice", invoiceId);
        return invoiceRepo.save(invoice);
    }

    // ── Marcar como Enviada ────────────────────────────────────────

    @Transactional
    public Invoice send(UUID companyId, UUID invoiceId, UUID userId) {
        Invoice invoice = getOne(companyId, invoiceId);
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.PENDING);
        }
        invoice.setSentAt(LocalDateTime.now());
        if (invoice.getStatus() == InvoiceStatus.PENDING) {
            invoice.setStatus(InvoiceStatus.SENT);
        }
        auditService.log(companyId, userId, AuditAction.SEND, "Invoice", invoiceId);
        return invoiceRepo.save(invoice);
    }

    // ── Registar Pagamento ─────────────────────────────────────────

    @Transactional
    public Invoice registerPayment(UUID companyId, UUID invoiceId, UUID userId, PaymentRequest dto) {
        Invoice invoice = getOne(companyId, invoiceId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw BusinessException.badRequest("Factura já se encontra paga na totalidade");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw BusinessException.badRequest("Factura anulada — não é possível registar pagamento");
        }
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("O valor do pagamento deve ser positivo");
        }
        if (dto.getAmount().compareTo(invoice.getAmountDue()) > 0) {
            throw BusinessException.badRequest(
                "O valor do pagamento excede o valor em dívida: " + invoice.getAmountDue());
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(dto.getAmount())
                .method(dto.getMethod() != null ? dto.getMethod() : PaymentMethod.BANK_TRANSFER)
                .paidAt(dto.getPaidAt() != null ? dto.getPaidAt() : LocalDateTime.now())
                .reference(dto.getReference())
                .notes(dto.getNotes())
                .currency(invoice.getCurrency())
                .build();

        invoice.getPayments().add(payment);
        invoice.setAmountPaid(invoice.getAmountPaid().add(dto.getAmount()));
        invoice.setAmountDue(invoice.getTotal().subtract(invoice.getAmountPaid()));

        if (invoice.getAmountDue().compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoice.setAmountDue(BigDecimal.ZERO);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        auditService.log(companyId, userId, AuditAction.PAY, "Invoice", invoiceId);
        return invoiceRepo.save(invoice);
    }

    // ── Anular ─────────────────────────────────────────────────────

    @Transactional
    public Invoice cancel(UUID companyId, UUID invoiceId, UUID userId, String reason) {
        Invoice invoice = getOne(companyId, invoiceId);

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw BusinessException.badRequest("Factura já foi anulada");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw BusinessException.badRequest("Não é possível anular uma factura paga. Crie uma Nota de Crédito.");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setCancelledAt(LocalDateTime.now());
        if (reason != null) {
            invoice.setNotes((invoice.getNotes() != null ? invoice.getNotes() + "\n" : "")
                    + "Motivo de anulação: " + reason);
        }

        auditService.log(companyId, userId, AuditAction.CANCEL, "Invoice", invoiceId);
        return invoiceRepo.save(invoice);
    }

    // ── Nota de Crédito ────────────────────────────────────────────

    @Transactional
    public Invoice createCreditNote(UUID companyId, UUID invoiceId, UUID userId) {
        Invoice original = getOne(companyId, invoiceId);
        Company company  = companyRepo.findByIdForUpdate(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));

        int    nextNumber = company.getInvoiceNextNumber();
        String fullNumber = String.format("NC %d/%s/%06d",
                LocalDate.now().getYear(), company.getInvoiceSeries(), nextNumber);
        company.setInvoiceNextNumber(nextNumber + 1);
        companyRepo.save(company);

        // Copiar linhas com valores negativos
        Invoice creditNote = Invoice.builder()
                .company(company)
                .customer(original.getCustomer())
                .type(InvoiceType.CREDIT_NOTE)
                .series(company.getInvoiceSeries())
                .number(nextNumber)
                .fullNumber(fullNumber)
                .status(InvoiceStatus.PENDING)
                .issueDate(LocalDate.now())
                .currency(original.getCurrency())
                .originalInvoice(original)
                .notes("Nota de Crédito referente a " + original.getFullNumber())
                .items(new ArrayList<>())
                .build();

        int order = 0;
        for (InvoiceItem origItem : original.getItems()) {
            InvoiceItem creditItem = InvoiceItem.builder()
                    .invoice(creditNote)
                    .product(origItem.getProduct())
                    .description(origItem.getDescription())
                    .quantity(origItem.getQuantity().negate())
                    .unit(origItem.getUnit())
                    .unitPrice(origItem.getUnitPrice())
                    .taxRate(origItem.getTaxRate())
                    .taxAmount(origItem.getTaxAmount().negate())
                    .subtotal(origItem.getSubtotal().negate())
                    .total(origItem.getTotal().negate())
                    .sortOrder(order++)
                    .build();
            creditNote.getItems().add(creditItem);
        }

        recalcTotals(creditNote);
        original.setStatus(InvoiceStatus.CREDITED);
        invoiceRepo.save(original);

        auditService.log(companyId, userId, AuditAction.CREATE, "Invoice", null);
        return invoiceRepo.save(creditNote);
    }

    // ── Cálculo de totais ──────────────────────────────────────────

    private void recalcTotals(Invoice invoice) {
        BigDecimal subtotal  = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discount  = BigDecimal.ZERO;

        for (InvoiceItem item : invoice.getItems()) {
            subtotal  = subtotal.add(item.getSubtotal());
            taxAmount = taxAmount.add(item.getTaxAmount());
            discount  = discount.add(item.getDiscountAmt());
        }

        BigDecimal total = subtotal.add(taxAmount);
        invoice.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxAmount(taxAmount.setScale(2, RoundingMode.HALF_UP));
        invoice.setDiscountAmount(discount.setScale(2, RoundingMode.HALF_UP));
        invoice.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        invoice.setAmountDue(total.subtract(invoice.getAmountPaid()).setScale(2, RoundingMode.HALF_UP));
    }

    private InvoiceItem buildItem(Invoice invoice, ItemRequest dto, UUID companyId, int order) {
        BigDecimal qty       = dto.getQuantity();
        BigDecimal unitPrice = dto.getUnitPrice();
        BigDecimal discPct   = dto.getDiscountPct() != null ? dto.getDiscountPct() : BigDecimal.ZERO;
        BigDecimal taxRate   = BigDecimal.ZERO;

        String description = dto.getDescription();
        Product product    = null;

        // Se tem produto, buscar dados
        if (dto.getProductId() != null) {
            product = productRepo.findByIdAndCompanyIdAndDeletedAtIsNull(dto.getProductId(), companyId)
                    .orElse(null);
            if (product != null) {
                if (description == null) description = product.getName();
                if (unitPrice   == null) unitPrice    = product.getPrice();
                if (product.getTaxRate() != null) taxRate = product.getTaxRate().getRate();
            }
        }

        // Override explícito
        if (dto.getTaxRateId() != null) {
            taxRateRepo.findById(dto.getTaxRateId()).ifPresent(tr ->
                    dto.setTaxRateOverride(tr.getRate()));
        }
        if (dto.getTaxRateOverride() != null) taxRate = dto.getTaxRateOverride();
        if (unitPrice == null) unitPrice = BigDecimal.ZERO;

        BigDecimal lineSubtotal = qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discAmt      = lineSubtotal.multiply(discPct)
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal baseForTax   = lineSubtotal.subtract(discAmt);
        BigDecimal taxAmt       = baseForTax.multiply(taxRate)
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total        = baseForTax.add(taxAmt);

        return InvoiceItem.builder()
                .invoice(invoice)
                .product(product)
                .description(description != null ? description : "")
                .quantity(qty)
                .unit(dto.getUnit() != null ? dto.getUnit()
                        : (product != null ? product.getUnit()
                                : ao.saas.faturacao.common.enums.ProductUnit.UNIT))
                .unitPrice(unitPrice)
                .discountPct(discPct)
                .discountAmt(discAmt)
                .taxRate(taxRate)
                .taxAmount(taxAmt)
                .subtotal(baseForTax)
                .total(total)
                .sortOrder(order)
                .build();
    }
}
