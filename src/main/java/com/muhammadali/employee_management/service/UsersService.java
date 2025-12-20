package com.muhammadali.employee_management.service;
import com.muhammadali.employee_management.dto.UsersRequestDTO;
import com.muhammadali.employee_management.dto.UsersResponseDTO;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.Role;
import com.muhammadali.employee_management.entity.Users;
import com.muhammadali.employee_management.exceptions.ResourceAlreadyExistsException;
import com.muhammadali.employee_management.exceptions.ResourceNotFoundException;
import com.muhammadali.employee_management.mapper.UsersMapper;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import com.muhammadali.employee_management.repository.RoleRepository;
import com.muhammadali.employee_management.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsersService {

    private final UsersRepository userRepository;

    private final EmployeeRepository employeeRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;


    @Transactional
    public UsersResponseDTO save(UsersRequestDTO dto) {
        Employee employee = getEmployeeById(dto.employeeId());
        Role role = getRoleById(dto.roleId());
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new ResourceAlreadyExistsException("User", "username", dto.username());
        }
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("User", "email", dto.email());
        }
        Users users = UsersMapper.toEntity(dto,role,employee);
        users.setEmployee(employee);
        users.setPassword(passwordEncoder.encode(dto.password()));
        Users saved = userRepository.save(users);
        return UsersMapper.toResponse(saved);
    }


    public List<UsersResponseDTO> findByUsernameLike(String username){
        return userRepository.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(UsersMapper::toResponse)
                .toList();
    }


    public UsersResponseDTO findByUsername(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return UsersMapper.toResponse(user);
    }


    @Transactional
    public UsersResponseDTO update(Long id, UsersRequestDTO dto) {
        Users users = getUserById(id);
       // Employee employee = getEmployeeById(dto.employeeId());
        Role role = getRoleById(dto.roleId());
        userRepository.findByUsername(dto.username())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new ResourceAlreadyExistsException("User", "username", dto.username()); });
        userRepository.findByEmail(dto.email())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new ResourceAlreadyExistsException("User", "email", dto.email()); });
        users.setRole(role);
        users.setUsername(dto.username());
        users.setEmail(dto.email());
        users.setStatus(dto.status());

        if (dto.password() != null && !dto.password().isBlank()) {
            users.setPassword(passwordEncoder.encode(dto.password()));
        }
        users.setUpdated_at(new Timestamp(System.currentTimeMillis()));
        Users updated = userRepository.save(users);
        return UsersMapper.toResponse(updated);
    }


    public List<UsersResponseDTO> findByRole(String roleName){
        return userRepository.findByRoleNameIgnoreCase(roleName)
                .stream()
                .map(UsersMapper::toResponse)
                .toList();
    }


    public long getTotalUsersCount(){
        return userRepository.count();
    }


    public List<UsersResponseDTO> findAll(){
        return userRepository.findAll()
                .stream()
                .map(UsersMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }


    private Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }


    private Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }


    private Users getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }


}
