package com.muhammadali.employee_management.entity;
import com.muhammadali.employee_management.enums.Gender;
import com.muhammadali.employee_management.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Getter
@Setter
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private Gender gender;

    private Date birthDate;

    private Date hire_date;

    private Double salary;

    private String position;

    private Integer department_id;

    private Integer role_id;

    private Integer manager_id;

    private String image_path;

    private Status status;

    private Timestamp created_at;

    private Timestamp updated_at;
}
