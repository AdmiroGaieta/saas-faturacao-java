package ao.saas.faturacao.modules.subscriptions.controller;

import ao.saas.faturacao.common.response.ApiResponse;
import ao.saas.faturacao.modules.subscriptions.entity.Subscription;
import ao.saas.faturacao.modules.subscriptions.service.SubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Subscrições")
@RestController
@RequestMapping("/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService svc;

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Subscription>> get(@PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.getByCompany(companyId)));
    }

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<Map<String,Object>>>> plans() {
        List<Map<String,Object>> plans = Arrays.asList(
            plan("FREE",         0,      1,   5,   20,  100, "Plano gratuito",
                Arrays.asList("1 utilizador","5 facturas/mês","20 clientes","PDF básico")),
            plan("STARTER",      5000,   3,   50,  200, 500, "Pequenas empresas",
                Arrays.asList("3 utilizadores","50 facturas/mês","200 clientes","PDF profissional","Relatórios")),
            plan("PROFESSIONAL", 15000,  10,  500, 2000,2000,"Para empresas em crescimento",
                Arrays.asList("10 utilizadores","500 facturas/mês","2.000 clientes","Relatórios avançados","Integração AGT")),
            plan("ENTERPRISE",   0,      -1,  -1,  -1,  -1,  "Soluções à medida",
                Arrays.asList("Utilizadores ilimitados","Facturas ilimitadas","Clientes ilimitados","Suporte dedicado","SLA garantido"))
        );
        return ResponseEntity.ok(ApiResponse.ok(plans));
    }

    private Map<String,Object> plan(String id, int price, int users,
            int invoices, int customers, int products, String description, List<String> features) {
        Map<String,Object> p = new LinkedHashMap<>();
        p.put("id", id); p.put("name", id); p.put("price", price);
        p.put("maxUsers", users); p.put("maxInvoices", invoices);
        p.put("maxCustomers", customers); p.put("maxProducts", products);
        p.put("description", description); p.put("features", features);
        return p;
    }
}
