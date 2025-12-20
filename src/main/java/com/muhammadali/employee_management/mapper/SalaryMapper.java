package com.muhammadali.employee_management.mapper;

import com.muhammadali.employee_management.dto.SalaryRequestDTO;
import com.muhammadali.employee_management.dto.SalaryResponseDTO;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.Salary;

import java.sql.Timestamp;

public class SalaryMapper {


    public static Salary toEntity(SalaryRequestDTO dto, Employee employee){
        Salary salary=new Salary();
        salary.setEmployee(employee);
        salary.setCurrency(dto.currency());
        salary.setPaymentDate(dto.paymentDate());
        salary.setAmount(dto.amount());
        salary.setBonus(dto.bonus());
        salary.setCreated_at(new Timestamp(System.currentTimeMillis()));
        return salary;
    }



    public static SalaryResponseDTO toResponse(Salary salary){
        return new SalaryResponseDTO(
                salary.getId(),
                salary.getEmployee()!=null ? salary.getEmployee().getId():null,
                salary.getAmount(),
                salary.getCurrency(),
                salary.getPaymentDate(),
                salary.getBonus()
        );
    }


}
