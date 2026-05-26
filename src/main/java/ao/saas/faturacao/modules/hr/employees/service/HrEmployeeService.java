package ao.saas.faturacao.modules.hr.employees.service;
import ao.saas.faturacao.common.enums.AuditAction;
import ao.saas.faturacao.common.enums.EmployeeStatus;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.audit.service.AuditService;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.hr.employees.entity.HrEmployee;
import ao.saas.faturacao.modules.hr.employees.repository.HrEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service @RequiredArgsConstructor
public class HrEmployeeService {
    private final HrEmployeeRepository repo;
    private final CompanyRepository companyRepo;
    private final AuditService auditService;

    public Page<HrEmployee> list(UUID companyId, String search, int page, int limit) {
        return repo.search(companyId, search==null||search.isBlank()?null:search,
                PageRequest.of(page-1,limit, Sort.by("lastName","firstName")));
    }
    public HrEmployee getOne(UUID companyId, UUID id) {
        return repo.findByIdAndCompanyIdAndDeletedAtIsNull(id,companyId)
                .orElseThrow(() -> BusinessException.notFound("Colaborador não encontrado"));
    }
    @Transactional
    public HrEmployee create(UUID companyId, UUID userId, HrEmployee dto) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        dto.setCompany(company);
        if (dto.getEmployeeNumber() == null) {
            long count = repo.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, EmployeeStatus.ACTIVE);
            dto.setEmployeeNumber(String.format("EMP-%04d", count + 1));
        }
        HrEmployee saved = repo.save(dto);
        auditService.log(companyId, userId, AuditAction.CREATE, "HrEmployee", saved.getId());
        return saved;
    }
    @Transactional
    public HrEmployee update(UUID companyId, UUID id, UUID userId, HrEmployee dto) {
        HrEmployee e = getOne(companyId, id);
        e.setFirstName(dto.getFirstName()); e.setLastName(dto.getLastName());
        e.setGender(dto.getGender()); e.setBirthDate(dto.getBirthDate());
        e.setNif(dto.getNif()); e.setIdNumber(dto.getIdNumber()); e.setInssNumber(dto.getInssNumber());
        e.setEmail(dto.getEmail()); e.setEmailWork(dto.getEmailWork()); e.setPhone(dto.getPhone());
        e.setAddress(dto.getAddress()); e.setCity(dto.getCity()); e.setProvince(dto.getProvince());
        e.setBaseSalary(dto.getBaseSalary()); e.setContractType(dto.getContractType());
        e.setDepartment(dto.getDepartment()); e.setPosition(dto.getPosition());
        e.setBankName(dto.getBankName()); e.setBankAccount(dto.getBankAccount());
        e.setNotes(dto.getNotes());
        auditService.log(companyId, userId, AuditAction.UPDATE, "HrEmployee", id);
        return repo.save(e);
    }
    @Transactional
    public HrEmployee terminate(UUID companyId, UUID id, UUID userId, LocalDate terminationDate) {
        HrEmployee e = getOne(companyId, id);
        e.setStatus(EmployeeStatus.TERMINATED);
        e.setTerminationDate(terminationDate != null ? terminationDate : LocalDate.now());
        auditService.log(companyId, userId, AuditAction.UPDATE, "HrEmployee", id);
        return repo.save(e);
    }
    public Map<String,Object> getStats(UUID companyId) {
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("active",    repo.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, EmployeeStatus.ACTIVE));
        s.put("onLeave",   repo.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, EmployeeStatus.ON_LEAVE));
        s.put("terminated",repo.countByCompanyIdAndStatusAndDeletedAtIsNull(companyId, EmployeeStatus.TERMINATED));
        return s;
    }
}
