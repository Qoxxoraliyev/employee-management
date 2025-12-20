package com.muhammadali.employee_management.dto.auth;

public record LoginResponseDTO(
   String accessToken,
   String refreshToken,
   String tokenType
) {}
