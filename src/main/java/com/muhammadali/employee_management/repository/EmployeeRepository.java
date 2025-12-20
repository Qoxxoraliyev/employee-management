package com.muhammadali.employee_management.repository;
import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long>,
        JpaSpecificationExecutor<Employee> {

    List<Employee> findByStatus(Status status);

    List<Employee> findByDepartmentName(String departmentName);

    Page<Employee> findAll(Pageable pageable);

    long countByDepartment_Id(Long departmentId);

    long count();

    long countByStatus(Status status);

    long countByHireDateAfter(LocalDate date);

    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    List<Employee> findByPhoneContainingIgnoreCase(String phone);

    List<Employee> findByGender(Gender gender);

    List<Employee> findByBirthDateBetween(LocalDate from, LocalDate to);

    @Query("""
        SELECT e FROM Employee e 
        WHERE TRIM(UPPER(e.department.name)) = TRIM(UPPER(:departmentName))
          AND TRIM(UPPER(e.position)) = TRIM(UPPER(:position))
        """)
    List<Employee> findByDepartmentNameAndPositionIgnoreCase(
            @Param("departmentName") String departmentName,
            @Param("position") String position
    );

    @Query("SELECT s.employee FROM Salary s WHERE s.amount BETWEEN :min AND :max")
    List<Employee> findEmployeeBySalaryBetween(@Param("min") Double min, @Param("max") Double max);


    List<Employee> findByStatusAndDepartmentName(Status status,String departmentName);


    List<Employee> findByDepartmentId(Long departmentId);

    @Query("SELECT AVG(s.amount) FROM Salary s JOIN s.employee e WHERE e.department.id=:deptId")
    Double findAverageSalaryByDepartmentId(@Param("deptId") Long deptId);


    @Query("SELECT MAX(s.amount) " +
            "FROM Salary s " +
            "JOIN s.employee e " +
            "WHERE e.department.id = :deptId")
    Double findMaxSalaryByDepartmentId(@Param("deptId") Long deptId);



    @Query(value = """
    SELECT DISTINCT ON (e.id) e.*
    FROM salaries s
    JOIN employee e ON s.employee_id = e.id
    ORDER BY e.id, s.amount DESC
    """, nativeQuery = true)
    List<Employee> findTopEmployeesBySalary(Pageable pageable);



    @Query("SELECT e FROM Employee e WHERE e.hireDate BETWEEN :from AND :to")
    List<Employee> findByHireDateBetween(@Param("from") LocalDate from,
                                         @Param("to") LocalDate to);




    @Query("SELECT MIN(s.amount) "+
    "FROM Salary s "+
    "JOIN s.employee e "+
    "WHERE e.department.id=:deptId")
    Double findMinSalaryByDepartmentId(@Param("deptId") Long deptId);



    @Query("""
        SELECT COUNT(DISTINCT e.position)
        FROM Employee e
        WHERE e.department.id = :departmentId
        """)
    long countPositionByDepartmentId(@Param("departmentId") Long departmentId);



}
