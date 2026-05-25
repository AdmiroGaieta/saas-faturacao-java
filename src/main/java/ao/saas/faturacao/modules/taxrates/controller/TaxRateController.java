package ao.saas.faturacao.modules.taxrates.controller;

import ao.saas.faturacao.common.response.ApiResponse;
import ao.saas.faturacao.modules.taxrates.entity.TaxRate;
import ao.saas.faturacao.modules.taxrates.service.TaxRateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Taxas IVA")
@RestController
@RequestMapping("/v1/companies/{companyId}/tax-rates")
@RequiredArgsConstructor
public class TaxRateController {
    private final TaxRateService svc;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxRate>>> list(@PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.list(companyId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaxRate>> create(
            @PathVariable UUID companyId, @RequestBody TaxRate dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(svc.create(companyId, dto)));
    }
}
