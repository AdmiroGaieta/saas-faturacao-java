package ao.saas.faturacao.modules.reports.service;

import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.invoices.entity.Invoice;
import ao.saas.faturacao.modules.invoices.entity.InvoiceItem;
import ao.saas.faturacao.modules.invoices.repository.InvoiceRepository;
import ao.saas.faturacao.common.exceptions.BusinessException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceReportService {

    private final JasperReportService jasperService;
    private final InvoiceRepository invoiceRepo;
    private final CompanyRepository companyRepo;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat AOA_FMT;

    static {
        AOA_FMT = NumberFormat.getInstance(new Locale("pt", "AO"));
        AOA_FMT.setMinimumFractionDigits(2);
        AOA_FMT.setMaximumFractionDigits(2);
    }

    // ── Comprovativo / Factura PDF ─────────────────────────────────

    public byte[] generateInvoicePdf(UUID companyId, UUID invoiceId) {
        Invoice invoice = invoiceRepo.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> BusinessException.notFound("Factura não encontrada"));

        Company company = invoice.getCompany();

        Map<String, Object> params = buildInvoiceParams(invoice, company);
        List<InvoiceItemRow> items = buildItemRows(invoice);

        return jasperService.generatePdf("invoice", params, items);
    }

    public byte[] generateInvoiceXlsx(UUID companyId, UUID invoiceId) {
        Invoice invoice = invoiceRepo.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> BusinessException.notFound("Factura não encontrada"));
        Company company = invoice.getCompany();
        Map<String, Object> params = buildInvoiceParams(invoice, company);
        List<InvoiceItemRow> items = buildItemRows(invoice);
        return jasperService.generateXlsx("invoice", params, items);
    }

    // ── Relatório de Vendas ────────────────────────────────────────

    public byte[] generateSalesReport(UUID companyId, String dateFrom, String dateTo,
                                       List<Invoice> invoices, String format) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));

        BigDecimal totalSubtotal = invoices.stream()
                .map(Invoice::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax = invoices.stream()
                .map(Invoice::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = invoices.stream()
                .map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = invoices.stream()
                .map(Invoice::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDue = invoices.stream()
                .map(Invoice::getAmountDue).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> params = new HashMap<>();
        params.put("COMPANY_NAME",    company.getName());
        params.put("COMPANY_NIF",     company.getNif());
        params.put("COMPANY_ADDRESS", buildAddress(company));
        params.put("REPORT_TITLE",    "Relatório de Vendas");
        params.put("DATE_FROM",       dateFrom);
        params.put("DATE_TO",         dateTo);
        params.put("TOTAL_RECORDS",   invoices.size());
        params.put("TOTAL_SUBTOTAL",  formatAOA(totalSubtotal));
        params.put("TOTAL_TAX",       formatAOA(totalTax));
        params.put("TOTAL_AMOUNT",    formatAOA(totalAmount));
        params.put("TOTAL_PAID",      formatAOA(totalPaid));
        params.put("TOTAL_DUE",       formatAOA(totalDue));
        params.put("GENERATED_AT",    java.time.LocalDateTime.now()
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        List<SalesReportRow> rows = invoices.stream()
                .map(inv -> SalesReportRow.builder()
                        .fullNumber(inv.getFullNumber())
                        .customerName(inv.getCustomer().getDisplayName())
                        .customerNif(inv.getCustomer().getNif() != null ? inv.getCustomer().getNif() : "")
                        .issueDate(inv.getIssueDate().format(DATE_FMT))
                        .dueDate(inv.getDueDate() != null ? inv.getDueDate().format(DATE_FMT) : "")
                        .status(inv.getStatus().name())
                        .subtotal(formatAOA(inv.getSubtotal()))
                        .taxAmount(formatAOA(inv.getTaxAmount()))
                        .total(formatAOA(inv.getTotal()))
                        .amountPaid(formatAOA(inv.getAmountPaid()))
                        .amountDue(formatAOA(inv.getAmountDue()))
                        .build())
                .collect(Collectors.toList());

        if ("xlsx".equalsIgnoreCase(format)) {
            return jasperService.generateXlsx("sales_report", params, rows);
        } else if ("csv".equalsIgnoreCase(format)) {
            return jasperService.generateCsv("sales_report", params, rows);
        }
        return jasperService.generatePdf("sales_report", params, rows);
    }

    // ── Relatório de IVA ───────────────────────────────────────────

    public byte[] generateIvaReport(UUID companyId, String dateFrom, String dateTo,
                                     List<Invoice> invoices, String format) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));

        // Agrupar por taxa de IVA
        Map<BigDecimal, IvaGroup> groups = new LinkedHashMap<>();
        invoices.forEach(inv ->
            inv.getItems().forEach(item -> {
                BigDecimal rate = item.getTaxRate();
                groups.computeIfAbsent(rate, r -> new IvaGroup(r, BigDecimal.ZERO, BigDecimal.ZERO))
                        .addValues(item.getSubtotal(), item.getTaxAmount());
            })
        );

        BigDecimal totalBase = groups.values().stream()
                .map(IvaGroup::getBase).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIva  = groups.values().stream()
                .map(IvaGroup::getIva).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> params = new HashMap<>();
        params.put("COMPANY_NAME",    company.getName());
        params.put("COMPANY_NIF",     company.getNif());
        params.put("REPORT_TITLE",    "Declaração de IVA");
        params.put("DATE_FROM",       dateFrom);
        params.put("DATE_TO",         dateTo);
        params.put("TOTAL_BASE",      formatAOA(totalBase));
        params.put("TOTAL_IVA",       formatAOA(totalIva));
        params.put("GENERATED_AT",    java.time.LocalDateTime.now()
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        List<IvaReportRow> rows = groups.values().stream()
                .map(g -> IvaReportRow.builder()
                        .taxRate(g.getRate().stripTrailingZeros().toPlainString() + "%")
                        .baseAmount(formatAOA(g.getBase()))
                        .ivaAmount(formatAOA(g.getIva()))
                        .build())
                .collect(Collectors.toList());

        if ("xlsx".equalsIgnoreCase(format)) return jasperService.generateXlsx("iva_report", params, rows);
        return jasperService.generatePdf("iva_report", params, rows);
    }

    // ── Relatório Mensal ───────────────────────────────────────────

    public byte[] generateMonthlyReport(UUID companyId, int year,
                                         List<MonthlyData> monthlyData, String format) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));

        Map<String, Object> params = new HashMap<>();
        params.put("COMPANY_NAME",  company.getName());
        params.put("COMPANY_NIF",   company.getNif());
        params.put("REPORT_TITLE",  "Relatório Anual de Vendas — " + year);
        params.put("YEAR",          String.valueOf(year));
        params.put("GENERATED_AT",  java.time.LocalDateTime.now()
                                      .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        BigDecimal totalRevenue = monthlyData.stream()
                .map(MonthlyData::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        params.put("TOTAL_REVENUE", formatAOA(totalRevenue));

        if ("xlsx".equalsIgnoreCase(format)) return jasperService.generateXlsx("monthly_report", params, monthlyData);
        return jasperService.generatePdf("monthly_report", params, monthlyData);
    }

    // ── Helpers ────────────────────────────────────────────────────

    private Map<String, Object> buildInvoiceParams(Invoice invoice, Company company) {
        Map<String, Object> p = new HashMap<>();

        // Empresa emissora
        p.put("COMPANY_NAME",     company.getName());
        p.put("COMPANY_NIF",      company.getNif());
        p.put("COMPANY_ADDRESS",  buildAddress(company));
        p.put("COMPANY_PHONE",    company.getPhone() != null ? company.getPhone() : "");
        p.put("COMPANY_EMAIL",    company.getEmail() != null ? company.getEmail() : "");
        p.put("COMPANY_BANK",     company.getBankName() != null ? company.getBankName() : "");
        p.put("COMPANY_ACCOUNT",  company.getBankAccount() != null ? company.getBankAccount() : "");
        p.put("COMPANY_IBAN",     company.getBankIban() != null ? company.getBankIban() : "");

        // Documento
        p.put("DOC_TYPE",         invoice.getType().getLabel());
        p.put("DOC_NUMBER",       invoice.getFullNumber());
        p.put("DOC_STATUS",       invoice.getStatus().name());
        p.put("ISSUE_DATE",       invoice.getIssueDate().format(DATE_FMT));
        p.put("DUE_DATE",         invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FMT) : "");
        p.put("CURRENCY",         invoice.getCurrency());
        p.put("REFERENCE",        invoice.getReferenceNumber() != null ? invoice.getReferenceNumber() : "");

        // Cliente
        p.put("CUSTOMER_NAME",    invoice.getCustomer().getDisplayName());
        p.put("CUSTOMER_NIF",     invoice.getCustomer().getNif() != null ? invoice.getCustomer().getNif() : "");
        p.put("CUSTOMER_ADDRESS", buildCustomerAddress(invoice.getCustomer()));
        p.put("CUSTOMER_EMAIL",   invoice.getCustomer().getEmail() != null ? invoice.getCustomer().getEmail() : "");
        p.put("CUSTOMER_PHONE",   invoice.getCustomer().getPhone() != null ? invoice.getCustomer().getPhone() : "");

        // Totais
        p.put("SUBTOTAL",         formatAOA(invoice.getSubtotal()));
        p.put("DISCOUNT",         formatAOA(invoice.getDiscountAmount()));
        p.put("TAX_AMOUNT",       formatAOA(invoice.getTaxAmount()));
        p.put("TOTAL",            formatAOA(invoice.getTotal()));
        p.put("AMOUNT_PAID",      formatAOA(invoice.getAmountPaid()));
        p.put("AMOUNT_DUE",       formatAOA(invoice.getAmountDue()));

        // Notas
        p.put("NOTES",            invoice.getNotes() != null ? invoice.getNotes() : "");
        p.put("TERMS",            invoice.getTermsConditions() != null ? invoice.getTermsConditions() : company.getTermsConditions() != null ? company.getTermsConditions() : "");

        // AGT
        p.put("AGT_HASH",         invoice.getAgtHash() != null ? invoice.getAgtHash() : "");
        p.put("AGT_QR",           invoice.getAgtQrCode() != null ? invoice.getAgtQrCode() : "");
        p.put("GENERATED_AT",     java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        return p;
    }

    private List<InvoiceItemRow> buildItemRows(Invoice invoice) {
        return invoice.getItems().stream()
                .map(item -> InvoiceItemRow.builder()
                        .description(item.getDescription())
                        .quantity(item.getQuantity().stripTrailingZeros().toPlainString())
                        .unit(item.getUnit().getLabel())
                        .unitPrice(formatAOA(item.getUnitPrice()))
                        .discountPct(item.getDiscountPct().compareTo(BigDecimal.ZERO) > 0
                                ? item.getDiscountPct().toPlainString() + "%" : "")
                        .taxRate(item.getTaxRate().toPlainString() + "%")
                        .subtotal(formatAOA(item.getSubtotal()))
                        .total(formatAOA(item.getTotal()))
                        .build())
                .collect(Collectors.toList());
    }

    private String buildAddress(Company c) {
        StringBuilder sb = new StringBuilder();
        if (c.getAddress() != null) sb.append(c.getAddress()).append("\n");
        if (c.getCity()    != null) sb.append(c.getCity());
        if (c.getProvince()!= null) sb.append(", ").append(c.getProvince());
        return sb.toString().trim();
    }

    private String buildCustomerAddress(ao.saas.faturacao.modules.customers.entity.Customer c) {
        StringBuilder sb = new StringBuilder();
        if (c.getAddress() != null) sb.append(c.getAddress()).append("\n");
        if (c.getCity()    != null) sb.append(c.getCity());
        if (c.getProvince()!= null) sb.append(", ").append(c.getProvince());
        return sb.toString().trim();
    }

    public static String formatAOA(BigDecimal value) {
        if (value == null) return "0,00";
        return AOA_FMT.format(value) + " AOA";
    }

    // ── DTOs de linha ──────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class InvoiceItemRow {
        private String description;
        private String quantity;
        private String unit;
        private String unitPrice;
        private String discountPct;
        private String taxRate;
        private String subtotal;
        private String total;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SalesReportRow {
        private String fullNumber;
        private String customerName;
        private String customerNif;
        private String issueDate;
        private String dueDate;
        private String status;
        private String subtotal;
        private String taxAmount;
        private String total;
        private String amountPaid;
        private String amountDue;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class IvaReportRow {
        private String taxRate;
        private String baseAmount;
        private String ivaAmount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MonthlyData {
        private int month;
        private String monthName;
        private int invoiceCount;
        private BigDecimal revenue;
        private BigDecimal taxes;
        private String revenueFormatted;
        private String taxesFormatted;
    }

    @Data
    @AllArgsConstructor
    private static class IvaGroup {
        private BigDecimal rate;
        private BigDecimal base;
        private BigDecimal iva;

        public void addValues(BigDecimal base, BigDecimal iva) {
            this.base = this.base.add(base != null ? base : BigDecimal.ZERO);
            this.iva  = this.iva.add(iva  != null ? iva  : BigDecimal.ZERO);
        }
    }
}
