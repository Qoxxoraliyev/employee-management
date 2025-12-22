package com.muhammadali.employee_management.repository;

import com.muhammadali.employee_management.entity.Role;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
public class RoleRepositoryTests {


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
    private RoleRepository repository;


    @Test
    @DisplayName("save – persists and returns with id")
    public void save() {
        Role admin = Role.builder().name("ADMIN").build();
        Role saved = repository.save(admin);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("findById – returns entity when exists")
    public void findById() {
        Role user = em.persistFlushFind(Role.builder().name("USER").build());
        Optional<Role> found = repository.findById(user.getId());
        assertThat(found).isPresent().hasValueSatisfying(r -> assertThat(r.getName()).isEqualTo("USER"));
    }

    @Test
    @DisplayName("findAll – returns all roles")
    public void findAll() {
        em.persist(Role.builder().name("ADMIN").build());
        em.persist(Role.builder().name("USER").build());
        em.flush();
        List<Role> all = repository.findAll();
        assertThat(all).hasSize(2).extracting("name").containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("delete – removes entity")
    public void delete() {
        Role role = em.persistFlushFind(Role.builder().name("TEMP").build());
        repository.delete(role);
        assertThat(repository.findById(role.getId())).isEmpty();
    }

    @Test
    @DisplayName("count – returns correct number")
    public void count() {
        em.persist(Role.builder().name("GUEST").build());
        em.flush();
        assertThat(repository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("existsById – true when present")
    public void existsById() {
        Role role = em.persistFlushFind(Role.builder().name("TEST").build());
        assertThat(repository.existsById(role.getId())).isTrue();
        assertThat(repository.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("context loads")
    public void contextLoads() {
        assertThat(repository).isNotNull();
    }
}
