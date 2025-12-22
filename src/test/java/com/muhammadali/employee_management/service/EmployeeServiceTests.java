package com.muhammadali.employee_management.service;

import com.muhammadali.employee_management.dto.EmployeeRequestDTO;
import com.muhammadali.employee_management.dto.EmployeeResponseDTO;
import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;
import com.muhammadali.employee_management.exceptions.BusinessValidationException;
import com.muhammadali.employee_management.exceptions.ResourceNotFoundException;
import com.muhammadali.employee_management.repository.DepartmentRepository;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTests {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService service;

    private Department hr, it;
    private Employee john, jane;

    @BeforeEach
    void setUp() {
        hr = Department.builder().id(1L).name("HR").managerId(1).build();
        it = Department.builder().id(2L).name("IT").managerId(2).build();

        john = Employee.builder().id(1L).firstName("John").lastName("Doe")
                .gender(Gender.MALE).birthDate(LocalDate.now().minusYears(30))
                .hireDate(LocalDate.now().minusDays(30)).status(Status.ACTIVE)
                .department(hr).position("Developer").build();

        jane = Employee.builder().id(2L).firstName("Jane").lastName("Smith")
                .gender(Gender.FEMALE).birthDate(LocalDate.now().minusYears(25))
                .hireDate(LocalDate.now().minusDays(10)).status(Status.INACTIVE)
                .department(it).position("Developer").build();
    }



    @Test
    void save_success() {
        EmployeeRequestDTO dto = buildDto("Alice", "Wonder", Gender.FEMALE, LocalDate.now().minusYears(28), LocalDate.now(), Status.ACTIVE, 1L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(hr));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> {
            Employee e = i.getArgument(0);
            e.setId(3L);
            return e;
        });

        EmployeeResponseDTO res = service.save(dto);

        assertThat(res.id()).isEqualTo(3L);
        assertThat(res.fullName()).isEqualTo("Alice Wonder");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void save_departmentNotFound() {
        EmployeeRequestDTO dto = buildDto("A", "B", Gender.MALE, LocalDate.now().minusYears(20), LocalDate.now(), Status.ACTIVE, 999L);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.save(dto)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_success() {
        EmployeeRequestDTO dto = buildDto("Johnny", "Doe", Gender.MALE, john.getBirthDate(), john.getHireDate(), Status.ACTIVE, it.getId());

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(john));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(it));
        when(employeeRepository.save(any(Employee.class))).thenReturn(john);

        EmployeeResponseDTO res = service.update(1L, dto);

        assertThat(res.fullName()).isEqualTo("Johnny Doe");
        assertThat(res.departmentName()).isEqualTo("IT");
    }

    @Test
    void update_notFound() {
        EmployeeRequestDTO dto = buildDto("x", "y", Gender.MALE, LocalDate.now().minusYears(20), LocalDate.now(), Status.ACTIVE, hr.getId());
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(999L, dto)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_success() {
        when(employeeRepository.existsById(1L)).thenReturn(true).thenReturn(false);
        doNothing().when(employeeRepository).deleteById(1L);

        assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();
        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void delete_notFound() {
        when(employeeRepository.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(999L)).isInstanceOf(IllegalArgumentException.class);
    }



    @Test
    void findAll_mapsToDTO() {
        when(employeeRepository.findAll()).thenReturn(List.of(john, jane));
        List<EmployeeResponseDTO> res = service.findAll();
        assertThat(res).hasSize(2);
    }

    @Test
    void getEmployeesByStatus() {
        when(employeeRepository.findByStatus(Status.ACTIVE)).thenReturn(List.of(john));
        List<EmployeeResponseDTO> res = service.getEmployeesStatus(Status.ACTIVE);
        assertThat(res).hasSize(1).extracting("fullName").containsExactly("John Doe");
    }

    @Test
    void getEmployeeByDepartment() {
        when(employeeRepository.findByDepartmentName("HR")).thenReturn(List.of(john));
        List<EmployeeResponseDTO> res = service.getEmployeeByDepartment("HR");
        assertThat(res).hasSize(1);
    }

    @Test
    void searchByName() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("smith", "smith")).thenReturn(List.of(jane));
        List<EmployeeResponseDTO> res = service.searchByName("smith");
        assertThat(res).hasSize(1);
    }

    @Test
    void searchByPhone() {
        when(employeeRepository.findByPhoneContainingIgnoreCase("000")).thenReturn(List.of(john, jane));
        List<EmployeeResponseDTO> res = service.searchByPhone("000");
        assertThat(res).hasSize(2);
    }

    @Test
    void searchByGender() {
        when(employeeRepository.findByGender(Gender.MALE)).thenReturn(List.of(john));
        List<EmployeeResponseDTO> res = service.searchByGender(Gender.MALE);
        assertThat(res).hasSize(1);
    }

    @Test
    void searchByBirthDateBetween_valid() {
        LocalDate from = LocalDate.now().minusYears(31);
        LocalDate to = LocalDate.now().minusYears(24);
        when(employeeRepository.findByBirthDateBetween(from, to)).thenReturn(List.of(john, jane));
        List<EmployeeResponseDTO> res = service.searchByBirthDateBetween(from, to);
        assertThat(res).hasSize(2);
    }

    @Test
    void searchByBirthDateBetween_invalid() {
        LocalDate from = LocalDate.now().minusYears(20);
        LocalDate to = LocalDate.now().minusYears(30);
        assertThatThrownBy(() -> service.searchByBirthDateBetween(from, to)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchBySalaryRange_valid() {
        when(employeeRepository.findEmployeeBySalaryBetween(4500.0, 7500.0)).thenReturn(List.of(john, jane));
        List<EmployeeResponseDTO> res = service.searchBySalaryRange(4500.0, 7500.0);
        assertThat(res).hasSize(2);
    }

    @Test
    void searchBySalaryRange_invalid() {
        assertThatThrownBy(() -> service.searchBySalaryRange(8000.0, 4000.0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchByHireDateRange() {
        LocalDate from = LocalDate.now().minusDays(40);
        LocalDate to = LocalDate.now();
        when(employeeRepository.findByHireDateBetween(from, to)).thenReturn(List.of(john, jane));
        List<EmployeeResponseDTO> res = service.searchByHireDateRange(from, to);
        assertThat(res).hasSize(2);
    }

    @Test
    void filterByStatusAndDepartment() {
        when(employeeRepository.findByStatusAndDepartmentName(Status.ACTIVE, "HR")).thenReturn(List.of(john));
        List<EmployeeResponseDTO> res = service.filterByStatusAndDepartment(Status.ACTIVE, "HR");
        assertThat(res).hasSize(1);
    }

    @Test
    void filterByDepartmentAndPosition_ignoreCase() {
        when(employeeRepository.findByDepartmentNameAndPositionIgnoreCase("hr", "developer")).thenReturn(List.of(john));
        List<EmployeeResponseDTO> res = service.filterByDepartmentAndPosition("hr", "developer");
        assertThat(res).hasSize(1);
    }



    @Test
    void getEmployeesWithPagingAndSorting() {
        Page<Employee> page = new PageImpl<>(List.of(john));
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<EmployeeResponseDTO> res = service.getEmployeesWithPagingAndSorting(0, 1, "firstName", "asc");
        assertThat(res.getTotalElements()).isEqualTo(1);
    }




    @Test
    void getTotalEmployees() {
        when(employeeRepository.count()).thenReturn(2L);
        assertThat(service.getTotalEmployees()).isEqualTo(2);
    }

    @Test
    void getActiveEmployeePercentage() {
        when(employeeRepository.count()).thenReturn(2L);
        when(employeeRepository.findByStatus(Status.ACTIVE)).thenReturn(List.of(john));
        assertThat(service.getActiveEmployeePercentage()).isEqualTo(50.0);
    }

    @Test
    void countNewEmployeesLast30Days() {
        when(employeeRepository.countByHireDateAfter(any(LocalDate.class))).thenReturn(2L);
        assertThat(service.countNewEmployeesLast30Days()).isEqualTo(2);
    }



    @Test
    void calculateAge_success() {
        int age = service.calculateAge(LocalDate.now().minusYears(30));
        assertThat(age).isEqualTo(30);
    }


    @Test
    void calculateAge_nullBirthDate() {
        assertThatThrownBy(() -> service.calculateAge(null)).isInstanceOf(BusinessValidationException.class);
    }


    @Test
    void calculateAge_futureDate() {
        assertThatThrownBy(() -> service.calculateAge(LocalDate.now().plusDays(1))).isInstanceOf(BusinessValidationException.class);
    }


    private EmployeeRequestDTO buildDto(String fn, String ln, Gender g, LocalDate bd, LocalDate hd, Status st, Long deptId) {
        return new EmployeeRequestDTO(fn, ln, "000", g, bd, hd, "Developer", deptId, null, st
        );
    }



}
