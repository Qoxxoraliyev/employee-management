package com.muhammadali.employee_management.controller;
import com.muhammadali.employee_management.dto.SalaryMonthlyStatDTO;
import com.muhammadali.employee_management.dto.SalaryRequestDTO;
import com.muhammadali.employee_management.dto.SalaryResponseDTO;
import com.muhammadali.employee_management.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;


    @Operation(summary = "Create a new salary record")
    @PostMapping
    public ResponseEntity<SalaryResponseDTO> create(@RequestBody SalaryRequestDTO dto){
        return ResponseEntity.ok(salaryService.save(dto));
    }


    @Operation(summary = "Update existing salary")
    @PutMapping("/{id}")
    public ResponseEntity<SalaryResponseDTO> update(@PathVariable Long id,
                                                    @RequestBody SalaryRequestDTO dto){
        return ResponseEntity.ok(salaryService.update(id,dto));
    }


    @Operation(summary = "Get all salary records")
    @GetMapping("/all")
    public ResponseEntity<List<SalaryResponseDTO>> getAll(){
        return ResponseEntity.ok(salaryService.findAll());
    }


    @Operation(summary = "Add bonus to a salary")
    @PutMapping("/{id}/bonus")
    public ResponseEntity<SalaryResponseDTO> addBonus(
            @PathVariable Long id,
            @RequestParam Double bonus
    ){
        SalaryResponseDTO response=salaryService.addBonus(id,bonus);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Calculate average salary")
    @GetMapping("/employee/{employeeId}/average")
    public ResponseEntity<Double> getAverageSalary(@PathVariable Long employeeId){
        return ResponseEntity.ok(salaryService.getAverageSalary(employeeId));
    }


    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<SalaryResponseDTO>> getByEmployee(@PathVariable Long employeeId){
        List<SalaryResponseDTO> list=salaryService.getSalaryHistoryByEmployee(employeeId);
        return ResponseEntity.ok(list);
    }


    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<SalaryResponseDTO>> getByDepartment(@PathVariable Long departmentId){
        return ResponseEntity.ok(salaryService.getSalariesByDepartment(departmentId));
    }


    @Operation(summary = "Download salary report (PDF)")
    @GetMapping("/pdf/{employeeId}")
    public ResponseEntity<byte[]> downloadSalaryReport(@PathVariable Long employeeId){
        byte[] pdf=salaryService.generateSalaryReport(employeeId);
        return ResponseEntity.ok()
                .header("Content-Disposition","attachment; filename=salary_report_"+employeeId+".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


    @GetMapping("/range")
    public ResponseEntity<List<SalaryResponseDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        return ResponseEntity.ok(salaryService.getSalariesByDateRange(startDate, endDate));
    }


    @GetMapping("/max")
    public ResponseEntity<Double> getMaxSalary() {
        return ResponseEntity.ok(salaryService.getMaxSalary());
    }


    @GetMapping("/range/amount")
    public ResponseEntity<List<SalaryResponseDTO>> getByAmountRange(
            @RequestParam Double minAmount,
            @RequestParam Double maxAmount
    ){
        return ResponseEntity.ok(salaryService.findByAmountRange(minAmount,maxAmount));
    }


    @GetMapping("/with-bonus")
    public ResponseEntity<List<SalaryResponseDTO>> getWithBonus(){
        return ResponseEntity.ok(salaryService.findWithBonus());
    }


    @GetMapping("/top-bonus")
    public ResponseEntity<List<SalaryResponseDTO>> getTop10HighestBonuses(){
        return ResponseEntity.ok(salaryService.getTop10HighestBonus());
    }


    @GetMapping("/department/{departmentId}/max")
    public ResponseEntity<Double> getMaxSalaryByDepartment(@PathVariable Long departmentId){
        return ResponseEntity.ok(salaryService.getMaxSalaryByDepartment(departmentId));
    }


    @GetMapping("/top10")
    public ResponseEntity<List<SalaryResponseDTO>> getTop10HighestSalaries(){
        return ResponseEntity.ok(salaryService.getTop10HighestSalaries());
    }


    @GetMapping("/min")
    public ResponseEntity<Double> getMinSalary(){
        return ResponseEntity.ok(salaryService.getMinSalary());
    }


    @GetMapping("/monthly-stats")
    public ResponseEntity<List<SalaryMonthlyStatDTO>> getMonthlyStats(@RequestParam(required = false) Integer year){
        return ResponseEntity.ok(salaryService.getMonthlySalaryStats(year));
    }


    @GetMapping("/employee/{employeeId}/max")
    public ResponseEntity<Double> getMaxSalaryByEmployee(@PathVariable Long employeeId){
        return ResponseEntity.ok(salaryService.getMaxSalaryByEmployee(employeeId));
    }


    @GetMapping("/without-bonus")
    public ResponseEntity<List<SalaryResponseDTO>> getWithoutBonus(){
        return ResponseEntity.ok(salaryService.findWithoutBonus());
    }


    @Operation(summary = "Delete salary record")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        salaryService.delete(id);
        return ResponseEntity.ok("successful");
    }



}
