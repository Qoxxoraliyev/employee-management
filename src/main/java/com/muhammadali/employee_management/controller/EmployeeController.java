package com.muhammadali.employee_management.controller;
import com.muhammadali.employee_management.dto.EmployeeRequestDTO;
import com.muhammadali.employee_management.dto.EmployeeResponseDTO;
import com.muhammadali.employee_management.dto.SalaryResponseDTO;
import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;
import com.muhammadali.employee_management.service.EmployeeService;
import com.muhammadali.employee_management.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management",description = "Basic CRUD operations for employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    private final SalaryService salaryService;

    @Operation(summary = "Create a new employee")
    @PostMapping("/save")
    public ResponseEntity<EmployeeResponseDTO> create(@Valid @RequestBody EmployeeRequestDTO requestDTO){
        return ResponseEntity.ok(employeeService.save(requestDTO));
    }


    @Operation(summary = "Update an employee")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.ok(employeeService.update(id,dto));
    }


    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAll(){
        return ResponseEntity.ok(employeeService.findAll());
    }


    @GetMapping("/age")
    public List<EmployeeResponseDTO> searchByAgeRange(
            @RequestParam int min,
            @RequestParam int max
    ){
        return employeeService.searchByAgeRange(min,max);
    }


    @Operation(summary = "Filter employees by status and department")
    @GetMapping("/search/status-department")
    public List<EmployeeResponseDTO> filterByStatusAndDepartment(
            @RequestParam Status status,
            @RequestParam String department
    ){
        return employeeService.filterByStatusAndDepartment(status,department);
    }


    @Operation(summary = "Get top 5 employees with the highest salaries")
    @GetMapping("/top-salary")
    public List<EmployeeResponseDTO> getTopSalaryEmployees(){
        return employeeService.getTop5HighestSalary();
    }


    @Operation(summary = "Search by hire date range")
    @GetMapping("/hiredate")
    public ResponseEntity<List<EmployeeResponseDTO>> byHireDate(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate to) {
        return ResponseEntity.ok(employeeService.searchByHireDateRange(from, to));
    }


    @Operation(summary = "Filter employees by department and position")
    @GetMapping("/filter")
    public List<EmployeeResponseDTO> filterByDepartmentAndPosition(
            @RequestParam String department,
            @RequestParam String position
    ){
        return employeeService.filterByDepartmentAndPosition(department,position);
    }


    @Operation(summary = "Search employees by salary range")
    @GetMapping("/salary")
    public List<EmployeeResponseDTO> searchBySalaryRange(
            @RequestParam Double min,
            @RequestParam Double max
    ){
        return employeeService.searchBySalaryRange(min,max);
    }


    @Operation(summary = "Search employees by name")
    @GetMapping("/search")
    public List<EmployeeResponseDTO> searchEmployees(@RequestParam String name){
        return employeeService.searchByName(name);
    }


    @Operation(summary = "Search employees by phone number")
    @GetMapping("/search/phone")
    public List<EmployeeResponseDTO> searchByPhone(@RequestParam String phone){
        return employeeService.searchByPhone(phone);
    }


    @Operation(summary = "Search employees by gender")
    @GetMapping("/search/gender")
    public List<EmployeeResponseDTO> searchByGender(@RequestParam Gender gender){
        return employeeService.searchByGender(gender);
    }


    @Operation(summary = "Search employees by birth date range")
    @GetMapping("/birthdate")
    public List<EmployeeResponseDTO> searchByBirthDateRange(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate to
    ){
        return employeeService.searchByBirthDateBetween(from, to);
    }


    @GetMapping("/status")
    public ResponseEntity<List<EmployeeResponseDTO>> getByStatus(@RequestParam Status status) {
        return ResponseEntity.ok(employeeService.getEmployeesStatus(status));
    }


    @Operation(summary = "Get employees by department name")
    @GetMapping("/department/{name}")
    public ResponseEntity<List<EmployeeResponseDTO>> getByDepartment(@PathVariable String name){
        List<EmployeeResponseDTO> findByDepartmentName=employeeService.getEmployeeByDepartment(name);
        return ResponseEntity.ok(findByDepartmentName);
    }


    @Operation(summary = "Get an employee's age")
    @GetMapping("/{id}/age")
    public ResponseEntity<Integer> getEmployeeAge(@PathVariable Long id){
        var employee=employeeService.findById(id);
        int age=employeeService.calculateAge(employee.getBirthDate());
        return ResponseEntity.ok(age);
    }


    @Operation(summary = "Get employees with pagination and sorting")
    @GetMapping("/paging")
    public Page<EmployeeResponseDTO> getEmployeesPagingAndSorting(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        return employeeService.getEmployeesWithPagingAndSorting(page,size,sortBy,direction);
    }


    @Operation(summary = "Get salary history of an employee")
    @GetMapping("/{employeeId}/salaries")
    public ResponseEntity<List<SalaryResponseDTO>> getEmployeeSalaryHistory(
            @PathVariable Long employeeId
    ){
        List<SalaryResponseDTO> salaryHistory=salaryService.getSalaryHistoryByEmployee(employeeId);
        return ResponseEntity.ok(salaryHistory);
    }


    @Operation(summary = "Advanced employee search")
    @GetMapping("/advanced-search")
    public List<EmployeeResponseDTO> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge
     ){
        return employeeService.advancedSearch(name,department,status,minAge,maxAge);
    }


    @Operation(summary = "Get total number of employees")
    @GetMapping("/count")
    public long getEmployeeCount(){
        return employeeService.getTotalEmployees();
    }


    @Operation(summary = "Get percentage of active employees")
    @GetMapping("/active-percentage")
    public double getActiveEmployeePercentage(){
        return employeeService.getActiveEmployeePercentage();
    }


    @Operation(summary = "Get number of new employees in the last 30 days")
    @GetMapping("/new-employees/month")
    public long getNewEmployeesLastMonth(){
        return employeeService.countNewEmployeesLast30Days();
    }


    @Operation(summary = "Delete an employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }



}
