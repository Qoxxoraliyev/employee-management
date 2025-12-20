package com.muhammadali.employee_management.mapper;

import com.muhammadali.employee_management.dto.DepartmentRequestDTO;
import com.muhammadali.employee_management.dto.DepartmentResponseDTO;
import com.muhammadali.employee_management.entity.Department;

public class DepartmentMapper {

    public static Department toEntity(DepartmentRequestDTO dto){
        Department d=new Department();
        d.setName(dto.name());
        d.setManagerId(dto.manager_id());
        return d;
    }

    public static DepartmentResponseDTO toResponse(Department d){
        return new DepartmentResponseDTO(
                d.getId(),
                d.getName(),
                d.getManagerId()
        );
    }
}
