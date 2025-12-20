package com.muhammadali.employee_management.service;
import com.muhammadali.employee_management.dto.*;
import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.exceptions.BusinessValidationException;
import com.muhammadali.employee_management.exceptions.DepartmentHasEmployeeException;
import com.muhammadali.employee_management.exceptions.InvalidDateRangeException;
import com.muhammadali.employee_management.exceptions.ResourceNotFoundException;
import com.muhammadali.employee_management.mapper.DepartmentMapper;
import com.muhammadali.employee_management.mapper.EmployeeMapper;
import com.muhammadali.employee_management.repository.DepartmentRepository;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final EmployeeRepository employeeRepository;

    @Transactional
    public DepartmentResponseDTO save(DepartmentRequestDTO dto){
        Department department= DepartmentMapper.toEntity(dto);
        department.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        Department saved=departmentRepository.save(department);
        return DepartmentMapper.toResponse(saved);
    }


    @Transactional
    public DepartmentResponseDTO update(Long id,DepartmentRequestDTO dto){
        Department existing=getDepartmentById(id);
        existing.setName(dto.name());
        existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        existing.setManagerId(dto.manager_id());
        return DepartmentMapper.toResponse(departmentRepository.save(existing));
    }


    public List<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId){
        List<Employee> employees=employeeRepository.findByDepartmentId(departmentId);
        return employees.stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    private Double safeReturn(Double value) {
        return value != null ? value : 0.0;
    }


    public Double getAverageSalary(Long departmentId){
        return safeReturn(employeeRepository.findAverageSalaryByDepartmentId(departmentId));
    }


    public Double getMaxSalary(Long departmentId){
        return safeReturn(employeeRepository.findMaxSalaryByDepartmentId(departmentId));
    }


    public Double getMinSalary(Long departmentId){
        return safeReturn(employeeRepository.findMinSalaryByDepartmentId(departmentId));
    }


    public long getPositionCountByDepartment(Long departmentId){
        getDepartmentOrThrow(departmentId);
        return employeeRepository.countPositionByDepartmentId(departmentId);
    }


    public List<DepartmentYearlyStatsDTO> getYearlyStats(){
        return departmentRepository.getGlobalYearlyHires();
    }


    public List<DepartmentYearlyStatsDTO> getYearlyStatsByDepartment(Long departmentId){
        getDepartmentOrThrow(departmentId);
        return departmentRepository.getYearlyHiresByDepartment(departmentId);
    }


    public List<DepartmentResponseDTO> getAll(){
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentMapper::toResponse)
                .collect(Collectors.toList());
    }


    public List<DepartmentResponseDTO> getByCreationDateRange(String from, String to) {
        Timestamp start = parseDate(from, true);
        Timestamp end = parseDate(to, false);

        List<Department> departments;

        if (start != null && end != null) {
            departments = departmentRepository.findByCreatedAtBetween(start, end);
        } else if (start != null) {
            departments = departmentRepository.findByCreatedAtGreaterThanEqual(start);
        } else if (end != null) {
            departments = departmentRepository.findByCreatedAtLessThanEqual(end);
        } else {
            departments = departmentRepository.findAll();
        }

        return departments.stream()
                .map(DepartmentMapper::toResponse)
                .toList();
    }


    private Timestamp parseDate(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.trim().isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr.trim());
            LocalDateTime dateTime = isStart
                    ? date.atStartOfDay()
                    : date.atTime(23, 59, 59, 999_000_000);
            return Timestamp.valueOf(dateTime);
        } catch (Exception e) {
            throw new InvalidDateRangeException(
                    "Invalid date format: " + dateStr,
                    "INVALID_DATE_FORMAT",
                    "Use YYYY-MM-DD"
            );
        }
    }


    public List<DepartmentResponseDTO> getByManager(Integer managerId) {
        if (managerId == null) {
            throw new BusinessValidationException(
                    "MANAGER_ID_REQUIRED",
                    "Manager ID must not be null"
            );
        }
        return departmentRepository.findByManagerId(managerId)
                .stream()
                .map(DepartmentMapper::toResponse)
                .toList();
    }


    public List<DepartmentResponseDTO> searchByName(String name){
        if (name != null && name.length() < 2) {
            throw new BusinessValidationException(
                    "INVALID_SEARCH_TEXT",
                    "Search text must contain at least 2 characters"
            );
        }
        List<Department> departments =
                (name == null || name.trim().isEmpty())
                        ? departmentRepository.findAll()
                        : departmentRepository.findByNameContainingIgnoreCase(name.trim());
        return departments.stream()
                .map(DepartmentMapper::toResponse)
                .toList();
    }




    public long getEmployeeCountInDepartment(Long departmentId){
        getDepartmentOrThrow(departmentId);
        return employeeRepository.countByDepartment_Id(departmentId);
    }


    public List<DepartmentEmployeeCountDTO> getEmployeeCountForAllDepartments(){
        return departmentRepository.getDepartmentEmployeeCounts();
    }


    @Transactional
    public void delete(Long id){
        Department department = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department", "id", id));

        long employees = employeeRepository.countByDepartment_Id(id);
        if (employees > 0) {
            throw new DepartmentHasEmployeeException(
                    "Cannot delete department with existing employees",
                    "DEPARTMENT_HAS_EMPLOYEES",
                    "Employee count: " + employees
            );
        }
        departmentRepository.delete(department);
    }


    private Department getDepartmentOrThrow(Long id){
        return getDepartmentById(id);
    }


    private Department getDepartmentById(Long id){
        return departmentRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Department","id",id));
    }


}
