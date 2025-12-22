package com.muhammadali.employee_management.entity;

import com.muhammadali.employee_management.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class Users implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3,max = 50,message = "Username must be between 3 and 50 characters")
    @Column(name = "username",nullable = false,unique = true,length = 50)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6,message = "Password must be at least 6 characters long")
    @Column(name = "password",nullable = false)
    private String password;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Column(name = "email" ,nullable = false,unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id",nullable = false)
    @NotNull(message = "Role must not be null ")
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="employee_id",unique = true)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;


    @Column(nullable = false)
    private Timestamp updated_at;

    @PrePersist
    protected void onCreate(){
        Timestamp now=new Timestamp(System.currentTimeMillis());
        this.updated_at=now;
    }

    @PreUpdate
    protected void onUpdate(){
        this.updated_at=new Timestamp(System.currentTimeMillis());
    }

}
