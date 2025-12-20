package com.muhammadali.employee_management.repository;

import com.muhammadali.employee_management.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users,Long> {

    Optional<Users> findByEmail(String email);

    Optional<Users> findByUsername(String username);

    List<Users> findByUsernameContainingIgnoreCase(String username);

    List<Users> findByRoleNameIgnoreCase(String roleName);



}
