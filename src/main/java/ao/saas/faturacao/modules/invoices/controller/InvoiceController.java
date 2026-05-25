package ao.saas.faturacao.modules.invoices.controller;

import ao.saas.faturacao.common.response.ApiResponse;
import ao.saas.faturacao.common.response.PageResponse;
import ao.saas.faturacao.modules.invoices.dto.InvoiceDTOs.*;
import ao.saas.faturacao.modules.invoices.entity.Invoice;
import ao.saas.faturacao.modules.invoices.service.InvoiceService;
import ao.saas.faturacao.modules.reports.service.InvoiceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@Tag(name = "Facturas", description = "Gestão de facturas, notas de crédito e pagamentos")
@RestController
@RequestMapping("/v1/companies/{companyId}/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService       invoiceService;
    private final InvoiceReportService reportService;

    private UUID userId(UserDetails p) { return UUID.fromString(p.getUsername()); }

    // ── CRUD ───────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar facturas com filtros e paginação")
    public ResponseEntity<ApiResponse<PageResponse<Invoice>>> list(
            @PathVariable UUID companyId,
            @ParameterObject InvoiceFilterRequest filter) {
        Page<Invoice> page = invoiceService.list(companyId, filter);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhe de uma factura")
    public ResponseEntity<ApiResponse<Invoice>> getOne(
            @PathVariable UUID companyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.getOne(companyId, id)));
    }

    @PostMapping
    @Operation(summary = "Criar nova factura (DRAFT ou PENDING)")
    public ResponseEntity<ApiResponse<Invoice>> create(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateInvoiceRequest dto) {
        Invoice inv = invoiceService.create(companyId, userId(principal), dto);
        return ResponseEntity.status(201).body(ApiResponse.created(inv));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar factura (apenas DRAFT)")
    public ResponseEntity<ApiResponse<Invoice>> update(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateInvoiceRequest dto) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.update(companyId, id, userId(principal), dto)));
    }

    // ── Acções de estado ───────────────────────────────────────────

    @PostMapping("/{id}/finalize")
    @Operation(summary = "Finalizar rascunho → PENDING")
    public ResponseEntity<ApiResponse<Invoice>> finalize(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.finalize(companyId, id, userId(principal))));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Marcar como enviada ao cliente")
    public ResponseEntity<ApiResponse<Invoice>> send(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.send(companyId, id, userId(principal))));
    }

    @PostMapping("/{id}/payments")
    @Operation(summary = "Registar pagamento (total ou parcial)")
    public ResponseEntity<ApiResponse<Invoice>> registerPayment(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody PaymentRequest dto) {
        return ResponseEntity.ok(ApiResponse.ok(
            invoiceService.registerPayment(companyId, id, userId(principal), dto)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Anular factura")
    public ResponseEntity<ApiResponse<Invoice>> cancel(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody(required = false) CancelRequest dto) {
        String reason = dto != null ? dto.getReason() : null;
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.cancel(companyId, id, userId(principal), reason)));
    }

    @PostMapping("/{id}/credit-note")
    @Operation(summary = "Criar Nota de Crédito a partir desta factura")
    public ResponseEntity<ApiResponse<Invoice>> creditNote(
            @PathVariable UUID companyId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        Invoice cn = invoiceService.createCreditNote(companyId, id, userId(principal));
        return ResponseEntity.status(201).body(ApiResponse.created(cn));
    }

    // ── PDF / XLSX ─────────────────────────────────────────────────

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download PDF da factura (JasperReports)")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable UUID companyId,
            @PathVariable UUID id) {
        Invoice inv = invoiceService.getOne(companyId, id);
        byte[] pdf  = reportService.generateInvoicePdf(companyId, id);
        String filename = inv.getFullNumber().replace(" ", "_").replace("/", "-") + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .body(pdf);
    }

    @GetMapping(value = "/{id}/xlsx",
                produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Download XLSX da factura")
    public ResponseEntity<byte[]> downloadXlsx(
            @PathVariable UUID companyId,
            @PathVariable UUID id) {
        Invoice inv  = invoiceService.getOne(companyId, id);
        byte[] xlsx  = reportService.generateInvoiceXlsx(companyId, id);
        String filename = inv.getFullNumber().replace(" ", "_").replace("/", "-") + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(xlsx);
    }
}
