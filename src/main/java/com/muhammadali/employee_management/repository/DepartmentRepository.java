package com.muhammadali.employee_management.repository;
import com.muhammadali.employee_management.dto.DepartmentEmployeeCountDTO;
import com.muhammadali.employee_management.dto.DepartmentYearlyStatsDTO;
import com.muhammadali.employee_management.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {


    List<Department> findByNameContainingIgnoreCase(String name);
    List<Department> findByManagerId(Integer managerId);
    List<Department> findByCreatedAtGreaterThanEqual(Timestamp start);
    List<Department> findByCreatedAtLessThanEqual(Timestamp end);
    List<Department> findByCreatedAtBetween(Timestamp start, Timestamp end);



    @Query("""
        SELECT new com.muhammadali.employee_management.dto.DepartmentEmployeeCountDTO(
            d.id, d.name, COUNT(e)
        )
        FROM Department d
        LEFT JOIN d.employees e
        GROUP BY d.id, d.name
        """)
    List<DepartmentEmployeeCountDTO> getDepartmentEmployeeCounts();


    @Query(value = """
            SELECT EXTRACT(YEAR FROM created_at) AS year, COUNT(*) AS employeeCount
            FROM employee
            GROUP BY EXTRACT(YEAR FROM created_at)
            ORDER BY EXTRACT(YEAR FROM created_at)
            """, nativeQuery = true)
    List<DepartmentYearlyStatsDTO> getGlobalYearlyHires();


    @Query(value = """
           SELECT 
           CAST(EXTRACT(YEAR FROM e.created_at) AS INTEGER) AS year,
           COUNT(*) AS employeeCount
           FROM employee e
           WHERE e.department_id = :departmentId
           GROUP BY year
           ORDER BY year
           """, nativeQuery = true)
    List<DepartmentYearlyStatsDTO> getYearlyHiresByDepartment(
            @Param("departmentId") Long departmentId
    );



}
