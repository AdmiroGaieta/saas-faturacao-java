package ao.saas.faturacao.modules.companies.controller;

import ao.saas.faturacao.common.response.ApiResponse;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.service.CompanyService;
import ao.saas.faturacao.modules.companies.service.CompanyService.DashboardStats;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Empresas")
@RestController
@RequestMapping("/v1/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService svc;
    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Company>>> list(@AuthenticationPrincipal UserDetails p) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listForUser(uid(p))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Company>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getOne(id)));
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<ApiResponse<DashboardStats>> dashboard(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getDashboard(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Company>> create(
            @AuthenticationPrincipal UserDetails p,
            @RequestBody Company dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(svc.create(uid(p), dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Company>> update(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p,
            @RequestBody Company dto) {
        return ResponseEntity.ok(ApiResponse.ok(svc.update(id, uid(p), dto)));
    }
}
