package ao.saas.faturacao.modules.hr.leaves.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.LeaveStatus;
import ao.saas.faturacao.common.enums.LeaveType;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.hr.employees.entity.HrEmployee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="hr_leaves")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HrLeave extends BaseEntity {
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="employee_id",nullable=false)
    private HrEmployee employee;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="approved_by")
    private HrEmployee approvedBy;
    @Enumerated(EnumType.STRING) @Column(name="leave_type",nullable=false,columnDefinition="leave_type")
    private LeaveType leaveType=LeaveType.ANNUAL;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="leave_status")
    private LeaveStatus status=LeaveStatus.PENDING;
    @Column(name="start_date",nullable=false) private LocalDate startDate;
    @Column(name="end_date",nullable=false) private LocalDate endDate;
    @Column(nullable=false,precision=5,scale=1) private BigDecimal days;
    @Column(columnDefinition="TEXT") private String reason;
    @Column(columnDefinition="TEXT") private String notes;
    @Column(name="rejected_reason",columnDefinition="TEXT") private String rejectedReason;
    @Column(name="approved_at") private LocalDateTime approvedAt;
}
