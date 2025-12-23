package com.muhammadali.employee_management.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muhammadali.employee_management.dto.EmployeeDocumentDTO;
import com.muhammadali.employee_management.exceptions.*;
import com.muhammadali.employee_management.service.EmployeeDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeDocumentController.class)
public class EmployeeDocumentControllerTests {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;

    @MockBean
    public EmployeeDocumentService documentService;

    public EmployeeDocumentDTO dto;
    public MockMultipartFile file;

    @BeforeEach
    public void setUp() {
        dto = new EmployeeDocumentDTO(
                "report.pdf",
                "application/pdf",
                "HR",
                Timestamp.from(Instant.now())
        );

        file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );
    }




    @Test
    @DisplayName("POST /api/employees/{employeeId}/documents/upload – success")
    public void uploadDocument_success() throws Exception {
        when(documentService.uploadDocument(eq(10L), any(), eq("HR"))).thenReturn(dto);

        mockMvc.perform(multipart("/api/employees/10/documents/upload")
                        .file(file)
                        .param("category", "HR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("report.pdf"));

        verify(documentService).uploadDocument(10L, file, "HR");
    }

    @Test
    @DisplayName("POST /api/employees/{employeeId}/documents/upload – employee not found")
    public void uploadDocument_employeeNotFound() throws Exception {
        when(documentService.uploadDocument(eq(99L), any(), eq("HR")))
                .thenThrow(new ResourceNotFoundException("Employee", "id", 99L));

        mockMvc.perform(multipart("/api/employees/99/documents/upload")
                        .file(file)
                        .param("category", "HR"))
                .andExpect(status().isNotFound());

        verify(documentService).uploadDocument(99L, file, "HR");
    }



    @Test
    @DisplayName("GET /api/employees/{employeeId}/documents/list – success")
    public void getEmployeeDocuments_success() throws Exception {
        when(documentService.getDocumentByEmployee(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/employees/10/documents/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("report.pdf"));

        verify(documentService).getDocumentByEmployee(10L);
    }



    @Test
    @DisplayName("GET /api/employees/employee/{id}/documents – with category")
    public void getDocumentByCategory_withCategory() throws Exception {
        when(documentService.getDocumentByEmployeeAndCategory(10L, "HR")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/employees/employee/10/documents")
                        .param("category", "HR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileCategory").value("HR"));

        verify(documentService).getDocumentByEmployeeAndCategory(10L, "HR");
    }

    @Test
    @DisplayName("GET /api/employees/employee/{id}/documents – without category")
    public void getDocumentByCategory_withoutCategory() throws Exception {
        when(documentService.getDocumentByEmployee(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/employees/employee/10/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("report.pdf"));

        verify(documentService).getDocumentByEmployee(10L);
    }



    @Test
    @DisplayName("DELETE /api/employees/{employeeId}/documents/{documentId} – success")
    public void deleteDocument_success() throws Exception {
        doNothing().when(documentService).deleteDocument(1L);

        mockMvc.perform(delete("/api/employees/10/documents/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Document deleted successfully"));

        verify(documentService).deleteDocument(1L);
    }

    @Test
    @DisplayName("DELETE /api/employees/{employeeId}/documents/{documentId} – document not found")
    public void deleteDocument_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("EmployeeDocument", "id", 1L))
                .when(documentService).deleteDocument(1L);

        mockMvc.perform(delete("/api/employees/10/documents/1"))
                .andExpect(status().isNotFound());

        verify(documentService).deleteDocument(1L);
    }



    @Test
    @DisplayName("GET /api/employees/documents/file/{documentId}/download – success")
    public void downloadDocument_success() throws Exception {
        Resource resource = new ByteArrayResource("dummy".getBytes());
        ResponseEntity<Resource> resp = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"")
                .body(resource);
        when(documentService.downloadDocument(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/employees/documents/file/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\""));

        verify(documentService).downloadDocument(1L);
    }

    @Test
    @DisplayName("GET /api/employees/documents/file/{documentId}/download – file not found on disk")
    public void downloadDocument_fileNotFound() throws Exception {
        when(documentService.downloadDocument(1L))
                .thenThrow(new FileNotFoundCustomException("File not found", "FILE_NOT_FOUND"));

        mockMvc.perform(get("/api/employees/documents/file/1/download"))
                .andExpect(status().isNotFound());

        verify(documentService).downloadDocument(1L);
    }
}
