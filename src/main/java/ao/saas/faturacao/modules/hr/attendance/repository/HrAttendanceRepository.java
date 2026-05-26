package ao.saas.faturacao.modules.hr.attendance.repository;
import ao.saas.faturacao.modules.hr.attendance.entity.HrAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface HrAttendanceRepository extends JpaRepository<HrAttendance,UUID> {
    Optional<HrAttendance> findByEmployeeIdAndDate(UUID employeeId, LocalDate date);
    List<HrAttendance> findByEmployeeIdAndDateBetweenOrderByDateAsc(UUID empId, LocalDate from, LocalDate to);
    @Query("SELECT COUNT(a) FROM HrAttendance a WHERE a.employee.id=:eid AND a.date>=:f AND a.date<=:t AND a.type='PRESENT'")
    long countPresentDays(@Param("eid") UUID empId, @Param("f") LocalDate from, @Param("t") LocalDate to);
}
