package ao.saas.faturacao.modules.hr.employees.entity;
import ao.saas.faturacao.common.BaseEntity;
import ao.saas.faturacao.common.enums.EmployeeContractType;
import ao.saas.faturacao.common.enums.EmployeeStatus;
import ao.saas.faturacao.common.enums.Gender;
import ao.saas.faturacao.modules.companies.entity.Company;
import ao.saas.faturacao.modules.hr.departments.entity.HrDepartment;
import ao.saas.faturacao.modules.hr.departments.entity.HrPosition;
import ao.saas.faturacao.modules.users.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="hr_employees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HrEmployee extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) @JsonIgnore
    private Company company;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
    private User user;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="department_id")
    private HrDepartment department;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="position_id")
    private HrPosition position;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="manager_id")
    private HrEmployee manager;
    // Dados pessoais
    @Column(name="employee_number",unique=true,length=20) private String employeeNumber;
    @Column(name="first_name",nullable=false,length=100) private String firstName;
    @Column(name="last_name",nullable=false,length=100) private String lastName;
    @Enumerated(EnumType.STRING) @Column(columnDefinition="gender") private Gender gender;
    @Column(name="birth_date") private LocalDate birthDate;
    @Column(length=100) private String nationality="Angolana";
    @Column(name="id_number",length=20) private String idNumber;
    @Column(length=20) private String nif;
    @Column(name="inss_number",length=20) private String inssNumber;
    // Contactos
    @Column(length=255) private String email;
    @Column(name="email_work",length=255) private String emailWork;
    @Column(length=30) private String phone;
    @Column(name="phone_emergency",length=30) private String phoneEmergency;
    @Column(name="emergency_contact",length=100) private String emergencyContact;
    @Column(length=500) private String address;
    @Column(length=100) private String city;
    @Column(length=100) private String province;
    // Dados laborais
    @Column(name="hire_date",nullable=false) private LocalDate hireDate;
    @Column(name="termination_date") private LocalDate terminationDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,columnDefinition="employee_status")
    private EmployeeStatus status=EmployeeStatus.ACTIVE;
    @Enumerated(EnumType.STRING) @Column(name="contract_type",nullable=false,columnDefinition="contract_type")
    private EmployeeContractType contractType=EmployeeContractType.PERMANENT;
    // Remuneração
    @Column(name="base_salary",nullable=false,precision=15,scale=2) private BigDecimal baseSalary=BigDecimal.ZERO;
    @Column(length=5) private String currency="AOA";
    @Column(name="bank_name",length=100) private String bankName;
    @Column(name="bank_account",length=100) private String bankAccount;
    @Column(name="bank_iban",length=50) private String bankIban;
    @Column(length=500) private String photo;
    @Column(columnDefinition="TEXT") private String notes;
    @Column(name="deleted_at") private LocalDateTime deletedAt;

    public String getFullName() { return firstName + " " + lastName; }
}
