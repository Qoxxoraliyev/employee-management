package com.muhammadali.employee_management.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muhammadali.employee_management.dto.*;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;
import com.muhammadali.employee_management.service.EmployeeService;
import com.muhammadali.employee_management.service.SalaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTests {

    @Autowired public MockMvc mockMvc;
    @Autowired public ObjectMapper objectMapper;

    @MockBean public EmployeeService employeeService;
    @MockBean public SalaryService salaryService;

    public EmployeeRequestDTO requestDTO;
    public EmployeeResponseDTO responseDTO;
    public SalaryResponseDTO salaryDTO;

    @BeforeEach
    public void setUp() {
        requestDTO = new EmployeeRequestDTO(
                "John",
                "Doe",
                "123456",
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1),
                "Developer",
                1L,
                null,
                Status.ACTIVE
        );

        responseDTO = new EmployeeResponseDTO(
                1L,
                "John Doe",
                "123456",
                Gender.MALE,
                LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1),
                "Developer",
                "IT",
                Status.ACTIVE,
                null
        );

        salaryDTO = new SalaryResponseDTO(
                1L,
                1L,
                5000.0,
                "USD",
                new Date(),
                200.0
        );
    }


    @Test
    @DisplayName("POST /api/employees/save – success")
    public void create_success() throws Exception {
        when(employeeService.save(any(EmployeeRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/employees/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(employeeService).save(requestDTO);
    }


    @Test
    @DisplayName("PUT /api/employees/{id} – success")
    public void update_success() throws Exception {
        when(employeeService.update(eq(1L), any(EmployeeRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(employeeService).update(1L, requestDTO);
    }


    @Test
    @DisplayName("GET /api/employees – success")
    public void getAll_success() throws Exception {
        when(employeeService.findAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(employeeService).findAll();
    }


    @Test
    @DisplayName("GET /api/employees/age – success")
    public void searchByAgeRange_success() throws Exception {
        when(employeeService.searchByAgeRange(25, 35)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/age")
                        .param("min", "25")
                        .param("max", "35"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(employeeService).searchByAgeRange(25, 35);
    }


    @Test
    @DisplayName("GET /api/employees/search/status-department – success")
    public void filterByStatusAndDepartment_success() throws Exception {
        when(employeeService.filterByStatusAndDepartment(Status.ACTIVE, "IT"))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/search/status-department")
                        .param("status", "ACTIVE")
                        .param("department", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("IT"));

        verify(employeeService).filterByStatusAndDepartment(Status.ACTIVE, "IT");
    }


    @Test
    @DisplayName("GET /api/employees/top-salary – success")
    public void getTopSalaryEmployees_success() throws Exception {
        when(employeeService.getTop5HighestSalary()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/top-salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(employeeService).getTop5HighestSalary();
    }


    @Test
    @DisplayName("GET /api/employees/hiredate – success")
    public void byHireDate_success() throws Exception {
        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 12, 31);
        when(employeeService.searchByHireDateRange(from, to)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/hiredate")
                        .param("from", "2020-01-01")
                        .param("to", "2020-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(employeeService).searchByHireDateRange(from, to);
    }


    @Test
    @DisplayName("GET /api/employees/filter – success")
    public void filterByDepartmentAndPosition_success() throws Exception {
        when(employeeService.filterByDepartmentAndPosition("IT", "Developer"))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/filter")
                        .param("department", "IT")
                        .param("position", "Developer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value("Developer"));

        verify(employeeService).filterByDepartmentAndPosition("IT", "Developer");
    }


    @Test
    @DisplayName("GET /api/employees/salary – success")
    public void searchBySalaryRange_success() throws Exception {
        when(employeeService.searchBySalaryRange(4000.0, 6000.0)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/salary")
                        .param("min", "4000")
                        .param("max", "6000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(employeeService).searchBySalaryRange(4000.0, 6000.0);
    }


    @Test
    @DisplayName("GET /api/employees/search – success")
    public void searchEmployees_success() throws Exception {
        when(employeeService.searchByName("John")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/search").param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(employeeService).searchByName("John");
    }


    @Test
    @DisplayName("GET /api/employees/search/phone – success")
    public void searchByPhone_success() throws Exception {
        when(employeeService.searchByPhone("123")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/search/phone").param("phone", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].phone").value("123456"));

        verify(employeeService).searchByPhone("123");
    }


    @Test
    @DisplayName("GET /api/employees/search/gender – success")
    public void searchByGender_success() throws Exception {
        when(employeeService.searchByGender(Gender.MALE)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/search/gender").param("gender", "MALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gender").value("MALE"));

        verify(employeeService).searchByGender(Gender.MALE);
    }


    @Test
    @DisplayName("GET /api/employees/birthdate – success")
    public void searchByBirthDateRange_success() throws Exception {
        LocalDate from = LocalDate.of(1990, 1, 1);
        LocalDate to = LocalDate.of(2000, 1, 1);
        when(employeeService.searchByBirthDateBetween(from, to)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/birthdate")
                        .param("from", "1990-01-01")
                        .param("to", "2000-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].birthDate").value("1990-01-01"));

        verify(employeeService).searchByBirthDateBetween(from, to);
    }


    @Test
    @DisplayName("GET /api/employees/status – success")
    public void getByStatus_success() throws Exception {
        when(employeeService.getEmployeesStatus(Status.ACTIVE)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/status").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(employeeService).getEmployeesStatus(Status.ACTIVE);
    }


    @Test
    @DisplayName("GET /api/employees/department/{name} – success")
    public void getByDepartment_success() throws Exception {
        when(employeeService.getEmployeeByDepartment("IT")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/department/IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("IT"));

        verify(employeeService).getEmployeeByDepartment("IT");
    }


    @Test
    @DisplayName("GET /api/employees/{id}/age – success")
    public void getEmployeeAge_success() throws Exception {
        when(employeeService.findById(1L)).thenReturn(
                Employee.builder().id(1L).birthDate(LocalDate.of(1990, 1, 1)).build());
        when(employeeService.calculateAge(LocalDate.of(1990, 1, 1))).thenReturn(34);

        mockMvc.perform(get("/api/employees/1/age"))
                .andExpect(status().isOk())
                .andExpect(content().string("34"));

        verify(employeeService).findById(1L);
        verify(employeeService).calculateAge(LocalDate.of(1990, 1, 1));
    }


    @Test
    @DisplayName("GET /api/employees/paging – success")
    public void getEmployeesPagingAndSorting_success() throws Exception {
        var page = new PageImpl<>(List.of(responseDTO));
        when(employeeService.getEmployeesWithPagingAndSorting(0, 10, "id", "asc"))
                .thenReturn(page);

        mockMvc.perform(get("/api/employees/paging")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("John Doe"));

        verify(employeeService).getEmployeesWithPagingAndSorting(0, 10, "id", "asc");
    }


    @Test
    @DisplayName("GET /api/employees/{employeeId}/salaries – success")
    public void getEmployeeSalaryHistory_success() throws Exception {
        when(salaryService.getSalaryHistoryByEmployee(1L)).thenReturn(List.of(salaryDTO));

        mockMvc.perform(get("/api/employees/1/salaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(5000.0));

        verify(salaryService).getSalaryHistoryByEmployee(1L);
    }


    @Test
    @DisplayName("GET /api/employees/advanced-search – success")
    public void advancedSearch_success() throws Exception {
        when(employeeService.advancedSearch("John", "IT", Status.ACTIVE, 25, 35))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/employees/advanced-search")
                        .param("name", "John")
                        .param("department", "IT")
                        .param("status", "ACTIVE")
                        .param("minAge", "25")
                        .param("maxAge", "35"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(employeeService).advancedSearch("John", "IT", Status.ACTIVE, 25, 35);
    }


    @Test
    @DisplayName("GET /api/employees/count – success")
    public void getEmployeeCount_success() throws Exception {
        when(employeeService.getTotalEmployees()).thenReturn(100L);

        mockMvc.perform(get("/api/employees/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        verify(employeeService).getTotalEmployees();
    }


    @Test
    @DisplayName("GET /api/employees/active-percentage – success")
    public void getActiveEmployeePercentage_success() throws Exception {
        when(employeeService.getActiveEmployeePercentage()).thenReturn(75.5);

        mockMvc.perform(get("/api/employees/active-percentage"))
                .andExpect(status().isOk())
                .andExpect(content().string("75.5"));

        verify(employeeService).getActiveEmployeePercentage();
    }


    @Test
    @DisplayName("GET /api/employees/new-employees/month – success")
    public void getNewEmployeesLastMonth_success() throws Exception {
        when(employeeService.countNewEmployeesLast30Days()).thenReturn(5L);

        mockMvc.perform(get("/api/employees/new-employees/month"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(employeeService).countNewEmployeesLast30Days();
    }


    @Test
    @DisplayName("DELETE /api/employees/{id} – success")
    public void delete_success() throws Exception {
        doNothing().when(employeeService).delete(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} – employee not found")
    public void delete_notFound() throws Exception {
        doThrow(new IllegalArgumentException("Employee not found with id: 1"))
                .when(employeeService).delete(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNotFound());

        verify(employeeService).delete(1L);
    }
}
