package com.muhammadali.employee_management.mapper;

import com.muhammadali.employee_management.dto.UsersRequestDTO;
import com.muhammadali.employee_management.dto.UsersResponseDTO;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.Role;
import com.muhammadali.employee_management.entity.Users;

import java.sql.Timestamp;

public class UsersMapper {

    public static Users toEntity(UsersRequestDTO dto, Role role, Employee employee){
        Users users=new Users();
        users.setRole(role);
        users.setEmployee(employee);
        users.setUsername(dto.username());
        users.setEmail(dto.email());
        users.setStatus(dto.status());
        users.setUpdated_at(new Timestamp(System.currentTimeMillis()));
        return users;
    }


    public static UsersResponseDTO toResponse(Users users){
        return new UsersResponseDTO(
                users.getId(),
                users.getUsername(),
                users.getEmail(),
                users.getRole()!=null ? users.getRole().getName():null,
                users.getStatus()!=null ? users.getStatus().name():null
        );
    }


}
