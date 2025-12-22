package com.muhammadali.employee_management.repository;

import com.muhammadali.employee_management.dto.SalaryMonthlyStatDTO;
import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.Salary;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@Testcontainers
public class SalaryRepositoryTests {


    @Container
    static final PostgreSQLContainer<?> pg =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("employee_management")
                    .withUsername("mohirdev")
                    .withPassword("123");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }


    @Autowired
    private TestEntityManager em;

    @Autowired
    private SalaryRepository repo;

    private Department hr, it;
    private Employee john, jane, bob;

    @BeforeEach
    void setUp() {
        hr = Department.builder().name("HR").managerId(1).build();
        it = Department.builder().name("IT").managerId(2).build();
        em.persistAndFlush(hr);
        em.persistAndFlush(it);

        john = employee("John", hr);
        jane = employee("Jane", hr);
        bob  = employee("Bob", it);

        salary(john, 5000d, 500d, date(2023, 1, 15));
        salary(jane, 6000d, 600d, date(2023, 2, 15));
        salary(bob,  7000d, null, date(2023, 3, 15));
        salary(john, 5500d, 550d, date(2023, 4, 15));
    }

    @Test
    @DisplayName("findByEmployee_IdOrderByPaymentDate")
    void findByEmployeeIdOrderByPaymentDate() {
        List<Salary> res =
                repo.findByEmployee_IdOrderByPaymentDate(john.getId());

        assertThat(res)
                .hasSize(2)
                .extracting("amount", "bonus")
                .containsExactly(
                        tuple(5000d, 500d),
                        tuple(5500d, 550d)
                );
    }

    @Test
    @DisplayName("getMonthlySalaryStats")
    void getMonthlySalaryStats() {
        List<SalaryMonthlyStatDTO> stats =
                repo.getMonthlySalaryStats(2023);

        assertThat(stats).hasSize(4);
        assertThat(stats.get(2))
                .extracting("year", "month", "total")
                .containsExactly(2023, 3, new BigDecimal("7000.00"));
    }

    @Test
    void contextLoads() {
        assertThat(repo).isNotNull();
    }

    private Employee employee(String name, Department dept) {
        Employee e = Employee.builder()
                .firstName(name)
                .lastName("Doe")
                .department(dept)
                .status(Status.ACTIVE)
                .build();
        em.persistAndFlush(e);
        return e;
    }

    private void salary(Employee e, double amount, Double bonus, Date payDate) {
        Salary s = Salary.builder()
                .employee(e)
                .amount(amount)
                .bonus(bonus)
                .paymentDate(payDate)
                .build();
        em.persistAndFlush(s);
    }

    private static Date date(int y, int m, int d) {
        return Date.from(
                LocalDate.of(y, m, d)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
    }
}
