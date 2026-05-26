package ao.saas.faturacao.modules.hr.payroll.controller;
import ao.saas.faturacao.common.response.*;
import ao.saas.faturacao.modules.hr.payroll.entity.HrPayroll;
import ao.saas.faturacao.modules.hr.payroll.service.PayrollService;
import ao.saas.faturacao.modules.hr.payroll.service.PayslipReportService;
import ao.saas.faturacao.modules.hr.payslips.entity.HrPayslip;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name="RH — Folhas de Pagamento")
@RestController @RequestMapping("/v1/companies/{companyId}/hr/payrolls")
@RequiredArgsConstructor
public class PayrollController {
    private final PayrollService        svc;
    private final PayslipReportService  reportSvc;
    private UUID uid(UserDetails p){ return UUID.fromString(p.getUsername()); }

    @GetMapping
    @Operation(summary="Listar folhas de pagamento")
    public ResponseEntity<ApiResponse<PageResponse<HrPayroll>>> list(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="12") int limit) {
        Page<HrPayroll> pg = svc.list(companyId, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(pg)));
    }

    @GetMapping("/{id}")
    @Operation(summary="Obter folha de pagamento")
    public ResponseEntity<ApiResponse<HrPayroll>> getOne(@PathVariable UUID companyId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getOne(companyId, id)));
    }

    @PostMapping
    @Operation(summary="Criar nova folha (DRAFT)")
    public ResponseEntity<ApiResponse<HrPayroll>> create(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal UserDetails p,
            @RequestBody Map<String,Object> body) {
        int year  = (int) body.get("year");
        int month = (int) body.get("month");
        String desc = (String) body.get("description");
        return ResponseEntity.status(201).body(ApiResponse.created(svc.create(companyId, uid(p), year, month, desc)));
    }

    @PostMapping("/{id}/calculate")
    @Operation(summary="Calcular — gera recibos para todos os colaboradores activos (IRT Angola)")
    public ResponseEntity<ApiResponse<HrPayroll>> calculate(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(svc.calculate(companyId, id, uid(p))));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary="Aprovar folha calculada")
    public ResponseEntity<ApiResponse<HrPayroll>> approve(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(svc.approve(companyId, id, uid(p))));
    }

    @PostMapping("/{id}/paid")
    @Operation(summary="Marcar folha como paga")
    public ResponseEntity<ApiResponse<HrPayroll>> markPaid(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(svc.markPaid(companyId, id, uid(p))));
    }

    @GetMapping("/{id}/payslips")
    @Operation(summary="Listar recibos de vencimento da folha")
    public ResponseEntity<ApiResponse<List<HrPayslip>>> listPayslips(
            @PathVariable UUID companyId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listPayslips(companyId, id)));
    }

    @GetMapping(value="/{id}/pdf", produces=MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary="Download PDF — Folha de Pagamento completa (JasperReports)")
    public ResponseEntity<byte[]> summaryPdf(
            @PathVariable UUID companyId, @PathVariable UUID id) {
        HrPayroll payroll = svc.getOne(companyId, id);
        byte[] pdf = reportSvc.generatePayrollSummaryPdf(companyId, id);
        String filename = "folha-pagamento_" + payroll.getPeriodYear() + "-" + String.format("%02d",payroll.getPeriodMonth()) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping(value="/{id}/xlsx", produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary="Download XLSX — Folha de Pagamento completa")
    public ResponseEntity<byte[]> summaryXlsx(
            @PathVariable UUID companyId, @PathVariable UUID id) {
        HrPayroll payroll = svc.getOne(companyId, id);
        byte[] xlsx = reportSvc.generatePayrollSummaryXlsx(companyId, id);
        String filename = "folha-pagamento_" + payroll.getPeriodYear() + "-" + String.format("%02d",payroll.getPeriodMonth()) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(xlsx);
    }

    @GetMapping(value="/payslips/{payslipId}/pdf", produces=MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary="Download PDF — Recibo de Vencimento individual (JasperReports)")
    public ResponseEntity<byte[]> payslipPdf(
            @PathVariable UUID companyId, @PathVariable UUID payslipId) {
        HrPayslip slip = svc.getPayslip(companyId, payslipId);
        byte[] pdf = reportSvc.generatePayslipPdf(companyId, payslipId);
        String empName = slip.getEmployee().getLastName().replaceAll("\\s+","-");
        String filename = "recibo-vencimento_" + empName + "_" + slip.getPeriodYear() + "-" + String.format("%02d",slip.getPeriodMonth()) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
