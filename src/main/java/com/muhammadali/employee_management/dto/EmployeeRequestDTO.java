package com.muhammadali.employee_management.dto;

import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;

import java.time.LocalDate;


public record EmployeeRequestDTO (
    String firstName,
    String lastName,
    String phone,
    Gender gender,
    LocalDate birthDate,
    LocalDate hireDate,
    String position,
    Long departmentId,
    String image_path,
    Status status
){}
