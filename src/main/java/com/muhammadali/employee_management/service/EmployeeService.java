package com.muhammadali.employee_management.service;
import com.muhammadali.employee_management.dto.EmployeeRequestDTO;
import com.muhammadali.employee_management.dto.EmployeeResponseDTO;
import com.muhammadali.employee_management.entity.Department;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;
import com.muhammadali.employee_management.exceptions.BusinessValidationException;
import com.muhammadali.employee_management.exceptions.InvalidDateRangeException;
import com.muhammadali.employee_management.exceptions.ResourceNotFoundException;
import com.muhammadali.employee_management.mapper.EmployeeMapper;
import com.muhammadali.employee_management.repository.DepartmentRepository;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import com.muhammadali.employee_management.specification.EmployeeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {


    private final EmployeeRepository employeeRepository;

    private final DepartmentRepository departmentRepository;


    @Transactional
    public EmployeeResponseDTO save(EmployeeRequestDTO dto){
        if (dto.departmentId() == null) {
            throw new BusinessValidationException("Department ID must not be null");
        }
        Department department = getDepartmentById(dto.departmentId());
        Employee employee = EmployeeMapper.toEntity(dto, department);
        employee.setCreated_at(Timestamp.valueOf(java.time.LocalDateTime.now()));
        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }


    public List<EmployeeResponseDTO> getTop5HighestSalary(){
        Pageable pageable = PageRequest.of(0, 5);
        List<Employee> employees = employeeRepository.findTopEmployeesBySalary(pageable);

        return employees.stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO> searchByPhone(String phone) {
        return employeeRepository.findByPhoneContainingIgnoreCase(phone)
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO> advancedSearch(
            String name, String department, Status status, Integer minAge, Integer maxAge) {

        Specification<Employee> spec = Specification.where(EmployeeSpecification.hasName(name))
                .and(EmployeeSpecification.hasDepartment(department))
                .and(EmployeeSpecification.hasStatus(status))
                .and(EmployeeSpecification.hasAgeBetween(minAge, maxAge));

        return employeeRepository.findAll(spec)
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO> searchByAgeRange(int min, int max) {
        validateAgeRange(min, max);
        LocalDate from = LocalDate.now().minusYears(max);
        LocalDate to = LocalDate.now().minusYears(min);
        return employeeRepository.findByBirthDateBetween(from, to)
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO> searchBySalaryRange(Double min, Double max) {
        if (min == null || max == null || min > max) {
            throw new IllegalArgumentException("Invalid salary range");
        }
        return employeeRepository.findEmployeeBySalaryBetween(min, max)
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO> searchByHireDateRange(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        return employeeRepository.findByHireDateBetween(from, to)
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO>  filterByStatusAndDepartment(Status status,String departmentName){
        List<Employee> employees=employeeRepository.findByStatusAndDepartmentName(status,departmentName);
        return employees.stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO> filterByDepartmentAndPosition(String departmentName,String position){
        List<Employee> employees=employeeRepository.findByDepartmentNameAndPositionIgnoreCase(departmentName,position);
        return employees.stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO> searchByBirthDateBetween(LocalDate from, LocalDate to){
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before 'to' date");
        }
        return employeeRepository.findByBirthDateBetween(from, to)
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO> searchByGender(Gender gender) {
        return employeeRepository.findByGender(gender)
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    public List<EmployeeResponseDTO> searchByName(String name) {
        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }


    @Transactional
    public EmployeeResponseDTO update(Long id, EmployeeRequestDTO dto){
        Employee existing = getEmployeeById(id);
        Department department = getDepartmentById(dto.departmentId());
        existing.setFirstName(dto.firstName());
        existing.setLastName(dto.lastName());
        existing.setPhone(dto.phone());
        existing.setGender(dto.gender());
        existing.setBirthDate(dto.birthDate());
        existing.setHireDate(dto.hireDate());
        existing.setPosition(dto.position());
        existing.setDepartment(department);
        existing.setStatus(dto.status());
        existing.setUpdated_at(new Timestamp(System.currentTimeMillis()));
        Employee updated = employeeRepository.save(existing);
        return EmployeeMapper.toResponse(updated);
    }


    public List<EmployeeResponseDTO> findAll(){
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeMapper::toResponse)
                .collect(Collectors.toList());
    }


    public List<EmployeeResponseDTO> getEmployeesStatus(Status status){
        return employeeRepository.findByStatus(status)
                .stream()
                .map(emp->new EmployeeResponseDTO(
                        emp.getId(),
                        emp.getFirstName()+" "+emp.getLastName(),
                        emp.getPhone(),
                        emp.getGender(),
                        emp.getBirthDate(),
                        emp.getHireDate(),
                        emp.getPosition(),
                        emp.getDepartment()!=null ? emp.getDepartment().getName():null,
                        emp.getStatus(),
                        emp.getImage_path()
                ))
                .toList();
    }


    public List<EmployeeResponseDTO> getEmployeeByDepartment(String departmentName){
        return employeeRepository.findByDepartmentName(departmentName)
                .stream()
                .map(emp->new EmployeeResponseDTO(
                        emp.getId(),
                        emp.getFirstName()+" "+emp.getLastName(),
                        emp.getPhone(),
                        emp.getGender(),
                        emp.getBirthDate(),
                        emp.getHireDate(),
                        emp.getPosition(),
                        emp.getDepartment()!=null?emp.getDepartment().getName():null,
                        emp.getStatus(),
                        emp.getImage_path()
                ))
                .toList();
    }


    public Page<EmployeeResponseDTO> getEmployeesWithPagingAndSorting(int page,int size,String sortBy,String direction){
        Sort sort=direction.equalsIgnoreCase("desc")
                ?Sort.by(sortBy).descending()
                :Sort.by(sortBy).ascending();
        Pageable pageable= PageRequest.of(page,size,sort);

        return employeeRepository.findAll(pageable)
                .map(emp->new EmployeeResponseDTO(
                        emp.getId(),
                        emp.getFirstName()+" "+emp.getLastName(),
                        emp.getPhone(),
                        emp.getGender(),
                        emp.getBirthDate(),
                        emp.getHireDate(),
                        emp.getPosition(),
                        emp.getDepartment()!=null?emp.getDepartment().getName():null,
                        emp.getStatus(),
                        emp.getImage_path()
                ));
    }


    public int calculateAge(LocalDate birthDate){
        if (birthDate == null){
            throw new BusinessValidationException("Birth date cannot be null");
        }
        if (birthDate.isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Birth date cannot be in future");
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }


    public Employee findById(Long id){
        return employeeRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("Employee not found"));
    }


    public long getTotalEmployees(){
        return employeeRepository.count();
    }


    public double getActiveEmployeePercentage() {
        long total = getTotalEmployees();
        if (total == 0) return 0.0;
        long active = employeeRepository.countByStatus(Status.ACTIVE);
        return Math.round((active * 100.0 / total) * 100.0) / 100.0;
    }


    public long countNewEmployeesLast30Days(){
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return employeeRepository.countByHireDateAfter(thirtyDaysAgo);
    }


    @Transactional
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }


    private Employee getEmployeeById(Long id){
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }


    private Department getDepartmentById(Long id){
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }


    private void validateAgeRange(int min, int max){
        if (min < 0 || max < 0 || min > max){
            throw new InvalidDateRangeException("Invalid age range: min=" + min + ", max=" + max);
        }
    }


    private void validateDateRange(LocalDate from, LocalDate to){
        if (from != null && to != null && from.isAfter(to)){
            throw new InvalidDateRangeException("'from' date must be before or equal to 'to' date");
        }
    }

}
