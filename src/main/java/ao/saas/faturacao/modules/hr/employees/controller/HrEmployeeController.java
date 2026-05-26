package ao.saas.faturacao.modules.hr.employees.controller;
import ao.saas.faturacao.common.response.*;
import ao.saas.faturacao.modules.hr.employees.entity.HrEmployee;
import ao.saas.faturacao.modules.hr.employees.service.HrEmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Tag(name="RH — Colaboradores") @RestController @RequestMapping("/v1/companies/{companyId}/hr/employees")
@RequiredArgsConstructor
public class HrEmployeeController {
    private final HrEmployeeService svc;
    private UUID uid(UserDetails p){ return UUID.fromString(p.getUsername()); }

    @GetMapping public ResponseEntity<ApiResponse<PageResponse<HrEmployee>>> list(
            @PathVariable UUID companyId, @RequestParam(required=false) String search,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(svc.list(companyId, search, page, limit))));
    }
    @GetMapping("/stats") public ResponseEntity<ApiResponse<Map<String,Object>>> stats(@PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getStats(companyId)));
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<HrEmployee>> getOne(@PathVariable UUID companyId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getOne(companyId, id)));
    }
    @PostMapping public ResponseEntity<ApiResponse<HrEmployee>> create(
            @PathVariable UUID companyId, @AuthenticationPrincipal UserDetails p, @RequestBody HrEmployee dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(svc.create(companyId, uid(p), dto)));
    }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<HrEmployee>> update(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p, @RequestBody HrEmployee dto) {
        return ResponseEntity.ok(ApiResponse.ok(svc.update(companyId, id, uid(p), dto)));
    }
    @PostMapping("/{id}/terminate") public ResponseEntity<ApiResponse<HrEmployee>> terminate(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate terminationDate) {
        return ResponseEntity.ok(ApiResponse.ok(svc.terminate(companyId, id, uid(p), terminationDate)));
    }
}
