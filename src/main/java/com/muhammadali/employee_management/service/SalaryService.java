package com.muhammadali.employee_management.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.muhammadali.employee_management.dto.SalaryMonthlyStatDTO;
import com.muhammadali.employee_management.dto.SalaryRequestDTO;
import com.muhammadali.employee_management.dto.SalaryResponseDTO;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.entity.Salary;
import com.muhammadali.employee_management.exceptions.BusinessValidationException;
import com.muhammadali.employee_management.exceptions.InvalidDateRangeException;
import com.muhammadali.employee_management.exceptions.ResourceNotFoundException;
import com.muhammadali.employee_management.mapper.SalaryMapper;
import com.muhammadali.employee_management.repository.EmployeeRepository;
import com.muhammadali.employee_management.repository.SalaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private static final DecimalFormat MONEY_FORMAT=new DecimalFormat("#,##0.00");
    private static final String DEFAULT_CURRENCY="USD";


    @Transactional
    public SalaryResponseDTO save(SalaryRequestDTO dto) {
        Employee employee = employeeRepository.findById(dto.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", dto.employeeId()));
        Salary salary = SalaryMapper.toEntity(dto, employee);
        Salary saved = salaryRepository.save(salary);
        return SalaryMapper.toResponse(saved);
    }


    @Transactional
    public SalaryResponseDTO update(Long id, SalaryRequestDTO dto) {
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary", "id", id));
        Employee employee = getEmployeeById(dto.employeeId());
        salary.setEmployee(employee);
        salary.setAmount(dto.amount());
        salary.setCurrency(dto.currency());
        salary.setPaymentDate(dto.paymentDate());
        salary.setBonus(dto.bonus());
        salary.setUpdated_at(new Timestamp(System.currentTimeMillis()));
        return SalaryMapper.toResponse(salaryRepository.save(salary));
    }

    public List<SalaryResponseDTO> findAll(){
        return salaryRepository.findAll()
                .stream()
                .map(SalaryMapper::toResponse)
                .collect(Collectors.toList());
    }


    public List<SalaryResponseDTO> getSalariesByDepartment(Long departmentId) {
        List<Salary> salaries = salaryRepository.findByEmployee_Department_IdOrderByPaymentDateDesc(departmentId);
        if (salaries.isEmpty()) {
            throw new BusinessValidationException("This department has no salary records.");
        }
        return salaries.stream().map(SalaryMapper::toResponse).toList();
    }


    public List<SalaryResponseDTO> findByAmountRange(Double min, Double max) {
        if (min == null || max == null) {
            throw new BusinessValidationException("minAmount and maxAmount must not be null");
        }
        if (min > max) {
            throw new InvalidDateRangeException("minAmount cannot be greater than maxAmount");
        }
        return salaryRepository.findByAmountBetween(min, max)
                .stream().map(SalaryMapper::toResponse).toList();
    }


    public Double getMaxSalary(){
        Double max=salaryRepository.findMaxSalary();
        return max!=null?max:0.0;
    }


    public List<SalaryResponseDTO> getTop10HighestBonus(){
        return salaryRepository.findTop10ByOrderByBonusDesc()
                .stream()
                .map(SalaryMapper::toResponse)
                .toList();
    }


    public List<SalaryResponseDTO> getTop10HighestSalaries(){
        return salaryRepository.findTop10ByOrderByAmountDesc()
                .stream()
                .map(SalaryMapper::toResponse)
                .toList();
    }


    public List<SalaryMonthlyStatDTO> getMonthlySalaryStats(Integer year){
        if (year==null){
            year=java.time.Year.now().getValue();
        }
        return salaryRepository.getMonthlySalaryStats(year);
    }


    public Double getMinSalary(){
        Double minSalary=salaryRepository.findMinSalary();
        return minSalary!=null?minSalary:0.0;
    }


    public Double getMaxSalaryByEmployee(Long employeeId){
        Double max=salaryRepository.findMaxSalaryByEmployeeId(employeeId);
        return max!=null ?max:0.0;
    }


    public Double getMaxSalaryByDepartment(Long departmentId){
        Double max=salaryRepository.findMaxSalaryByDepartmentId(departmentId);
        return max!=null?max:0.0;
    }


    public List<SalaryResponseDTO> findWithBonus(){
        return salaryRepository.findAllWithPositiveBonus()
                .stream()
                .map(SalaryMapper::toResponse)
                .toList();
    }



    public List<SalaryResponseDTO> findWithoutBonus(){
        return salaryRepository.findByBonusIsNull()
                .stream()
                .map(SalaryMapper::toResponse)
                .toList();
    }


    public List<SalaryResponseDTO> getSalariesByDateRange(Date startDate,Date endDate){
        List<Salary> salaries=salaryRepository.findByPaymentDateBetween(startDate,endDate);
        return salaries.stream()
                .map(SalaryMapper::toResponse)
                .collect(Collectors.toList());
    }


    public List<SalaryResponseDTO> getSalaryHistoryByEmployee(Long employeeId){
        if (!employeeRepository.existsById(employeeId)){
            throw new RuntimeException("Employee not found with id= "+employeeId);
        }

        return salaryRepository.findByEmployee_IdOrderByPaymentDate(employeeId)
                .stream()
                .map(SalaryMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public SalaryResponseDTO addBonus(Long salaryId, Double bonusAmount) {
        if (bonusAmount == null || bonusAmount <= 0) {
            throw new BusinessValidationException("Bonus amount must be positive");
        }
        Salary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary", "id", salaryId));
        double existingBonus = salary.getBonus() != null ? salary.getBonus() : 0.0;
        salary.setBonus(existingBonus + bonusAmount);
        salary.setUpdated_at(new Timestamp(System.currentTimeMillis()));
        return SalaryMapper.toResponse(salaryRepository.save(salary));
    }


    public Double getAverageSalary(Long employeeId){
        Double avg=salaryRepository.findAverageSalaryByEmployeeId(employeeId);
        return avg!=null ? avg:0.0;
    }


    public byte[] generateSalaryReport(Long employeeId) {
        List<Salary> salaries = salaryRepository.findByEmployee_IdOrderByPaymentDate(employeeId);
        if (salaries.isEmpty()) {
            throw new IllegalArgumentException("This employee has no salary history.");
        }
        Employee employee = salaries.get(0).getEmployee();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Document document = new Document(PageSize.A4, 40, 40, 80, 60)) {
            PdfWriter.getInstance(document, baos);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(0, 51, 102));
            Paragraph title = new Paragraph("Monthly Salary Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(25);
            document.add(title);
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Employee: " + employee.getFirstName() + " " + employee.getLastName(), infoFont));
            document.add(new Paragraph("Department: " + employee.getDepartment().getName(), infoFont));
            document.add(new Paragraph("Position: " + (employee.getPosition() != null ? employee.getPosition() : "N/A"), infoFont));
            document.add(new Paragraph("Report Generated: " + new Date(), infoFont));
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{20, 20, 15, 15, 20});
            addTableHeader(table, "Payment Date", "Base Salary", "Currency", "Bonus", "Total Amount");
            for (Salary s : salaries) {
                table.addCell(formatDate(s.getPaymentDate()));
                table.addCell(formatMoney(s.getAmount()));
                table.addCell(s.getCurrency() != null ? s.getCurrency() : DEFAULT_CURRENCY);
                double bonus = s.getBonus() != null ? s.getBonus() : 0.0;
                table.addCell(formatMoney(bonus));
                double total = s.getAmount() + bonus;
                table.addCell(formatMoney(total));
            }
            document.add(table);
            double totalPaid = salaries.stream()
                    .mapToDouble(s -> s.getAmount() + (s.getBonus() != null ? s.getBonus() : 0.0))
                    .sum();
            Paragraph summary = new Paragraph("\nTotal Amount Paid: " + formatMoney(totalPaid) + " " + DEFAULT_CURRENCY,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
            summary.setAlignment(Element.ALIGN_RIGHT);
            document.add(summary);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating salary report PDF", e);
        }
    }


    @Transactional
    public void delete(Long id) {
        if (!salaryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Salary", "id", id);
        }
        salaryRepository.deleteById(id);
    }


    private Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }


    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new Color(200, 200, 255));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }


    private String formatMoney(Double amount){
        if (amount==null) return "0.00";
        return MONEY_FORMAT.format(amount);
    }


    private String formatDate(Date date) {
        return date == null
                ? "-"
                : date.toInstant()
                .atZone(ZoneId.of("Asia/Tashkent"))
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }


}


