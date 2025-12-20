package com.muhammadali.employee_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Setter;


import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "salaries")
public class Salary implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id",nullable = false)
    @NotNull(message = "Employee cannot be null")
    private Employee employee;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    @Column(name = "amount",nullable = false)
    private Double amount;

    @NotNull(message = "Currency cannot be null")
    @Column(name = "currency",length = 10,nullable = false)
    private String currency;

    @Temporal(TemporalType.DATE)
    @NotNull(message = "Payment date cannot be null")
    @Column(name = "payment_date",nullable = false)
    private Date paymentDate;

    @PositiveOrZero(message = "Bonus must be zero or positive")
    @Column(name = "bonus")
    private Double bonus;

    @Column(name = "created_at",updatable = false)
    private Timestamp created_at;

    @Column(name = "update_at")
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
