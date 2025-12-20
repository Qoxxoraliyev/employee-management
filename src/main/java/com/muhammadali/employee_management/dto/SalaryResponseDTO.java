package com.muhammadali.employee_management.dto;

import java.util.Date;

public record SalaryResponseDTO(
        Long id,
        Long employeeId,
        Double amount,
        String currency,
        Date paymentDate,
        Double bonus
) {}
