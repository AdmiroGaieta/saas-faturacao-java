package ao.saas.faturacao.modules.expenses.expenses.controller;
import ao.saas.faturacao.common.response.*;
import ao.saas.faturacao.modules.expenses.expenses.entity.Expense;
import ao.saas.faturacao.modules.expenses.expenses.service.ExpenseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@Tag(name="Despesas") @RestController @RequestMapping("/v1/companies/{companyId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService svc;
    private UUID uid(UserDetails p){ return UUID.fromString(p.getUsername()); }

    @GetMapping public ResponseEntity<ApiResponse<PageResponse<Expense>>> list(
            @PathVariable UUID companyId, @RequestParam(required=false) String status,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int limit) {
        Page<Expense> pg = svc.list(companyId, status, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(pg)));
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<Expense>> getOne(@PathVariable UUID companyId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getOne(companyId, id)));
    }
    @GetMapping("/summary") public ResponseEntity<ApiResponse<Map<String,Object>>> summary(@PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getSummary(companyId)));
    }
    @PostMapping public ResponseEntity<ApiResponse<Expense>> create(
            @PathVariable UUID companyId, @AuthenticationPrincipal UserDetails p, @RequestBody Expense dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(svc.create(companyId, uid(p), dto)));
    }
    @PostMapping("/{id}/approve") public ResponseEntity<ApiResponse<Expense>> approve(
            @PathVariable UUID companyId, @PathVariable UUID id, @AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(svc.approve(companyId, id, uid(p))));
    }
    @PostMapping("/{id}/reject") public ResponseEntity<ApiResponse<Expense>> reject(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p, @RequestBody(required=false) Map<String,String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok(svc.reject(companyId, id, uid(p), reason)));
    }
}
