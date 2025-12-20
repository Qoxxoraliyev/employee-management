package com.muhammadali.employee_management.dto;

public record UsersResponseDTO(
        Long id,
        String username,
        String email,
        String roleName,
        String status
){}
