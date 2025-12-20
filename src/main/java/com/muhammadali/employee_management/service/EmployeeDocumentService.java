package com.muhammadali.employee_management.service;
import com.muhammadali.employee_management.dto.EmployeeDocumentDTO;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.EmployeeDocument;
import com.muhammadali.employee_management.exceptions.FileNotFoundCustomException;
import com.muhammadali.employee_management.exceptions.FileStorageException;
import com.muhammadali.employee_management.exceptions.FileUploadException;
import com.muhammadali.employee_management.exceptions.ResourceNotFoundException;
import com.muhammadali.employee_management.mapper.EmployeeDocumentMapper;
import com.muhammadali.employee_management.repository.EmployeeDocumentRepository;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import org.springframework.core.io.Resource;
import lombok.SneakyThrows;
import org.springframework.core.io.FileSystemResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeDocumentService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path rootLocation;

    private final EmployeeRepository employeeRepository;

    private final EmployeeDocumentRepository documentRepository;


    @jakarta.annotation.PostConstruct
    public void init(){
        this.rootLocation= Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        }catch (Exception e){
            throw new RuntimeException("Could not create upload directory: "+uploadDir,e);
        }
    }


    @Transactional
    @SneakyThrows
    public EmployeeDocumentDTO uploadDocument(Long employeeId, MultipartFile file, String category) {
        Employee employee = getEmployeeById(employeeId);
        validateFile(file);
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + extension;
        Path destinationPath;
        try {
            destinationPath = rootLocation.resolve(storedFilename)
                    .normalize()
                    .toAbsolutePath();

            if (!destinationPath.startsWith(rootLocation)) {
                throw new FileUploadException("Cannot store file outside upload directory",
                        "INVALID_PATH");
            }

            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            throw new FileStorageException("Failed to save file", "STORAGE_ERROR", e.getMessage());
        }

        EmployeeDocument document = new EmployeeDocument();
        document.setFileName(originalFilename);
        document.setFileType(file.getContentType());
        document.setFileCategory(category);
        document.setFilePath(destinationPath.toString());
        document.setUploadedAt(Timestamp.from(Instant.now()));
        document.setEmployee(employee);

        return EmployeeDocumentMapper.toDTO(documentRepository.save(document));
    }


    public List<EmployeeDocumentDTO> getDocumentByEmployeeAndCategory(Long employeeId,String category){
        return documentRepository.findByEmployee_IdAndFileCategory(employeeId,category)
                .stream()
                .map(EmployeeDocumentMapper::toDTO)
                .collect(Collectors.toList());
    }


    public ResponseEntity<Resource> downloadDocument(Long documentId) {
        EmployeeDocument document = getDocumentById(documentId);
        Path filePath = Paths.get(document.getFilePath()).normalize();
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundCustomException(
                    "File not found or unreadable: " + document.getFileName(),
                    "FILE_NOT_FOUND"
            );
        }
        String contentType = resolveContentType(filePath, document.getFileType());
        String encodedFilename = UriUtils.encode(document.getFileName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename +
                                "\"; filename*=UTF-8''" + encodedFilename)
                .build();
    }


    @Transactional
    public void deleteDocument(Long documentId) {
        EmployeeDocument document = getDocumentById(documentId);
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (Exception e) {
            throw new FileStorageException(
                    "Failed to delete file",
                    "DELETE_ERROR",
                    e.getMessage()
            );
        }
        documentRepository.delete(document);
    }


    public List<EmployeeDocumentDTO> getDocumentByEmployee(Long employeeId) {
        getEmployeeById(employeeId);
        return documentRepository.findByEmployee_Id(employeeId)
                .stream()
                .map(EmployeeDocumentMapper::toDTO)
                .collect(Collectors.toList());
    }


    private Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "id", id
                ));
    }


    private EmployeeDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EmployeeDocument", "id", id
                ));
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("Uploaded file is empty", "FILE_EMPTY");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.contains("..")) {
            throw new FileUploadException("Invalid file name", "INVALID_FILE_NAME");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileUploadException("File size exceeds 10MB limit", "FILE_TOO_LARGE");
        }
    }


    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }


    private String resolveContentType(Path path, String fallback) {
        try {
            String type = Files.probeContentType(path);
            return type != null ? type : fallback != null ? fallback : "application/octet-stream";
        } catch (Exception e) {
            return fallback != null ? fallback : "application/octet-stream";
        }
    }


}
