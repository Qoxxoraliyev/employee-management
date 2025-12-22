package com.muhammadali.employee_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_documents")
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "File name cannot be blank")
    @Column(name = "file_name",nullable = false)
    private String fileName;

    @NotBlank(message = "File type cannot be blank")
    @Column(name = "file_type",nullable = false)
    private String fileType;

    @NotBlank(message = "File category cannot be blank")
    @Column(name = "file_category",nullable = false)
    private String fileCategory;

    @NotBlank(message = "File path cannot be blank")
    @Column(name = "file_path",nullable = false)
    private String filePath;

    @Column(name = "uploaded_at",updatable = false)
    private Timestamp uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id",nullable = false)
    @NotNull(message = "Employee cannot be null")
    private Employee employee;

    @PrePersist
    protected void onUpload(){
        uploadedAt=new Timestamp(System.currentTimeMillis());
    }

}
