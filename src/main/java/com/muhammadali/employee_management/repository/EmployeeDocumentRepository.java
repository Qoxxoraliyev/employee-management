package com.muhammadali.employee_management.repository;

import com.muhammadali.employee_management.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument,Long> {

    List<EmployeeDocument> findByEmployee_Id(Long employeeId);

    List<EmployeeDocument> findByEmployee_IdAndFileCategory(Long employeeId,String category);

}
