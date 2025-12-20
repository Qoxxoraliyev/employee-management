package com.muhammadali.employee_management.dto;

import com.muhammadali.employee_management.enums.Status;

public record UsersRequestDTO(
        String username,
        String password,
        String email,
        Long roleId,
        Long employeeId,
        Status status
){}
