package com.muhammadali.employee_management.service;
import com.muhammadali.employee_management.dto.SalaryMonthlyStatDTO;
import com.muhammadali.employee_management.dto.SalaryRequestDTO;
import com.muhammadali.employee_management.dto.SalaryResponseDTO;
import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.Salary;
import com.muhammadali.employee_management.exceptions.BusinessValidationException;
import com.muhammadali.employee_management.exceptions.InvalidDateRangeException;
import com.muhammadali.employee_management.exceptions.ResourceNotFoundException;
import com.muhammadali.employee_management.mapper.SalaryMapper;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import com.muhammadali.employee_management.repository.SalaryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalaryServiceTests {

    @Mock public SalaryRepository salaryRepository;
    @Mock public EmployeeRepository employeeRepository;
    @Mock public SalaryMapper salaryMapper;
    @Captor public ArgumentCaptor<Salary> salaryCaptor;

    @InjectMocks public SalaryService service;

    public Employee employee;
    public Department department;
    public Salary salary;
    public SalaryRequestDTO dto;

    @BeforeEach
    public void setUp() {
        department = Department.builder().id(1L).name("IT").build();
        employee = Employee.builder().id(10L).firstName("John").department(department).build();

        dto = new SalaryRequestDTO(10L, 5000.0, "USD", new Date(), 200.0);

        salary = Salary.builder()
                .id(1L)
                .amount(5000.0)
                .bonus(200.0)
                .currency("USD")
                .employee(employee)
                .paymentDate(new Date())
                .build();
    }



    @Test
    public void save_success() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(salaryRepository.save(any(Salary.class))).thenReturn(salary);

        SalaryResponseDTO response = service.save(dto);

        verify(salaryRepository).save(salaryCaptor.capture());
        Salary saved = salaryCaptor.getValue();
        assertThat(saved.getAmount()).isEqualTo(5000.0);
        assertThat(saved.getEmployee()).isEqualTo(employee);
    }

    @Test
    public void save_employeeNotFound() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.save(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id : '10'");
        verify(salaryRepository, never()).save(any());
    }



    @Test
    public void update_success() {
        SalaryRequestDTO updateDto = new SalaryRequestDTO(10L, 6000.0, "EUR", new Date(), 300.0);
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(salary));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));

        service.update(1L, updateDto);

        verify(salaryRepository).save(salary);
        assertThat(salary.getAmount()).isEqualTo(6000.0);
        assertThat(salary.getCurrency()).isEqualTo("EUR");
        assertThat(salary.getBonus()).isEqualTo(300.0);
    }

    @Test
    public void update_salaryNotFound() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Salary not found with id : '1'");
    }



    @Test
    public void findAll_success() {
        when(salaryRepository.findAll()).thenReturn(List.of(salary));
        List<SalaryResponseDTO> list = service.findAll();
        assertThat(list).hasSize(1);
        verify(salaryRepository).findAll();
    }



    @Test
    public void getSalariesByDepartment_success() {
        when(salaryRepository.findByEmployee_Department_IdOrderByPaymentDateDesc(1L))
                .thenReturn(List.of(salary));
        List<SalaryResponseDTO> res = service.getSalariesByDepartment(1L);
        assertThat(res).hasSize(1);
    }

    @Test
    public void getSalariesByDepartment_empty() {
        when(salaryRepository.findByEmployee_Department_IdOrderByPaymentDateDesc(1L))
                .thenReturn(List.of());
        assertThatThrownBy(() -> service.getSalariesByDepartment(1L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("This department has no salary records.");
    }



    @Test
    public void findByAmountRange_success() {
        when(salaryRepository.findByAmountBetween(4000.0, 6000.0))
                .thenReturn(List.of(salary));
        List<SalaryResponseDTO> res = service.findByAmountRange(4000.0, 6000.0);
        assertThat(res).hasSize(1);
    }

    @Test
    public void findByAmountRange_nullMin() {
        assertThatThrownBy(() -> service.findByAmountRange(null, 6000.0))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    public void findByAmountRange_minGreaterThanMax() {
        assertThatThrownBy(() -> service.findByAmountRange(7000.0, 6000.0))
                .isInstanceOf(InvalidDateRangeException.class);
    }



    @Test
    public void getMaxSalary_repositoryValue() {
        when(salaryRepository.findMaxSalary()).thenReturn(9000.0);
        assertThat(service.getMaxSalary()).isEqualTo(9000.0);
    }

    @Test
    public void getMaxSalary_repositoryNull() {
        when(salaryRepository.findMaxSalary()).thenReturn(null);
        assertThat(service.getMaxSalary()).isEqualTo(0.0);
    }

    @Test
    public void getMinSalary_repositoryValue() {
        when(salaryRepository.findMinSalary()).thenReturn(3000.0);
        assertThat(service.getMinSalary()).isEqualTo(3000.0);
    }

    @Test
    public void getMinSalary_repositoryNull() {
        when(salaryRepository.findMinSalary()).thenReturn(null);
        assertThat(service.getMinSalary()).isEqualTo(0.0);
    }



    @Test
    public void getTop10HighestBonus_success() {
        when(salaryRepository.findTop10ByOrderByBonusDesc()).thenReturn(List.of(salary));
        List<SalaryResponseDTO> res = service.getTop10HighestBonus();
        assertThat(res).hasSize(1);
    }

    @Test
    public void getTop10HighestSalaries_success() {
        when(salaryRepository.findTop10ByOrderByAmountDesc()).thenReturn(List.of(salary));
        List<SalaryResponseDTO> res = service.getTop10HighestSalaries();
        assertThat(res).hasSize(1);
    }



    @Test
    public void getMonthlySalaryStats_withYear() {
        SalaryMonthlyStatDTO stat = new SalaryMonthlyStatDTO(2024, 1, 15000.0);
        when(salaryRepository.getMonthlySalaryStats(2024)).thenReturn(List.of(stat));
        List<SalaryMonthlyStatDTO> res = service.getMonthlySalaryStats(2024);
        assertThat(res).hasSize(1);
        verify(salaryRepository).getMonthlySalaryStats(2024);
    }

    @Test
    public void getMonthlySalaryStats_defaultYear() {
        int currentYear = java.time.Year.now().getValue();
        when(salaryRepository.getMonthlySalaryStats(currentYear)).thenReturn(List.of());
        service.getMonthlySalaryStats(null);
        verify(salaryRepository).getMonthlySalaryStats(currentYear);
    }



    @Test
    public void getMaxSalaryByEmployee_success() {
        when(salaryRepository.findMaxSalaryByEmployeeId(10L)).thenReturn(7000.0);
        assertThat(service.getMaxSalaryByEmployee(10L)).isEqualTo(7000.0);
    }

    @Test
    public void getMaxSalaryByDepartment_success() {
        when(salaryRepository.findMaxSalaryByDepartmentId(1L)).thenReturn(8000.0);
        assertThat(service.getMaxSalaryByDepartment(1L)).isEqualTo(8000.0);
    }



    @Test
    public void findWithBonus_success() {
        when(salaryRepository.findAllWithPositiveBonus()).thenReturn(List.of(salary));
        List<SalaryResponseDTO> res = service.findWithBonus();
        assertThat(res).hasSize(1);
    }

    @Test
    public void findWithoutBonus_success() {
        when(salaryRepository.findByBonusIsNull()).thenReturn(List.of(salary));
        List<SalaryResponseDTO> res = service.findWithoutBonus();
        assertThat(res).hasSize(1);
    }



    @Test
    public void getSalariesByDateRange_success() {
        Date start = new Date();
        Date end = new Date();
        when(salaryRepository.findByPaymentDateBetween(start, end)).thenReturn(List.of(salary));
        List<SalaryResponseDTO> res = service.getSalariesByDateRange(start, end);
        assertThat(res).hasSize(1);
    }



    @Test
    public void getSalaryHistoryByEmployee_success() {
        when(employeeRepository.existsById(10L)).thenReturn(true);
        when(salaryRepository.findByEmployee_IdOrderByPaymentDate(10L)).thenReturn(List.of(salary));
        List<SalaryResponseDTO> res = service.getSalaryHistoryByEmployee(10L);
        assertThat(res).hasSize(1);
    }

    @Test
    public void getSalaryHistoryByEmployee_notFound() {
        when(employeeRepository.existsById(10L)).thenReturn(false);
        assertThatThrownBy(() -> service.getSalaryHistoryByEmployee(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found with id= 10");
    }



    @Test
    public void addBonus_success() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.of(salary));
        service.addBonus(1L, 100.0);
        assertThat(salary.getBonus()).isEqualTo(300.0); // 200 + 100
    }

    @Test
    public void addBonus_negative() {
        assertThatThrownBy(() -> service.addBonus(1L, -50.0))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Bonus amount must be positive");
    }

    @Test
    public void addBonus_salaryNotFound() {
        when(salaryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.addBonus(1L, 100.0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Salary not found with id : '1'");
    }



    @Test
    public void getAverageSalary_success() {
        when(salaryRepository.findAverageSalaryByEmployeeId(10L)).thenReturn(5500.0);
        assertThat(service.getAverageSalary(10L)).isEqualTo(5500.0);
    }



    @Test
    public void delete_success() {
        when(salaryRepository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(salaryRepository).deleteById(1L);
    }

    @Test
    public void delete_notFound() {
        when(salaryRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Salary not found with id : '1'");
    }
}
