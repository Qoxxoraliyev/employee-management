package com.muhammadali.employee_management.dto;

public record DepartmentEmployeeCountDTO(
   Long departmentId,
   String departmentName,
   Long employeeCount
) {}
