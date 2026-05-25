package ao.saas.faturacao.modules.reports.service;

import ao.saas.faturacao.common.enums.InvoiceStatus;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.customers.repository.CustomerRepository;
import ao.saas.faturacao.modules.invoices.entity.Invoice;
import ao.saas.faturacao.modules.invoices.repository.InvoiceRepository;
import ao.saas.faturacao.modules.reports.service.InvoiceReportService.MonthlyData;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportsService {

    private final InvoiceRepository   invoiceRepo;
    private final CustomerRepository  customerRepo;
    private final CompanyRepository   companyRepo;
    private final InvoiceReportService invoiceReportService;

    private static final String[] MONTH_NAMES = {
        "Janeiro","Fevereiro","Março","Abril","Maio","Junho",
        "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"
    };

    // ── Relatório de Vendas ────────────────────────────────────────

    public byte[] salesReport(UUID companyId, String dateFromStr, String dateToStr, String format) {
        LocalDate from = LocalDate.parse(dateFromStr);
        LocalDate to   = LocalDate.parse(dateToStr);

        Specification<Invoice> spec = Specification.<Invoice>where(
                (r, q, cb) -> cb.equal(r.get("company").get("id"), companyId))
            .and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("issueDate"), from))
            .and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("issueDate"), to))
            .and((r, q, cb) -> cb.notEqual(r.get("status"), InvoiceStatus.DRAFT));

        List<Invoice> invoices = invoiceRepo.findAll(spec);
        return invoiceReportService.generateSalesReport(companyId, dateFromStr, dateToStr, invoices, format);
    }

    // ── Relatório Mensal ───────────────────────────────────────────

    public byte[] monthlyReport(UUID companyId, int year, String format) {
        LocalDate from = LocalDate.of(year, 1,  1);
        LocalDate to   = LocalDate.of(year, 12, 31);

        Specification<Invoice> spec = Specification.<Invoice>where(
                (r, q, cb) -> cb.equal(r.get("company").get("id"), companyId))
            .and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("issueDate"), from))
            .and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("issueDate"), to))
            .and((r, q, cb) -> cb.notEqual(r.get("status"), InvoiceStatus.DRAFT))
            .and((r, q, cb) -> cb.notEqual(r.get("status"), InvoiceStatus.CANCELLED));

        List<Invoice> invoices = invoiceRepo.findAll(spec);

        // Agrupar por mês
        Map<Integer, List<Invoice>> byMonth = invoices.stream()
                .collect(Collectors.groupingBy(i -> i.getIssueDate().getMonthValue()));

        List<MonthlyData> monthlyData = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            List<Invoice> monthInvoices = byMonth.getOrDefault(m, Collections.emptyList());
            BigDecimal rev = monthInvoices.stream()
                    .map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tax = monthInvoices.stream()
                    .map(Invoice::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyData.add(MonthlyData.builder()
                    .month(m)
                    .monthName(MONTH_NAMES[m - 1])
                    .invoiceCount(monthInvoices.size())
                    .revenue(rev)
                    .taxes(tax)
                    .revenueFormatted(InvoiceReportService.formatAOA(rev))
                    .taxesFormatted(InvoiceReportService.formatAOA(tax))
                    .build());
        }

        return invoiceReportService.generateMonthlyReport(companyId, year, monthlyData, format);
    }

    // ── Relatório de IVA ───────────────────────────────────────────

    public byte[] ivaReport(UUID companyId, String dateFromStr, String dateToStr, String format) {
        LocalDate from = LocalDate.parse(dateFromStr);
        LocalDate to   = LocalDate.parse(dateToStr);

        Specification<Invoice> spec = Specification.<Invoice>where(
                (r, q, cb) -> cb.equal(r.get("company").get("id"), companyId))
            .and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("issueDate"), from))
            .and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("issueDate"), to))
            .and((r, q, cb) -> cb.notEqual(r.get("status"), InvoiceStatus.DRAFT))
            .and((r, q, cb) -> cb.notEqual(r.get("status"), InvoiceStatus.CANCELLED));

        List<Invoice> invoices = invoiceRepo.findAll(spec);
        return invoiceReportService.generateIvaReport(companyId, dateFromStr, dateToStr, invoices, format);
    }

    // ── Saldos de Clientes ─────────────────────────────────────────

    public List<CustomerBalanceRow> customerBalance(UUID companyId) {
        List<InvoiceStatus> openStatuses = Arrays.asList(
            InvoiceStatus.PENDING, InvoiceStatus.SENT,
            InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE);

        Specification<Invoice> spec = Specification.<Invoice>where(
                (r, q, cb) -> cb.equal(r.get("company").get("id"), companyId))
            .and((r, q, cb) -> r.get("status").in(openStatuses));

        List<Invoice> invoices = invoiceRepo.findAll(spec);

        Map<UUID, CustomerBalanceRow> balances = new LinkedHashMap<>();
        invoices.forEach(inv -> {
            UUID custId = inv.getCustomer().getId();
            balances.computeIfAbsent(custId, id -> CustomerBalanceRow.builder()
                    .customerId(custId)
                    .customerName(inv.getCustomer().getDisplayName())
                    .customerNif(inv.getCustomer().getNif())
                    .totalInvoiced(BigDecimal.ZERO)
                    .totalPaid(BigDecimal.ZERO)
                    .totalDue(BigDecimal.ZERO)
                    .invoiceCount(0)
                    .build());

            CustomerBalanceRow row = balances.get(custId);
            row.setTotalInvoiced(row.getTotalInvoiced().add(inv.getTotal()));
            row.setTotalPaid(row.getTotalPaid().add(inv.getAmountPaid()));
            row.setTotalDue(row.getTotalDue().add(inv.getAmountDue()));
            row.setInvoiceCount(row.getInvoiceCount() + 1);
        });

        return new ArrayList<>(balances.values()).stream()
                .sorted(Comparator.comparing(CustomerBalanceRow::getTotalDue).reversed())
                .collect(Collectors.toList());
    }

    // ── DTO ────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CustomerBalanceRow {
        private UUID       customerId;
        private String     customerName;
        private String     customerNif;
        private BigDecimal totalInvoiced;
        private BigDecimal totalPaid;
        private BigDecimal totalDue;
        private int        invoiceCount;
    }
}
