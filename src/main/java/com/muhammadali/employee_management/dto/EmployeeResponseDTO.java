package com.muhammadali.employee_management.dto;

import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;

import java.time.LocalDate;


public record EmployeeResponseDTO(
   Long id,
   String fullName,
   String phone,
   Gender gender,
   LocalDate birthDate,
   LocalDate hire_date,
   String position,
   String departmentName,
   Status status,
   String image_path
) {}
