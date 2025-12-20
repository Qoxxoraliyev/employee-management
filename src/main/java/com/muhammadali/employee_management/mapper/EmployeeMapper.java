package com.muhammadali.employee_management.mapper;

import com.muhammadali.employee_management.dto.EmployeeRequestDTO;
import com.muhammadali.employee_management.dto.EmployeeResponseDTO;
import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;



public class EmployeeMapper {

    public static Employee toEntity(EmployeeRequestDTO dto, Department department){
        Employee e=new Employee();
        e.setFirstName(dto.firstName());
        e.setLastName(dto.lastName());
        e.setPhone(dto.phone());
        e.setGender(dto.gender());
        e.setBirthDate(dto.birthDate());
        e.setHireDate(dto.hireDate());
        e.setPosition(dto.position());
        e.setDepartment(department);
        e.setImage_path(dto.image_path());
        e.setStatus(dto.status());
        return e;
    }

    public static EmployeeResponseDTO toResponse(Employee e){
        return new EmployeeResponseDTO(
                e.getId(),
                e.getFirstName()+" "+e.getLastName(),
                e.getPhone(),
                e.getGender(),
                e.getBirthDate(),
                e.getHireDate(),
                e.getPosition(),
                e.getDepartment()!=null ? e.getDepartment().getName():null,
                e.getStatus(),
                e.getImage_path()
        );
    }
}
