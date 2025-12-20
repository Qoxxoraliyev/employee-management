package com.muhammadali.employee_management.repository;

import com.muhammadali.employee_management.dto.SalaryMonthlyStatDTO;
import com.muhammadali.employee_management.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



import java.util.Date;
import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<Salary,Long> {

    List<Salary> findByEmployee_IdOrderByPaymentDate(Long employeeId);

    @Query("SELECT AVG(s.amount) FROM Salary s WHERE s.employee.id=:employeeId")
    Double findAverageSalaryByEmployeeId(Long employeeId);

    List<Salary> findByEmployee_Department_IdOrderByPaymentDateDesc(Long departmentId);

    @Query("SELECT s FROM Salary s WHERE s.amount BETWEEN :min AND :max ORDER BY s.amount ASC")
    List<Salary> findByAmountBetween(Double min,Double max);

    List<Salary> findByPaymentDateBetween(Date startDate,Date endDate);

    @Query("SELECT s FROM Salary s WHERE s.bonus IS NOT NULL AND s.bonus > 0")
    List<Salary> findAllWithPositiveBonus();


    List<Salary> findByBonusIsNull();

    @Query("SELECT MAX(s.amount) FROM Salary s")
    Double findMaxSalary();

    @Query("SELECT MAX(s.amount) FROM Salary s WHERE s.employee.id=:employeeId")
    Double findMaxSalaryByEmployeeId(Long employeeId);

    @Query("""
            SELECT MAX(s.amount)
            FROM Salary s
            WHERE s.employee.department.id=:departmentId
            """)
    Double findMaxSalaryByDepartmentId(Long departmentId);

    @Query("SELECT MIN(s.amount) FROM Salary s")
    Double findMinSalary();


    @Query("""
            SELECT new com.muhammadali.employee_management.dto.SalaryMonthlyStatDTO(
                    YEAR(s.paymentDate),
                    MONTH(s.paymentDate),
                    SUM(s.amount + COALESCE(s.bonus, 0))
                )
                FROM Salary s
                WHERE YEAR(s.paymentDate)=:year
                GROUP BY YEAR(s.paymentDate), MONTH(s.paymentDate)
                ORDER BY MONTH(s.paymentDate)
            """)
    List<SalaryMonthlyStatDTO> getMonthlySalaryStats(@Param("year") Integer year);

    List<Salary> findTop10ByOrderByAmountDesc();

    List<Salary> findTop10ByOrderByBonusDesc();
}
