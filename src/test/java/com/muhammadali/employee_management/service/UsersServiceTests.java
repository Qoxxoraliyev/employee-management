package com.muhammadali.employee_management.service;

import com.muhammadali.employee_management.dto.UsersRequestDTO;
import com.muhammadali.employee_management.dto.UsersResponseDTO;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.Role;
import com.muhammadali.employee_management.entity.Users;
import com.muhammadali.employee_management.enums.Status;
import com.muhammadali.employee_management.exceptions.ResourceAlreadyExistsException;
import com.muhammadali.employee_management.exceptions.ResourceNotFoundException;
import com.muhammadali.employee_management.mapper.UsersMapper;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import com.muhammadali.employee_management.repository.RoleRepository;
import com.muhammadali.employee_management.repository.UsersRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsersServiceTests {

    @Mock public UsersRepository usersRepository;
    @Mock public EmployeeRepository employeeRepository;
    @Mock public RoleRepository roleRepository;
    @Mock public PasswordEncoder passwordEncoder;
    @Mock public UsersMapper usersMapper;

    @InjectMocks public UsersService service;

    @Captor public ArgumentCaptor<Users> userCaptor;

    public Employee employee;
    public Role role;
    public Users user;
    public UsersRequestDTO dto;
    public UsersResponseDTO responseDTO;

    @BeforeEach
    public void setUp() {
        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .build();

        role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();


        dto = new UsersRequestDTO(
                "john",
                "secret",
                "john@example.com",
                1L,
                1L,
                Status.ACTIVE
        );


        user = Users.builder()
                .id(10L)
                .username("john")
                .email("john@example.com")
                .password("encoded")
                .role(role)
                .employee(employee)
                .status(Status.ACTIVE)
                .build();

        responseDTO = new UsersResponseDTO(
                10L,
                "john",
                "john@example.com",
                "ADMIN",
                Status.ACTIVE.name()
        );
    }




    @Test
    public void save_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(usersRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(usersRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(usersRepository.save(any(Users.class))).thenReturn(user);
        when(usersMapper.toResponse(user)).thenReturn(responseDTO);

        UsersResponseDTO result = service.save(dto);

        verify(usersRepository).save(userCaptor.capture());
        Users saved = userCaptor.getValue();
        assertThat(saved.getPassword()).isEqualTo("encoded");
        assertThat(saved.getEmployee()).isEqualTo(employee);
        assertThat(saved.getRole()).isEqualTo(role);
        assertThat(result).isEqualTo(responseDTO);
    }

    @Test
    public void save_usernameExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(usersRepository.findByUsername("john")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.save(dto))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("User already exists with username : 'john'");
        verify(usersRepository, never()).save(any());
    }

    @Test
    public void save_emailExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(usersRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(usersRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.save(dto))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("User already exists with email : 'john@example.com'");
        verify(usersRepository, never()).save(any());
    }



    @Test
    public void findByUsernameLike_success() {
        when(usersRepository.findByUsernameContainingIgnoreCase("jo")).thenReturn(List.of(user));
        when(usersMapper.toResponse(user)).thenReturn(responseDTO);

        List<UsersResponseDTO> result = service.findByUsernameLike("jo");

        assertThat(result).hasSize(1).containsExactly(responseDTO);
    }



    @Test
    public void findByUsername_success() {
        when(usersRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(usersMapper.toResponse(user)).thenReturn(responseDTO);

        UsersResponseDTO result = service.findByUsername("john");
        assertThat(result).isEqualTo(responseDTO);
    }

    @Test
    public void findByUsername_notFound() {
        when(usersRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUsername("john"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username : 'john'");
    }



    @Test
    public void update_success() {
        UsersRequestDTO updateDto = new UsersRequestDTO("john2", "john2@example.com", "newPass", 1L, 1L, Status.INACTIVE);
        when(usersRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(usersRepository.findByUsername("john2")).thenReturn(Optional.empty());
        when(usersRepository.findByEmail("john2@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");
        when(usersRepository.save(any(Users.class))).thenReturn(user);
        when(usersMapper.toResponse(user)).thenReturn(responseDTO);

        UsersResponseDTO result = service.update(10L, updateDto);

        verify(usersRepository).save(user);
        assertThat(user.getUsername()).isEqualTo("john2");
        assertThat(user.getEmail()).isEqualTo("john2@example.com");
        assertThat(user.getPassword()).isEqualTo("newEncoded");
        assertThat(user.getStatus()).isEqualTo("INACTIVE");
        assertThat(result).isEqualTo(responseDTO);
    }

    @Test
    public void update_usernameExists() {
        Users other = Users.builder().id(99L).username("john2").build();
        UsersRequestDTO updateDto = new UsersRequestDTO("john2", "john2@example.com", null, 1L, 1L, Status.ACTIVE);
        when(usersRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(usersRepository.findByUsername("john2")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(10L, updateDto))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("User already exists with username : 'john2'");
    }

    @Test
    public void update_emailExists() {
        Users other = Users.builder().id(99L).email("john2@example.com").build();
        UsersRequestDTO updateDto = new UsersRequestDTO("john2", "john2@example.com", null, 1L, 1L, Status.ACTIVE);
        when(usersRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(usersRepository.findByUsername("john2")).thenReturn(Optional.empty());
        when(usersRepository.findByEmail("john2@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(10L, updateDto))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("User already exists with email : 'john2@example.com'");
    }



    @Test
    public void findByRole_success() {
        when(usersRepository.findByRoleNameIgnoreCase("admin")).thenReturn(List.of(user));
        when(usersMapper.toResponse(user)).thenReturn(responseDTO);

        List<UsersResponseDTO> result = service.findByRole("admin");
        assertThat(result).hasSize(1).containsExactly(responseDTO);
    }



    @Test
    public void getTotalUsersCount_success() {
        when(usersRepository.count()).thenReturn(5L);
        assertThat(service.getTotalUsersCount()).isEqualTo(5L);
    }



    @Test
    public void findAll_success() {
        when(usersRepository.findAll()).thenReturn(List.of(user));
        when(usersMapper.toResponse(user)).thenReturn(responseDTO);

        List<UsersResponseDTO> result = service.findAll();
        assertThat(result).hasSize(1).containsExactly(responseDTO);
    }



    @Test
    public void delete_success() {
        when(usersRepository.existsById(10L)).thenReturn(true);
        service.delete(10L);
        verify(usersRepository).deleteById(10L);
    }

    @Test
    public void delete_notFound() {
        when(usersRepository.existsById(10L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id : '10'");
        verify(usersRepository, never()).deleteById(anyLong());
    }
}
