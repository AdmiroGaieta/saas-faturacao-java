package ao.saas.faturacao.modules.products.controller;

import ao.saas.faturacao.common.response.ApiResponse;
import ao.saas.faturacao.common.response.PageResponse;
import ao.saas.faturacao.modules.products.entity.Product;
import ao.saas.faturacao.modules.products.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Produtos & Serviços")
@RestController
@RequestMapping("/v1/companies/{companyId}/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService svc;
    private UUID uid(UserDetails p) { return UUID.fromString(p.getUsername()); }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Product>>> list(
            @PathVariable UUID companyId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Product> pg = svc.list(companyId, search, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(pg)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getOne(@PathVariable UUID companyId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getOne(companyId, id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> create(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal UserDetails p,
            @RequestBody Product dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(svc.create(companyId, uid(p), dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> update(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p,
            @RequestBody Product dto) {
        return ResponseEntity.ok(ApiResponse.ok(svc.update(companyId, id, uid(p), dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID companyId, @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails p) {
        svc.delete(companyId, id, uid(p));
        return ResponseEntity.ok(ApiResponse.ok("Produto eliminado"));
    }
}
