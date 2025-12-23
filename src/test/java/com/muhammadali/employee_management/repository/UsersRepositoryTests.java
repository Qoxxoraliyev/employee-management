package com.muhammadali.employee_management.repository;

import com.muhammadali.employee_management.entity.Role;
import com.muhammadali.employee_management.entity.Users;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
public class UsersRepositoryTests {

    @Container
    static PostgreSQLContainer<?> pg =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("employee_management")
                    .withUsername("mohirdev")
                    .withPassword("123");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
    }

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UsersRepository repo;

    private Users admin;
    private Users user;
    private Users guest;

    @BeforeEach
    public void setUp() {
        Role adminRole = em.persistAndFlush(Role.builder().name("ROLE_ADMIN").build());
        Role userRole  = em.persistAndFlush(Role.builder().name("ROLE_USER").build());
        Role guestRole = em.persistAndFlush(Role.builder().name("ROLE_GUEST").build());

        admin = createUser("admin", "admin@mail.com", adminRole);
        user  = createUser("user", "user@mail.com", userRole);
        guest = createUser("Guest", "guest@mail.com", guestRole);

        em.persistAndFlush(admin);
        em.persistAndFlush(user);
        em.persistAndFlush(guest);
    }


    @Test
    @DisplayName("findByEmail – existing")
    public void findByEmailExisting() {
        Optional<Users> result = repo.findByEmail("admin@mail.com");
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("findByEmail – not found")
    public void findByEmailNotFound() {
        assertThat(repo.findByEmail("missing@mail.com")).isEmpty();
    }


    @Test
    @DisplayName("findByUsername – existing")
    public void findByUsernameExisting() {
        Optional<Users> result = repo.findByUsername("user");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("user@mail.com");
    }

    @Test
    @DisplayName("findByUsername – not found")
    public void findByUsernameNotFound() {
        assertThat(repo.findByUsername("missing")).isEmpty();
    }


    @Test
    @DisplayName("findByUsernameContainingIgnoreCase – partial match")
    public void findByUsernameContainingIgnoreCase() {
        List<Users> users = repo.findByUsernameContainingIgnoreCase("ST");
        assertThat(users)
                .hasSize(1)
                .extracting(Users::getUsername)
                .containsExactly("Guest");
    }


    @Test
    @DisplayName("findByRoleNameIgnoreCase – ADMIN")
    public void findByRoleNameIgnoreCase_Admin() {
        List<Users> admins = repo.findByRoleNameIgnoreCase("ADMIN");
        assertThat(admins)
                .hasSize(1)
                .extracting(Users::getUsername)
                .containsExactly("admin");
    }

    @Test
    @DisplayName("findByRoleNameIgnoreCase – USER")
    public void findByRoleNameIgnoreCase_User() {
        List<Users> users = repo.findByRoleNameIgnoreCase("USER");
        assertThat(users)
                .hasSize(1)
                .extracting(Users::getUsername)
                .containsExactly("user");
    }



    @Test
    @DisplayName("context loads")
    public void contextLoads() {
        assertThat(repo).isNotNull();
    }


    @Test
    @DisplayName("findByEmail – case insensitive")
    public void findByEmailCaseInsensitive() {
        Optional<Users> result = repo.findByEmail("ADMIN@MAIL.COM");
        assertThat(result).isEmpty();
    }

    private Users createUser(String username, String email, Role role) {
        return Users.builder()
                .username(username)
                .email(email)
                .password("ENC")
                .role(role)
                .status(Status.ACTIVE)
                .build();
    }
}
