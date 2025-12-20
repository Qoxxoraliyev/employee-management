package com.muhammadali.employee_management.repository;
import com.muhammadali.employee_management.dto.DepartmentEmployeeCountDTO;
import com.muhammadali.employee_management.dto.DepartmentYearlyStatsDTO;
import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
public class DepartmentRepositoryTests {


    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("employee_management")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @Autowired
    private TestEntityManager em;
    @Autowired
    private DepartmentRepository departmentRepository;
    private Department hr, it;
    private Employee john, jane, bob;
    @BeforeEach
    void setUp() {
        hr = Department.builder()
                .name("Human Resources")
                .managerId(1)
                .createdAt(now())
                .build();
        it = Department.builder()
                .name("Information Technology")
                .managerId(2)
                .createdAt(now())
                .build();
        em.persist(hr);
        em.persist(it);
        john = Employee.builder().firstName("John").department(hr).created_at(now()).build();
        jane = Employee.builder().firstName("Jane").department(hr).created_at(now()).build();
        bob  = Employee.builder().firstName("Bob").department(it).created_at(now()).build();
        em.persist(john);
        em.persist(jane);
        em.persist(bob);
        em.flush();
    }


    @Test
    @DisplayName("findByNameContainingIgnoreCase")
    void findByNameContainingIgnoreCase() {
        List<Department> result =
                departmentRepository.findByNameContainingIgnoreCase("resources");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Human Resources");
    }


    @Test
    @DisplayName("findByManagerId")
    void findByManagerId() {
        List<Department> result =
                departmentRepository.findByManagerId(2);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Information Technology");
    }


    @Test
    @DisplayName("findByCreatedAtBetween")
    void findByCreatedAtBetween() {
        Timestamp from = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp to   = Timestamp.valueOf(LocalDateTime.now().plusDays(1));

        List<Department> result =
                departmentRepository.findByCreatedAtBetween(from, to);

        assertThat(result).hasSize(2);
    }


    @Test
    @DisplayName("getDepartmentEmployeeCounts")
    void getDepartmentEmployeeCounts() {
        List<DepartmentEmployeeCountDTO> result =
                departmentRepository.getDepartmentEmployeeCounts();
        assertThat(result).hasSize(2);
        assertThat(result)
                .filteredOn(d -> d.departmentName().equals("Human Resources"))
                .extracting(DepartmentEmployeeCountDTO::employeeCount)
                .containsExactly(2L);
    }


    @Test
    @DisplayName("getGlobalYearlyHires")
    void getGlobalYearlyHires() {
        List<DepartmentYearlyStatsDTO> result =
                departmentRepository.getGlobalYearlyHires();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("getYearlyHiresByDepartment")
    void getYearlyHiresByDepartment() {
        List<DepartmentYearlyStatsDTO> result =
                departmentRepository.getYearlyHiresByDepartment(hr.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeCount()).isEqualTo(2);
    }

    @Test
    void contextLoads() {
        assertThat(departmentRepository).isNotNull();
    }

    private static Timestamp now() {
        return Timestamp.valueOf(LocalDateTime.now());
    }
}
