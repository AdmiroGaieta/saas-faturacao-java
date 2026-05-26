package ao.saas.faturacao.modules.crm.leads.controller;
import ao.saas.faturacao.common.response.*;
import ao.saas.faturacao.modules.crm.leads.entity.CrmLead;
import ao.saas.faturacao.modules.crm.leads.service.CrmLeadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@Tag(name="CRM — Leads") @RestController @RequestMapping("/v1/companies/{companyId}/crm/leads")
@RequiredArgsConstructor
public class CrmLeadController {
    private final CrmLeadService svc;
    private UUID uid(UserDetails p){ return UUID.fromString(p.getUsername()); }

    @GetMapping public ResponseEntity<ApiResponse<PageResponse<CrmLead>>> list(
            @PathVariable UUID companyId, @RequestParam(required=false) String status,
            @RequestParam(required=false) String search,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int limit) {
        Page<CrmLead> pg = svc.list(companyId,status,search,page,limit);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(pg)));
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<CrmLead>> getOne(
            @PathVariable UUID companyId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getOne(companyId,id)));
    }
    @GetMapping("/dashboard") public ResponseEntity<ApiResponse<Map<String,Object>>> dashboard(@PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getDashboard(companyId)));
    }
    @PostMapping public ResponseEntity<ApiResponse<CrmLead>> create(
            @PathVariable UUID companyId, @AuthenticationPrincipal UserDetails p, @RequestBody CrmLead dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(svc.create(companyId,uid(p),dto)));
    }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<CrmLead>> update(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p, @RequestBody CrmLead dto) {
        return ResponseEntity.ok(ApiResponse.ok(svc.update(companyId,id,uid(p),dto)));
    }
}
