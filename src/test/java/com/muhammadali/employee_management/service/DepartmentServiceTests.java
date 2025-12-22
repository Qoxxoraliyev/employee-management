package com.muhammadali.employee_management.service;

import com.muhammadali.employee_management.dto.*;
import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.exceptions.*;
import com.muhammadali.employee_management.repository.DepartmentRepository;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import jakarta.validation.constraints.Null;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTests {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentService departmentService;


    @Test
    void save_shouldCreateDepartment() {
        DepartmentRequestDTO dto =
                new DepartmentRequestDTO("IT", 1);

        Department saved = Department.builder()
                .id(1L)
                .name("IT")
                .managerId(1)
                .build();

        when(departmentRepository.save(any()))
                .thenReturn(saved);

        DepartmentResponseDTO res =
                departmentService.save(dto);

        assertThat(res.id()).isEqualTo(1L);
        assertThat(res.name()).isEqualTo("IT");
        assertThat(res.manager_id()).isEqualTo(1);
    }


    @Test
    void update_shouldUpdateDepartment() {
        Department existing = Department.builder()
                .id(1L)
                .name("HR")
                .managerId(1)
                .build();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(departmentRepository.save(any()))
                .thenReturn(existing);

        DepartmentResponseDTO res =
                departmentService.update(1L,
                        new DepartmentRequestDTO("HR-new", 99));

        assertThat(res.name()).isEqualTo("HR-new");
        assertThat(res.manager_id()).isEqualTo(99);
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                departmentService.update(99L,
                        new DepartmentRequestDTO("X", 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    void delete_shouldDeleteWhenNoEmployees() {
        Department dept = Department.builder().id(1L).build();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(dept));

        when(employeeRepository.countByDepartment_Id(1L))
                .thenReturn(0L);

        departmentService.delete(1L);

        verify(departmentRepository).delete(dept);
    }



    @Test
    void delete_shouldThrowWhenEmployeesExist() {
        Department dept = Department.builder().id(1L).build();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(dept));

        when(employeeRepository.countByDepartment_Id(1L))
                .thenReturn(3L);

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(DepartmentHasEmployeeException.class);
    }


    @Test
    void delete_shouldThrowWhenNotFound() {
        when(departmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    void searchByName_shouldReturnList() {
        when(departmentRepository.findByNameContainingIgnoreCase("it"))
                .thenReturn(List.of(
                        Department.builder()
                                .id(1L)
                                .name("IT")
                                .managerId(1)
                                .build()
                ));
        List<DepartmentResponseDTO> res =
                departmentService.searchByName("it");

        assertThat(res).hasSize(1);
    }


    @Test
    void searchByName_blank_shouldReturnAll() {
        when(departmentRepository.findAll())
                .thenReturn(List.of(
                        new Department(),
                        new Department()
                ));

        assertThat(departmentService.searchByName(""))
                .hasSize(2);
    }

    @Test
    void searchByName_shortText_shouldThrow() {
        assertThatThrownBy(() ->
                departmentService.searchByName("x"))
                .isInstanceOf(BusinessValidationException.class);
    }



    @Test
    void getByManager_shouldReturnDepartments() {
        when(departmentRepository.findByManagerId(1))
                .thenReturn(List.of(new Department()));

        assertThat(departmentService.getByManager(1))
                .hasSize(1);
    }

    @Test
    void getByManager_null_shouldThrow() {
        assertThatThrownBy(() ->
                departmentService.getByManager(null))
                .isInstanceOf(BusinessValidationException.class);
    }


    @Test
    void getByCreationDateRange_shouldReturnList() {
        when(departmentRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(List.of(new Department()));

        assertThat(
                departmentService.getByCreationDateRange(
                        "2024-01-01", "2024-12-31"))
                .hasSize(1);
    }

    @Test
    void getByCreationDateRange_invalidDate_shouldThrow() {
        assertThatThrownBy(() ->
                departmentService.getByCreationDateRange("bad", null))
                .isInstanceOf(InvalidDateRangeException.class);
    }


    @Test
    void getAverageSalary_shouldReturnZeroWhenNull() {
        when(employeeRepository.findAverageSalaryByDepartmentId(1L))
                .thenReturn(null);

        assertThat(departmentService.getAverageSalary(1L))
                .isEqualTo(0.0);
    }


    @Test
    void getMaxSalary_shouldReturnValue() {
        when(employeeRepository.findMaxSalaryByDepartmentId(1L))
                .thenReturn(9000.0);

        assertThat(departmentService.getMaxSalary(1L))
                .isEqualTo(9000.0);
    }


    @Test
    void getMinSalary_shouldReturnZeroWhenNull() {
        when(employeeRepository.findMinSalaryByDepartmentId(1L))
                .thenReturn(null);

        assertThat(departmentService.getMinSalary(1L))
                .isEqualTo(0.0);
    }



    @Test
    void getYearlyStats_shouldReturnList() {
        when(departmentRepository.getGlobalYearlyHires())
                .thenReturn(List.of(
                        mock(DepartmentYearlyStatsDTO.class)
                ));

        assertThat(departmentService.getYearlyStats())
                .hasSize(1);
    }



    @Test
    void getYearlyStatsByDepartment_shouldReturnList() {
        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(new Department()));

        when(departmentRepository.getYearlyHiresByDepartment(1L))
                .thenReturn(List.of(
                        mock(DepartmentYearlyStatsDTO.class)
                ));

        assertThat(departmentService.getYearlyStatsByDepartment(1L))
                .hasSize(1);
    }



    @Test
    void getEmployeesByDepartment_shouldReturnList() {
        when(employeeRepository.findByDepartmentId(1L))
                .thenReturn(List.of(new Employee()));

        assertThat(
                departmentService.getEmployeesByDepartment(1L))
                .hasSize(1);
    }


    @Test
    void getEmployeeCountInDepartment_shouldReturnCount() {
        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(new Department()));

        when(employeeRepository.countByDepartment_Id(1L))
                .thenReturn(5L);

        assertThat(
                departmentService.getEmployeeCountInDepartment(1L))
                .isEqualTo(5);
    }


    @Test
    void getEmployeeCountForAllDepartments_shouldReturnDTOs() {
        when(departmentRepository.getDepartmentEmployeeCounts())
                .thenReturn(List.of(
                        mock(DepartmentEmployeeCountDTO.class)
                ));

        assertThat(
                departmentService.getEmployeeCountForAllDepartments())
                .hasSize(1);
    }


}
