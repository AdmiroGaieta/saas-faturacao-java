package ao.saas.faturacao.modules.reports.controller;

import ao.saas.faturacao.common.response.ApiResponse;
import ao.saas.faturacao.modules.reports.service.ReportsService;
import ao.saas.faturacao.modules.reports.service.ReportsService.CustomerBalanceRow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Tag(name = "Relatórios", description = "Geração de relatórios em PDF, XLSX e CSV via JasperReports")
@RestController
@RequestMapping("/v1/companies/{companyId}/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ── Relatório de Vendas ────────────────────────────────────────

    @GetMapping("/sales")
    @Operation(summary = "Relatório de Vendas (PDF/XLSX/CSV)")
    public ResponseEntity<byte[]> salesReport(
            @PathVariable UUID companyId,
            @Parameter(description = "Data início (YYYY-MM-DD)")
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().withDayOfYear(1)}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Data fim (YYYY-MM-DD)")
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Formato: pdf | xlsx | csv")
            @RequestParam(defaultValue = "pdf") String format) {

        if (dateFrom == null) dateFrom = LocalDate.now().withDayOfYear(1);
        if (dateTo   == null) dateTo   = LocalDate.now();

        byte[] data = reportsService.salesReport(companyId, dateFrom.toString(), dateTo.toString(), format);
        return buildResponse(data, format, "relatorio-vendas_" + DATE_FMT.format(dateFrom) + "_" + DATE_FMT.format(dateTo));
    }

    // ── Relatório Mensal ───────────────────────────────────────────

    @GetMapping("/monthly")
    @Operation(summary = "Relatório Mensal Anual (PDF/XLSX)")
    public ResponseEntity<byte[]> monthlyReport(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") Integer year,
            @RequestParam(defaultValue = "pdf") String format) {

        if (year == null) year = LocalDate.now().getYear();
        byte[] data = reportsService.monthlyReport(companyId, year, format);
        return buildResponse(data, format, "relatorio-mensal_" + year);
    }

    // ── Declaração de IVA ──────────────────────────────────────────

    @GetMapping("/iva")
    @Operation(summary = "Declaração de IVA (PDF/XLSX)")
    public ResponseEntity<byte[]> ivaReport(
            @PathVariable UUID companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "pdf") String format) {

        byte[] data = reportsService.ivaReport(companyId, dateFrom.toString(), dateTo.toString(), format);
        return buildResponse(data, format, "declaracao-iva_" + DATE_FMT.format(dateFrom) + "_" + DATE_FMT.format(dateTo));
    }

    // ── Saldos de Clientes ─────────────────────────────────────────

    @GetMapping("/customer-balance")
    @Operation(summary = "Saldos em dívida por cliente (JSON)")
    public ResponseEntity<ApiResponse<List<CustomerBalanceRow>>> customerBalance(
            @PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(reportsService.customerBalance(companyId)));
    }

    // ── Helper ─────────────────────────────────────────────────────

    private ResponseEntity<byte[]> buildResponse(byte[] data, String format, String filename) {
        String contentType;
        String extension;

        switch (format.toLowerCase()) {
            case "xlsx":
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                extension   = ".xlsx";
                break;
            case "csv":
                contentType = "text/csv; charset=UTF-8";
                extension   = ".csv";
                break;
            default:
                contentType = MediaType.APPLICATION_PDF_VALUE;
                extension   = ".pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + extension + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(data);
    }
}
