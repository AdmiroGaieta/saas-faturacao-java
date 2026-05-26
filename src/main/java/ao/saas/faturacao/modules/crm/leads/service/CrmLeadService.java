package ao.saas.faturacao.modules.crm.leads.service;
import ao.saas.faturacao.common.enums.AuditAction;
import ao.saas.faturacao.common.enums.LeadStatus;
import ao.saas.faturacao.common.exceptions.BusinessException;
import ao.saas.faturacao.modules.audit.service.AuditService;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.companies.repository.CompanyRepository;
import ao.saas.faturacao.modules.crm.leads.entity.CrmLead;
import ao.saas.faturacao.modules.crm.leads.repository.CrmLeadRepository;
import ao.saas.faturacao.modules.crm.pipelines.entity.CrmPipeline;
import ao.saas.faturacao.modules.crm.pipelines.entity.CrmStage;
import ao.saas.faturacao.modules.crm.pipelines.repository.CrmPipelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor
public class CrmLeadService {
    private final CrmLeadRepository leadRepo;
    private final CrmPipelineRepository pipelineRepo;
    private final CompanyRepository companyRepo;
    private final AuditService auditService;

    public Page<CrmLead> list(UUID companyId, String status, String search, int page, int limit) {
        Specification<CrmLead> spec = (r,q,cb) -> cb.equal(r.get("company").get("id"), companyId);
        if (status != null && !status.isBlank()) {
            try { LeadStatus s = LeadStatus.valueOf(status.toUpperCase());
                spec = spec.and((r,q,cb) -> cb.equal(r.get("status"), s));
            } catch (Exception ignored) {}
        }
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((r,q,cb) -> cb.or(
                cb.like(cb.lower(r.get("title")), like),
                cb.like(cb.lower(cb.coalesce(r.get("contactName"), "")), like),
                cb.like(cb.lower(cb.coalesce(r.get("contactCompany"), "")), like)
            ));
        }
        return leadRepo.findAll(spec, PageRequest.of(page-1, limit, Sort.by(Sort.Direction.DESC,"createdAt")));
    }

    public CrmLead getOne(UUID companyId, UUID id) {
        return leadRepo.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> BusinessException.notFound("Lead não encontrado"));
    }

    @Transactional
    public CrmLead create(UUID companyId, UUID userId, CrmLead dto) {
        Company company = companyRepo.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> BusinessException.notFound("Empresa não encontrada"));
        dto.setCompany(company);
        // Associar ao pipeline default se não especificado
        if (dto.getPipeline() == null) {
            pipelineRepo.findByCompanyIdAndIsDefaultTrue(companyId).ifPresent(dto::setPipeline);
        }
        CrmLead saved = leadRepo.save(dto);
        auditService.log(companyId, userId, AuditAction.CREATE, "CrmLead", saved.getId());
        return saved;
    }

    @Transactional
    public CrmLead update(UUID companyId, UUID id, UUID userId, CrmLead dto) {
        CrmLead lead = getOne(companyId, id);
        lead.setTitle(dto.getTitle()); lead.setDescription(dto.getDescription());
        lead.setStatus(dto.getStatus()); lead.setSource(dto.getSource());
        lead.setValue(dto.getValue()); lead.setProbability(dto.getProbability());
        lead.setExpectedClose(dto.getExpectedClose());
        lead.setContactName(dto.getContactName()); lead.setContactEmail(dto.getContactEmail());
        lead.setContactPhone(dto.getContactPhone()); lead.setContactCompany(dto.getContactCompany());
        lead.setNotes(dto.getNotes());
        if (dto.getStage() != null) lead.setStage(dto.getStage());
        if (dto.getAssignedTo() != null) lead.setAssignedTo(dto.getAssignedTo());
        if ((dto.getStatus() == LeadStatus.WON || dto.getStatus() == LeadStatus.LOST) && lead.getClosedAt() == null) {
            lead.setClosedAt(LocalDateTime.now());
        }
        auditService.log(companyId, userId, AuditAction.UPDATE, "CrmLead", id);
        return leadRepo.save(lead);
    }

    public Map<String,Object> getDashboard(UUID companyId) {
        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("total",      leadRepo.count());
        stats.put("new",        leadRepo.countByCompanyIdAndStatus(companyId, LeadStatus.NEW));
        stats.put("inProgress", leadRepo.countByCompanyIdAndStatus(companyId, LeadStatus.NEGOTIATION));
        stats.put("won",        leadRepo.countByCompanyIdAndStatus(companyId, LeadStatus.WON));
        stats.put("lost",       leadRepo.countByCompanyIdAndStatus(companyId, LeadStatus.LOST));
        stats.put("wonValue",   leadRepo.sumWonValue(companyId));
        return stats;
    }
}
