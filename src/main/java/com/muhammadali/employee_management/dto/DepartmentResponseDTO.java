package com.muhammadali.employee_management.dto;

public record DepartmentResponseDTO(
        Long id,
        String name,
        Integer manager_id
) {}
