package com.muhammadali.employee_management.dto;

import java.sql.Timestamp;

public record EmployeeDocumentDTO(
   String fileName,
   String fileType,
   String fileCategory,
   Timestamp uploadedAt
){}
