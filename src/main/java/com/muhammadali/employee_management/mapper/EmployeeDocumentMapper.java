package com.muhammadali.employee_management.mapper;

import com.muhammadali.employee_management.dto.EmployeeDocumentDTO;
import com.muhammadali.employee_management.entity.EmployeeDocument;



public class EmployeeDocumentMapper {


    public static EmployeeDocumentDTO toDTO(EmployeeDocument document){
        return new EmployeeDocumentDTO(
                document.getFileName(),
                document.getFileType(),
                document.getFileCategory(),
                document.getUploadedAt()
        );
    }



}
