package com.muhammadali.employee_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muhammadali.employee_management.dto.*;
import com.muhammadali.employee_management.exceptions.DepartmentHasEmployeeException;
import com.muhammadali.employee_management.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
public class DepartmentControllerTests {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;

    @MockBean
    public DepartmentService departmentService;

    public DepartmentRequestDTO requestDTO;
    public DepartmentResponseDTO responseDTO;
    public DepartmentEmployeeCountDTO countDTO;
    public DepartmentYearlyStatsDTO yearlyDTO;

    @BeforeEach
    public void setUp() {
        requestDTO = new DepartmentRequestDTO("IT", 1);
        responseDTO = new DepartmentResponseDTO(1L, "IT", 1);
        countDTO = new DepartmentEmployeeCountDTO(1L, "IT", 10L);
        yearlyDTO = new DepartmentYearlyStatsDTO() {
            @Override
            public Integer getYear() {
                return 2024;
            }

            @Override
            public Long getEmployeeCount() {
                return 15L;
            }
        };
    }

    @Test
    @DisplayName("POST /api/departments/create – success")
    public void save_success() throws Exception {
        when(departmentService.save(requestDTO)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/departments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("IT"));

        verify(departmentService).save(requestDTO);
    }

    @Test
    @DisplayName("GET /api/departments/search – success")
    public void searchByName_success() throws Exception {
        when(departmentService.searchByName("IT")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/departments/search").param("name", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("IT"));

        verify(departmentService).searchByName("IT");
    }

    @Test
    @DisplayName("GET /api/departments/stats/yearly – success")
    public void getYearlyStats_success() throws Exception {
        when(departmentService.getYearlyStats()).thenReturn(List.of(yearlyDTO));

        mockMvc.perform(get("/api/departments/stats/yearly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2024))
                .andExpect(jsonPath("$[0].employeeCount").value(15));

        verify(departmentService).getYearlyStats();
    }

    @Test
    @DisplayName("PUT /api/departments/update/{id} – success")
    public void update_success() throws Exception {
        when(departmentService.update(1L, requestDTO)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/departments/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IT"));

        verify(departmentService).update(1L, requestDTO);
    }

    @Test
    @DisplayName("GET /api/departments/manager/{managerId} – success")
    public void getDepartmentsByManager_success() throws Exception {
        when(departmentService.getByManager(1)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/departments/manager/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].managerId").value(1));

        verify(departmentService).getByManager(1);
    }

    @Test
    @DisplayName("GET /api/departments/created – success with range")
    public void getByCreationDate_success() throws Exception {
        when(departmentService.getByCreationDateRange("2023-01-01", "2023-12-31"))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/departments/created")
                        .param("from", "2023-01-01")
                        .param("to", "2023-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("IT"));

        verify(departmentService).getByCreationDateRange("2023-01-01", "2023-12-31");
    }

    @Test
    @DisplayName("GET /api/departments/all – success")
    public void getAll_success() throws Exception {
        when(departmentService.getAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/departments/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("IT"));

        verify(departmentService).getAll();
    }

    @Test
    @DisplayName("GET /api/departments/{id}/stats – MIN_SALARY")
    public void getDepartmentStats_minSalary() throws Exception {
        when(departmentService.getMinSalary(1L)).thenReturn(3000.0);

        mockMvc.perform(get("/api/departments/1/stats").param("type", "MIN_SALARY"))
                .andExpect(status().isOk())
                .andExpect(content().string("3000.0"));

        verify(departmentService).getMinSalary(1L);
    }

    @Test
    @DisplayName("GET /api/departments/{id}/stats – invalid type")
    public void getDepartmentStats_invalidType() throws Exception {
        mockMvc.perform(get("/api/departments/1/stats").param("type", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid type provided: INVALID"));

        verifyNoInteractions(departmentService);
    }

    @Test
    @DisplayName("GET /api/departments/employee-count – success")
    public void getEmployeeCountForAllDepartments_success() throws Exception {
        when(departmentService.getEmployeeCountForAllDepartments()).thenReturn(List.of(countDTO));

        mockMvc.perform(get("/api/departments/employee-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departmentName").value("IT"))
                .andExpect(jsonPath("$[0].employeeCount").value(10));

        verify(departmentService).getEmployeeCountForAllDepartments();
    }

    @Test
    @DisplayName("DELETE /api/departments/delete/{id} – success")
    public void delete_success() throws Exception {
        mockMvc.perform(delete("/api/departments/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Department successfully deleted"));

        verify(departmentService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/departments/delete/{id} – department has employees")
    public void delete_departmentHasEmployees() throws Exception {
        doThrow(new DepartmentHasEmployeeException(
                "Cannot delete department with existing employees",
                "DEPARTMENT_HAS_EMPLOYEES",
                "Employee count: 5"))
                .when(departmentService).delete(1L);

        mockMvc.perform(delete("/api/departments/delete/1"))
                .andExpect(status().isConflict());

        verify(departmentService).delete(1L);
    }
}
