package com.muhammadali.employee_management.entity;
import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name cannot be blank")
    @Column(name = "first_name",nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Past(message = "Birth date must be in the past")
    @Temporal(TemporalType.DATE)
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "hireDate")
    private LocalDate hireDate;


    @Column(name = "position")
    private String position;

    @ManyToOne
    @JoinColumn(name = "department_id",nullable = false)
    private Department department;


    @Column(name = "image_path")
    private String image_path;


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "created_at",updatable = false)
    private Timestamp created_at;

    @Column(name = "updated_at")
    private Timestamp updated_at;

    @PrePersist
    protected void onCreate(){
        created_at=new Timestamp(System.currentTimeMillis());
        updated_at=created_at;
    }

    @PreUpdate
    protected void onUpdate(){
        updated_at=new Timestamp(System.currentTimeMillis());
    }

}
