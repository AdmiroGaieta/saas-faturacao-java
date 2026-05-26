package ao.saas.faturacao.modules.hr.departments.controller;
import ao.saas.faturacao.common.response.ApiResponse;
import ao.saas.faturacao.modules.hr.departments.entity.*;
import ao.saas.faturacao.modules.hr.departments.service.HrDepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Tag(name="RH — Departamentos & Cargos") @RestController @RequestMapping("/v1/companies/{companyId}/hr")
@RequiredArgsConstructor
public class HrDepartmentController {
    private final HrDepartmentService svc;

    @GetMapping("/departments") public ResponseEntity<ApiResponse<List<HrDepartment>>> depts(@PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listDepartments(companyId)));
    }
    @PostMapping("/departments") public ResponseEntity<ApiResponse<HrDepartment>> createDept(
            @PathVariable UUID companyId, @RequestBody HrDepartment dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(svc.createDept(companyId, dto)));
    }
    @GetMapping("/positions") public ResponseEntity<ApiResponse<List<HrPosition>>> positions(@PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.ok(svc.listPositions(companyId)));
    }
    @PostMapping("/positions") public ResponseEntity<ApiResponse<HrPosition>> createPosition(
            @PathVariable UUID companyId, @RequestBody HrPosition dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(svc.createPosition(companyId, dto)));
    }
}
