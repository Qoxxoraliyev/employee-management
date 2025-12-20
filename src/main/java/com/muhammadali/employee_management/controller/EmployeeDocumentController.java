package com.muhammadali.employee_management.controller;
import com.muhammadali.employee_management.dto.EmployeeDocumentDTO;
import com.muhammadali.employee_management.service.EmployeeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/employees/{employeeId}/documents")
@RequiredArgsConstructor
public class EmployeeDocumentController {

    private final EmployeeDocumentService employeeDocumentService;

    @Operation(summary = "Upload a document for an employee")
    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<EmployeeDocumentDTO> uploadDocument(
            @PathVariable Long employeeId,
            @RequestParam("file")MultipartFile file,
            @RequestParam("category") String category
            ) throws Exception{
        EmployeeDocumentDTO dto=employeeDocumentService.uploadDocument(employeeId,file,category);
            return ResponseEntity.ok(dto);
    }


    @Operation(summary = "Get all documents belonging to an employee")
    @GetMapping("/list")
    public ResponseEntity<List<EmployeeDocumentDTO>> getEmployeeDocuments(@PathVariable Long employeeId){
        List<EmployeeDocumentDTO> docs=employeeDocumentService.getDocumentByEmployee(employeeId);
        return ResponseEntity.ok(docs);
    }


    @Operation(summary = "Get employee documents by category")
    @GetMapping("/employee/{id}/documents")
    public List<EmployeeDocumentDTO> getDocumentByCategory(
            @PathVariable Long id,
            @RequestParam(required = false) String category){
        if (category!=null && !category.isEmpty()){
            return employeeDocumentService.getDocumentByEmployeeAndCategory(id,category);
        }
        return employeeDocumentService.getDocumentByEmployee(id);
    }


    @Operation(summary = "Delete an employee document")
    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable Long employeeId,
            @PathVariable Long documentId){
        employeeDocumentService.deleteDocument(documentId);
        return ResponseEntity.ok("Document deleted successfully");
    }


    @Operation(summary = "Download a document file")
    @GetMapping("/file/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        return employeeDocumentService.downloadDocument(documentId);
    }



}
