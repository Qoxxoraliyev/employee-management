package com.muhammadali.employee_management.controller;
import com.muhammadali.employee_management.dto.*;
import com.muhammadali.employee_management.enums.DepartmentStatType;
import com.muhammadali.employee_management.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "API for managing departments and employee counts")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "Create a new department")
    @PostMapping("/create")
    public ResponseEntity<DepartmentResponseDTO> save(@RequestBody DepartmentRequestDTO dto){
        DepartmentResponseDTO result=departmentService.save(dto);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/search")
    public ResponseEntity<List<DepartmentResponseDTO>> searchByName(
            @RequestParam(required = false) String name){
        List<DepartmentResponseDTO> result=departmentService.searchByName(name);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/stats/yearly")
    public ResponseEntity<List<DepartmentYearlyStatsDTO>> getYearlyStats(){
        return ResponseEntity.ok(departmentService.getYearlyStats());
    }


    @Operation(summary = "Update department information")
    @PutMapping("/update/{id}")
    public ResponseEntity<DepartmentResponseDTO> update(@PathVariable Long id,
                                                        @RequestBody DepartmentRequestDTO dto){
        DepartmentResponseDTO updated=departmentService.update(id,dto);
        return ResponseEntity.ok(updated);
    }




    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<DepartmentResponseDTO>> getDepartmentsByManager(@PathVariable Integer managerId){
        return ResponseEntity.ok(departmentService.getByManager(managerId));
    }


    @GetMapping("/created")
    public ResponseEntity<List<DepartmentResponseDTO>> getByCreationDate(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to){
        List<DepartmentResponseDTO> result=departmentService.getByCreationDateRange(from,to);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/all")
    public ResponseEntity<List<DepartmentResponseDTO>> getAll(){
        List<DepartmentResponseDTO> departments=departmentService.getAll();
        return ResponseEntity.ok(departments);
    }


    @Operation(summary = "Get department statistics")
    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getDepartmentStats(
            @PathVariable Long id,
            @RequestParam String type
    ) {
        DepartmentStatType statType;
        try {
            statType = DepartmentStatType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid type provided: " + type);
        }

        return switch (statType) {
            case MIN_SALARY -> ResponseEntity.ok(departmentService.getMinSalary(id));
            case MAX_SALARY -> ResponseEntity.ok(departmentService.getMaxSalary(id));
            case AVG_SALARY -> ResponseEntity.ok(departmentService.getAverageSalary(id));
            case POSITION_COUNT -> ResponseEntity.ok(departmentService.getPositionCountByDepartment(id));
            case EMPLOYEE_COUNT -> ResponseEntity.ok(departmentService.getEmployeeCountInDepartment(id));
            case EMPLOYEES -> ResponseEntity.ok(departmentService.getEmployeesByDepartment(id));
            case YEARLY_STATS -> ResponseEntity.ok(departmentService.getYearlyStatsByDepartment(id));
            default -> ResponseEntity.badRequest().body("Invalid type provided");
        };
    }


    @GetMapping("/employee-count")
    public List<DepartmentEmployeeCountDTO> getEmployeeCountForAllDepartments(){
        return departmentService.getEmployeeCountForAllDepartments();
    }


    @Operation(summary = "Delete a department")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        departmentService.delete(id);
        return ResponseEntity.ok("Department successfully deleted");
    }


}
