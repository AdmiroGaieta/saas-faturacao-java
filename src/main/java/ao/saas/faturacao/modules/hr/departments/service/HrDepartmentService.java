package ao.saas.faturacao.modules.hr.departments.service;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.hr.departments.entity.*;
import ao.saas.faturacao.modules.hr.departments.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class HrDepartmentService {
    private final HrDepartmentRepository deptRepo;
    private final HrPositionRepository posRepo;
    private final CompanyRepository companyRepo;

    public List<HrDepartment> listDepartments(UUID companyId) {
        return deptRepo.findByCompanyIdAndIsActiveTrueOrderByNameAsc(companyId);
    }
    public List<HrPosition> listPositions(UUID companyId) {
        return posRepo.findByCompanyIdAndIsActiveTrueOrderByNameAsc(companyId);
    }
    @Transactional
    public HrDepartment createDept(UUID companyId, HrDepartment dto) {
        Company c = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        dto.setCompany(c);
        return deptRepo.save(dto);
    }
    @Transactional
    public HrPosition createPosition(UUID companyId, HrPosition dto) {
        Company c = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        dto.setCompany(c);
        return posRepo.save(dto);
    }
}
