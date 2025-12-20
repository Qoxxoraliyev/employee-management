package com.muhammadali.employee_management.repository;

import com.muhammadali.employee_management.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,Long> {
}
