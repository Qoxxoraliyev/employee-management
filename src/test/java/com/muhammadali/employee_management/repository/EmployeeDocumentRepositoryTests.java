package com.muhammadali.employee_management.repository;

import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.EmployeeDocument;
import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
public class EmployeeDocumentRepositoryTests {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("employee_management")
                    .withUsername("mohirdev")
                    .withPassword("123");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EmployeeDocumentRepository repository;

    private Employee john;
    private Employee jane;

    @BeforeEach
    void setUp() {
        Department hr = Department.builder()
                .name("HR")
                .managerId(1)
                .build();
        em.persistAndFlush(hr);

        john = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .hireDate(LocalDate.of(2020, 1, 1))
                .status(Status.ACTIVE)
                .department(hr)
                .build();

        jane = Employee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1992, 2, 2))
                .hireDate(LocalDate.of(2021, 1, 1))
                .status(Status.ACTIVE)
                .department(hr)
                .build();

        em.persistAndFlush(john);
        em.persistAndFlush(jane);

        em.persistAndFlush(doc(john, "PASSPORT", "passport.pdf"));
        em.persistAndFlush(doc(john, "CONTRACT", "contract.pdf"));
        em.persistAndFlush(doc(jane, "RESUME", "resume.pdf"));
    }


    @Test
    @DisplayName("findByEmployee_Id returns all documents of employee")
    void findByEmployeeId() {
        List<EmployeeDocument> docs =
                repository.findByEmployee_Id(john.getId());

        assertThat(docs)
                .hasSize(2)
                .extracting(EmployeeDocument::getFileName)
                .containsExactlyInAnyOrder("passport.pdf", "contract.pdf");
    }

    @Test
    @DisplayName("findByEmployee_IdAndFileCategory filters correctly")
    void findByEmployeeIdAndFileCategory() {
        List<EmployeeDocument> contracts =
                repository.findByEmployee_IdAndFileCategory(john.getId(), "CONTRACT");

        assertThat(contracts)
                .hasSize(1)
                .extracting(EmployeeDocument::getFileName)
                .containsExactly("contract.pdf");

        List<EmployeeDocument> empty =
                repository.findByEmployee_IdAndFileCategory(jane.getId(), "CONTRACT");

        assertThat(empty).isEmpty();
    }

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
    }

    private EmployeeDocument doc(Employee employee, String category, String fileName) {
        return EmployeeDocument.builder()
                .employee(employee)
                .fileCategory(category)
                .fileName(fileName)
                .fileType("application/pdf")
                .build();
    }
}
