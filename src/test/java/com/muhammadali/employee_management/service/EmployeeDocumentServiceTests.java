package com.muhammadali.employee_management.service;

import com.muhammadali.employee_management.dto.EmployeeDocumentDTO;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.EmployeeDocument;
import com.muhammadali.employee_management.exceptions.*;
import com.muhammadali.employee_management.mapper.EmployeeDocumentMapper;
import com.muhammadali.employee_management.repository.EmployeeDocumentRepository;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
public class EmployeeDocumentServiceTests {

    @InjectMocks
    public EmployeeDocumentService service;

    @Mock
    public EmployeeRepository employeeRepository;

    @Mock
    public EmployeeDocumentRepository documentRepository;

    @Mock
    public EmployeeDocumentMapper mapper;

    @Captor
    public ArgumentCaptor<EmployeeDocument> documentCaptor;

    @TempDir
    public Path tempDir;

    public Employee employee;
    public EmployeeDocument doc;
    public EmployeeDocumentDTO dto;


    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(
                service,
                "uploadDir",
                tempDir.toString()
        );

        service.init();

        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .build();

        doc = new EmployeeDocument();
        doc.setId(10L);
        doc.setFileName("report.pdf");
        doc.setFileType("application/pdf");
        doc.setFileCategory("HR");
        doc.setFilePath(tempDir.resolve("report.pdf").toString());
        doc.setEmployee(employee);
        doc.setUploadedAt(Timestamp.from(Instant.now()));

        dto = new EmployeeDocumentDTO(
                "report.pdf",
                "application/pdf",
                "HR",
                Timestamp.from(Instant.now())
        );
    }




    @Test
    @DisplayName("uploadDocument – success")
    public void uploadDocument_success() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(documentRepository.save(any(EmployeeDocument.class))).thenReturn(doc);
        when(mapper.toDTO(doc)).thenReturn(dto);

        EmployeeDocumentDTO result =
                service.uploadDocument(1L, file, "HR");

        assertThat(result).isEqualTo(dto);
        verify(documentRepository).save(documentCaptor.capture());

        EmployeeDocument saved = documentCaptor.getValue();
        assertThat(saved.getEmployee()).isEqualTo(employee);
        assertThat(saved.getFileCategory()).isEqualTo("HR");
        assertThat(Files.exists(Path.of(saved.getFilePath()))).isTrue();
    }

    @Test
    @DisplayName("uploadDocument – employee not found")
    public void uploadDocument_employeeNotFound() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.uploadDocument(1L, file, "HR"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(documentRepository, never()).save(any());
    }

    @Test
    @DisplayName("uploadDocument – empty file")
    public void uploadDocument_emptyFile() {
        MultipartFile file =
                new MockMultipartFile("file", "", "application/pdf", new byte[0]);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() ->
                service.uploadDocument(1L, file, "HR"))
                .isInstanceOf(FileUploadException.class);
    }

    @Test
    @DisplayName("uploadDocument – file too large")
    public void uploadDocument_fileTooLarge() {
        byte[] big = new byte[11 * 1024 * 1024];
        MultipartFile file = new MockMultipartFile(
                "file",
                "big.bin",
                "application/octet-stream",
                big
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() ->
                service.uploadDocument(1L, file, "HR"))
                .isInstanceOf(FileUploadException.class);
    }



    @Test
    @DisplayName("getDocumentByEmployee – success")
    public void getDocumentByEmployee() {
        when(documentRepository.findByEmployee_Id(1L))
                .thenReturn(List.of(doc));
        when(mapper.toDTO(doc)).thenReturn(dto);

        List<EmployeeDocumentDTO> result =
                service.getDocumentByEmployee(1L);

        assertThat(result).hasSize(1).containsExactly(dto);
    }


    @Test
    @DisplayName("getDocumentByEmployeeAndCategory – success")
    public void getDocumentByEmployeeAndCategory() {
        when(documentRepository
                .findByEmployee_IdAndFileCategory(1L, "HR"))
                .thenReturn(List.of(doc));

        when(mapper.toDTO(doc)).thenReturn(dto);

        List<EmployeeDocumentDTO> result =
                service.getDocumentByEmployeeAndCategory(1L, "HR");

        assertThat(result).hasSize(1).containsExactly(dto);
    }



    @Test
    @DisplayName("downloadDocument – success")
    public void downloadDocument_success() throws IOException {
        Files.write(Path.of(doc.getFilePath()), "content".getBytes());
        when(documentRepository.findById(10L)).thenReturn(Optional.of(doc));

        ResponseEntity<Resource> response =
                service.downloadDocument(10L);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders()
                .getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("report.pdf");
    }

    @Test
    @DisplayName("downloadDocument – file not found")
    public void downloadDocument_fileNotFound() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() ->
                service.downloadDocument(10L))
                .isInstanceOf(FileNotFoundCustomException.class);
    }



    @Test
    @DisplayName("deleteDocument – success")
    public void deleteDocument_success() throws IOException {
        Path file = Files.createFile(Path.of(doc.getFilePath()));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(doc));

        service.deleteDocument(10L);

        assertThat(Files.exists(file)).isFalse();
        verify(documentRepository).delete(doc);
    }

    @Test
    @DisplayName("deleteDocument – not found")
    public void deleteDocument_notFound() {
        when(documentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.deleteDocument(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

