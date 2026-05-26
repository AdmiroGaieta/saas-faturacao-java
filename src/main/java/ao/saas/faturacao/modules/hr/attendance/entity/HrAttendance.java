package ao.saas.faturacao.modules.hr.attendance.entity;
import ao.saas.faturacao.common.enums.AttendanceType;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.hr.employees.entity.HrEmployee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity @Table(name="hr_attendance",uniqueConstraints=@UniqueConstraint(columnNames={"employee_id","date"}))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HrAttendance {
    @Id @GeneratedValue @Column(columnDefinition="uuid",updatable=false) private UUID id;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="employee_id",nullable=false)
    private HrEmployee employee;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @Column(nullable=false) private LocalDate date;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="attendance_type")
    private AttendanceType type=AttendanceType.PRESENT;
    @Column(name="check_in") private LocalTime checkIn;
    @Column(name="check_out") private LocalTime checkOut;
    @Column(name="hours_worked",precision=5,scale=2) private BigDecimal hoursWorked;
    @Column(name="overtime_hours",precision=5,scale=2) private BigDecimal overtimeHours=BigDecimal.ZERO;
    @Column(columnDefinition="TEXT") private String notes;
    @CreatedDate @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
}
