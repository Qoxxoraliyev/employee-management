package com.muhammadali.employee_management.repository;

import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.Salary;
import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
public class EmployeeRepositoryTests {


    @Container
    static final PostgreSQLContainer<?> pg =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("employee_management")
                    .withUsername("mohirdev")
                    .withPassword("123");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
    }


    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository repo;

    private Department hr, it;
    private Employee john, jane, bob;

    @BeforeEach
    void setUp() {
        hr = Department.builder().name("Human Resources").managerId(1).build();
        it = Department.builder().name("IT").managerId(2).build();

        entityManager.persistAndFlush(hr);
        entityManager.persistAndFlush(it);

        john = employee("John", "Doe", Gender.MALE, LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1), Status.ACTIVE, hr, "5000");

        jane = employee("Jane", "Smith", Gender.FEMALE, LocalDate.of(1992, 2, 2),
                LocalDate.of(2021, 1, 1), Status.ACTIVE, hr, "6000");

        bob = employee("Bob", "Johnson", Gender.MALE, LocalDate.of(1988, 3, 3),
                LocalDate.of(2022, 1, 1), Status.INACTIVE, it, "7000");

        entityManager.persistAndFlush(john);
        entityManager.persistAndFlush(jane);
        entityManager.persistAndFlush(bob);
    }


    @Test
    @DisplayName("findByStatus – filter by enum")
    void findByStatus() {
        List<Employee> active = repo.findByStatus(Status.ACTIVE);
        assertThat(active)
                .hasSize(2)
                .extracting("firstName")
                .containsExactly("John", "Jane");
    }


    @Test
    @DisplayName("findByDepartmentName – join department")
    void findByDepartmentName() {
        List<Employee> hrEmps = repo.findByDepartmentName("Human Resources");
        assertThat(hrEmps).hasSize(2);
    }


    @Test
    @DisplayName("countByDepartment_Id – scalar count")
    void countByDepartmentId() {
        assertThat(repo.countByDepartment_Id(hr.getId())).isEqualTo(2);
        assertThat(repo.countByDepartment_Id(it.getId())).isEqualTo(1);
    }


    @Test
    @DisplayName("countByStatus – aggregate by enum")
    void countByStatus() {
        assertThat(repo.countByStatus(Status.ACTIVE)).isEqualTo(2);
        assertThat(repo.countByStatus(Status.INACTIVE)).isEqualTo(1);
    }


    @Test
    @DisplayName("countByHireDateAfter – date comparison")
    void countByHireDateAfter() {
        assertThat(repo.countByHireDateAfter(LocalDate.of(2020, 6, 1))).isEqualTo(2);
    }

    @Test
    @DisplayName("findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase – OR search")
    void findByNameOrLastNameContainingIgnoreCase() {
        List<Employee> res = repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("jo", "smi");
        assertThat(res)
                .hasSize(2)
                .extracting("firstName")
                .containsExactly("Bob", "Jane");
    }

    @Test
    @DisplayName("findByPhoneContainingIgnoreCase – phone substring")
    void findByPhoneContainingIgnoreCase() {
        List<Employee> res = repo.findByPhoneContainingIgnoreCase("111");
        assertThat(res)
                .hasSize(1)
                .extracting("firstName")
                .containsExactly("John");
    }

    @Test
    @DisplayName("findByGender – enum filter")
    void findByGender() {
        List<Employee> males = repo.findByGender(Gender.MALE);
        assertThat(males).hasSize(2);
    }

    @Test
    @DisplayName("findByBirthDateBetween – date range")
    void findByBirthDateBetween() {
        List<Employee> res = repo.findByBirthDateBetween(
                LocalDate.of(1989, 1, 1),
                LocalDate.of(1991, 1, 1));
        assertThat(res).hasSize(2);
    }



    @Test
    @DisplayName("findByDepartmentNameAndPositionIgnoreCase – case-insensitive department + position")
    void findByDepartmentNameAndPositionIgnoreCase() {
        List<Employee> res = repo.findByDepartmentNameAndPositionIgnoreCase("human resources", "developer");
        assertThat(res).hasSize(3); // barcha employee'larga "Developer" position berilgan
    }

    @Test
    @DisplayName("findEmployeeBySalaryBetween – salary range")
    void findEmployeeBySalaryBetween() {
        List<Employee> res = repo.findEmployeeBySalaryBetween(5500.0, 7500.0);
        assertThat(res)
                .hasSize(2)
                .extracting("firstName")
                .containsExactly("Jane", "Bob");
    }

    @Test
    @DisplayName("findByStatusAndDepartmentName – composite filter")
    void findByStatusAndDepartmentName() {
        List<Employee> res = repo.findByStatusAndDepartmentName(Status.ACTIVE, "IT");
        assertThat(res).isEmpty(); // IT da faqat INACTIVE bor
    }

    @Test
    @DisplayName("findByDepartmentId – simple FK")
    void findByDepartmentId() {
        List<Employee> hrEmps = repo.findByDepartmentId(hr.getId());
        assertThat(hrEmps).hasSize(2);
    }

    @Test
    @DisplayName("findAverageSalaryByDepartmentId – aggregate")
    void findAverageSalaryByDepartmentId() {
        Double avg = repo.findAverageSalaryByDepartmentId(hr.getId());
        assertThat(avg).isEqualTo(5500.0);
    }

    @Test
    @DisplayName("findMaxSalaryByDepartmentId – aggregate")
    void findMaxSalaryByDepartmentId() {
        Double max = repo.findMaxSalaryByDepartmentId(it.getId());
        assertThat(max).isEqualTo(7000.0);
    }

    @Test
    @DisplayName("findMinSalaryByDepartmentId – aggregate")
    void findMinSalaryByDepartmentId() {
        Double min = repo.findMinSalaryByDepartmentId(hr.getId());
        assertThat(min).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("countPositionByDepartmentId – distinct count")
    void countPositionByDepartmentId() {
        long cnt = repo.countPositionByDepartmentId(hr.getId());
        assertThat(cnt).isEqualTo(1L); // faqat "Developer"
    }

    @Test
    @DisplayName("findTopEmployeesBySalary – native query + pagination")
    void findTopEmployeesBySalary() {
        Pageable topTwo = PageRequest.of(0, 2);
        List<Employee> top = repo.findTopEmployeesBySalary(topTwo);
        assertThat(top)
                .hasSize(2)
                .extracting("firstName")
                .containsExactly("Bob", "Jane");
    }

    @Test
    @DisplayName("findByHireDateBetween – date range JPQL")
    void findByHireDateBetween() {
        List<Employee> res = repo.findByHireDateBetween(
                LocalDate.of(2020, 6, 1),
                LocalDate.of(2022, 6, 1));
        assertThat(res).hasSize(2);
    }



    @Test
    @DisplayName("findAll(Pageable) – basic pagination")
    void findAllPageable() {
        Page<Employee> page = repo.findAll(PageRequest.of(0, 2));
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("JpaSpecificationExecutor – dynamic WHERE")
    void specCount() {
        Specification<Employee> spec = (root, query, cb) ->
                cb.equal(root.get("department").get("name"), "IT");

        long cnt = repo.count(spec);
        assertThat(cnt).isEqualTo(1);
    }



    @Test
    @DisplayName("context loads")
    void contextLoads() {
        assertThat(repo).isNotNull();
    }



    private Employee employee(String fn, String ln, Gender g, LocalDate bd, LocalDate hd,
                              Status st, Department dept, String salaryAmount) {
        Employee e = Employee.builder()
                .firstName(fn)
                .lastName(ln)
                .gender(g)
                .birthDate(bd)
                .hireDate(hd)
                .status(st)
                .department(dept)
                .position("Developer")
                .phone("111-222-" + fn)
                .build();

        entityManager.persistAndFlush(e);

        Salary s = Salary.builder()
                .employee(e)
                .amount(Double.parseDouble(salaryAmount))
                .build();

        entityManager.persistAndFlush(s);

        return e;
    }
}