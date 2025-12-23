package com.muhammadali.employee_management.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muhammadali.employee_management.dto.SalaryMonthlyStatDTO;
import com.muhammadali.employee_management.dto.SalaryRequestDTO;
import com.muhammadali.employee_management.dto.SalaryResponseDTO;
import com.muhammadali.employee_management.exceptions.*;
import com.muhammadali.employee_management.service.SalaryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Date;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalaryController.class)
public class SalaryControllerTests {

    @Autowired public MockMvc mockMvc;
    @Autowired public ObjectMapper objectMapper;

    @MockBean public SalaryService salaryService;

    public SalaryRequestDTO requestDTO;
    public SalaryResponseDTO responseDTO;
    public SalaryMonthlyStatDTO monthlyDTO;

    @BeforeEach
    public void setUp() {
        requestDTO = new SalaryRequestDTO(10L, 5000.0, "USD",
                new Date(), 200.0);
        responseDTO = new SalaryResponseDTO(1L,1L, 5000.0, "USD",
                new Date(), 200.0);
        monthlyDTO = new SalaryMonthlyStatDTO(2024, 1, 15000.0);
    }

    @Test
    public void create_success() throws Exception {
        when(salaryService.save(any(SalaryRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000.0));

        verify(salaryService).save(requestDTO);
    }

    @Test
    public void create_employeeNotFound() throws Exception {
        when(salaryService.save(any(SalaryRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 10L));

        mockMvc.perform(post("/api/salaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());

        verify(salaryService).save(requestDTO);
    }

    @Test
    public void update_success() throws Exception {
        when(salaryService.update(eq(1L), any(SalaryRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/salaries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000.0));

        verify(salaryService).update(1L, requestDTO);
    }

    @Test
    public void update_notFound() throws Exception {
        when(salaryService.update(eq(1L), any(SalaryRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Salary", "id", 1L));

        mockMvc.perform(put("/api/salaries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());

        verify(salaryService).update(1L, requestDTO);
    }

    @Test
    public void getAll_success() throws Exception {
        when(salaryService.findAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/salaries/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(5000.0));

        verify(salaryService).findAll();
    }

    @Test
    public void addBonus_success() throws Exception {
        when(salaryService.addBonus(1L, 100.0)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/salaries/1/bonus")
                        .param("bonus", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bonus").value(200.0));

        verify(salaryService).addBonus(1L, 100.0);
    }

    @Test
    public void addBonus_negative() throws Exception {
        when(salaryService.addBonus(1L, -50.0))
                .thenThrow(new BusinessValidationException("Bonus amount must be positive"));

        mockMvc.perform(put("/api/salaries/1/bonus")
                        .param("bonus", "-50"))
                .andExpect(status().isBadRequest());

        verify(salaryService).addBonus(1L, -50.0);
    }

    @Test
    public void addBonus_salaryNotFound() throws Exception {
        when(salaryService.addBonus(1L, 100.0))
                .thenThrow(new ResourceNotFoundException("Salary", "id", 1L));

        mockMvc.perform(put("/api/salaries/1/bonus")
                        .param("bonus", "100"))
                .andExpect(status().isNotFound());

        verify(salaryService).addBonus(1L, 100.0);
    }

    @Test
    public void getAverageSalary_success() throws Exception {
        when(salaryService.getAverageSalary(10L)).thenReturn(5500.0);

        mockMvc.perform(get("/api/salaries/employee/10/average"))
                .andExpect(status().isOk())
                .andExpect(content().string("5500.0"));

        verify(salaryService).getAverageSalary(10L);
    }

    @Test
    public void getByEmployee_success() throws Exception {
        when(salaryService.getSalaryHistoryByEmployee(10L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/salaries/employee/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"));

        verify(salaryService).getSalaryHistoryByEmployee(10L);
    }

    @Test
    public void getByEmployee_notFound() throws Exception {
        when(salaryService.getSalaryHistoryByEmployee(99L))
                .thenThrow(new RuntimeException("Employee not found with id= 99"));

        mockMvc.perform(get("/api/salaries/employee/99"))
                .andExpect(status().isNotFound());

        verify(salaryService).getSalaryHistoryByEmployee(99L);
    }

    @Test
    public void getByDepartment_success() throws Exception {
        when(salaryService.getSalariesByDepartment(1L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/salaries/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(5000.0));

        verify(salaryService).getSalariesByDepartment(1L);
    }

    @Test
    public void getByDepartment_empty() throws Exception {
        when(salaryService.getSalariesByDepartment(1L))
                .thenThrow(new BusinessValidationException("This department has no salary records."));

        mockMvc.perform(get("/api/salaries/department/1"))
                .andExpect(status().isBadRequest());

        verify(salaryService).getSalariesByDepartment(1L);
    }

    @Test
    public void getByDateRange_success() throws Exception {
        Date start = new Date();
        Date end = new Date();
        when(salaryService.getSalariesByDateRange(any(), any())).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/salaries/range")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(5000.0));

        verify(salaryService).getSalariesByDateRange(any(), any());
    }

    @Test
    public void getMaxSalary_success() throws Exception {
        when(salaryService.getMaxSalary()).thenReturn(9000.0);

        mockMvc.perform(get("/api/salaries/max"))
                .andExpect(status().isOk())
                .andExpect(content().string("9000.0"));

        verify(salaryService).getMaxSalary();
    }

    @Test
    public void getByAmountRange_success() throws Exception {
        when(salaryService.findByAmountRange(4000.0, 6000.0)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/salaries/range/amount")
                        .param("minAmount", "4000")
                        .param("maxAmount", "6000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(5000.0));

        verify(salaryService).findByAmountRange(4000.0, 6000.0);
    }

    @Test
    public void getByAmountRange_invalid() throws Exception {
        when(salaryService.findByAmountRange(7000.0, 6000.0))
                .thenThrow(new InvalidDateRangeException("minAmount cannot be greater than maxAmount"));

        mockMvc.perform(get("/api/salaries/range/amount")
                        .param("minAmount", "7000")
                        .param("maxAmount", "6000"))
                .andExpect(status().isBadRequest());

        verify(salaryService).findByAmountRange(7000.0, 6000.0);
    }

    @Test
    public void getWithBonus_success() throws Exception {
        when(salaryService.findWithBonus()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/salaries/with-bonus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bonus").value(200.0));

        verify(salaryService).findWithBonus();
    }

    @Test
    public void getTop10HighestBonuses_success() throws Exception {
        when(salaryService.getTop10HighestBonus()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/salaries/top-bonus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bonus").value(200.0));

        verify(salaryService).getTop10HighestBonus();
    }

    @Test
    public void getMaxSalaryByDepartment_success() throws Exception {
        when(salaryService.getMaxSalaryByDepartment(1L)).thenReturn(8000.0);

        mockMvc.perform(get("/api/salaries/department/1/max"))
                .andExpect(status().isOk())
                .andExpect(content().string("8000.0"));

        verify(salaryService).getMaxSalaryByDepartment(1L);
    }

    @Test
    public void getTop10HighestSalaries_success() throws Exception {
        when(salaryService.getTop10HighestSalaries()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/salaries/top10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(5000.0));

        verify(salaryService).getTop10HighestSalaries();
    }

    @Test
    public void getMinSalary_success() throws Exception {
        when(salaryService.getMinSalary()).thenReturn(3000.0);

        mockMvc.perform(get("/api/salaries/min"))
                .andExpect(status().isOk())
                .andExpect(content().string("3000.0"));

        verify(salaryService).getMinSalary();
    }

    @Test
    public void getMonthlyStats_withYear() throws Exception {
        when(salaryService.getMonthlySalaryStats(2024)).thenReturn(List.of(monthlyDTO));

        mockMvc.perform(get("/api/salaries/monthly-stats")
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2024));

        verify(salaryService).getMonthlySalaryStats(2024);
    }

    @Test
    public void getMonthlyStats_defaultYear() throws Exception {
        int currentYear = java.time.Year.now().getValue();
        when(salaryService.getMonthlySalaryStats(currentYear)).thenReturn(List.of(monthlyDTO));

        mockMvc.perform(get("/api/salaries/monthly-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(currentYear));

        verify(salaryService).getMonthlySalaryStats(currentYear);
    }

    @Test
    public void getMaxSalaryByEmployee_success() throws Exception {
        when(salaryService.getMaxSalaryByEmployee(10L)).thenReturn(7000.0);

        mockMvc.perform(get("/api/salaries/employee/10/max"))
                .andExpect(status().isOk())
                .andExpect(content().string("7000.0"));

        verify(salaryService).getMaxSalaryByEmployee(10L);
    }

    @Test
    public void getWithoutBonus_success() throws Exception {
        when(salaryService.findWithoutBonus()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/salaries/without-bonus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bonus").value(200.0));

        verify(salaryService).findWithoutBonus();
    }

    @Test
    public void delete_success() throws Exception {
        doNothing().when(salaryService).delete(1L);

        mockMvc.perform(delete("/api/salaries/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("successful"));

        verify(salaryService).delete(1L);
    }

    @Test
    public void delete_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Salary", "id", 1L))
                .when(salaryService).delete(1L);

        mockMvc.perform(delete("/api/salaries/1"))
                .andExpect(status().isNotFound());

        verify(salaryService).delete(1L);
    }
}
