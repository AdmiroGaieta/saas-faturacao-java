package ao.saas.faturacao.modules.hr.leaves.controller;
import ao.saas.faturacao.common.response.*;
import ao.saas.faturacao.modules.hr.leaves.entity.HrLeave;
import ao.saas.faturacao.modules.hr.leaves.service.HrLeaveService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@Tag(name="RH — Férias & Ausências") @RestController @RequestMapping("/v1/companies/{companyId}/hr/leaves")
@RequiredArgsConstructor
public class HrLeaveController {
    private final HrLeaveService svc;
    private UUID uid(UserDetails p){ return UUID.fromString(p.getUsername()); }

    @GetMapping public ResponseEntity<ApiResponse<PageResponse<HrLeave>>> list(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int limit) {
        Page<HrLeave> pg = svc.list(companyId, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(pg)));
    }
    @PostMapping public ResponseEntity<ApiResponse<HrLeave>> create(
            @PathVariable UUID companyId, @RequestBody HrLeave dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(svc.create(companyId, dto)));
    }
    @PostMapping("/{id}/approve") public ResponseEntity<ApiResponse<HrLeave>> approve(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(svc.approve(companyId, id, uid(p))));
    }
    @PostMapping("/{id}/reject") public ResponseEntity<ApiResponse<HrLeave>> reject(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @RequestBody(required=false) Map<String,String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok(svc.reject(companyId, id, reason)));
    }
}
