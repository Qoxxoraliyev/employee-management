package com.muhammadali.employee_management.specification;

import com.muhammadali.employee_management.entity.Employee;
import com.muhammadali.employee_management.enums.Status;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class EmployeeSpecification {

    public static Specification<Employee> hasName(String name){
        return (root,query,builder)->
                name==null ? null :
                        builder.or(
                                builder.like(builder.lower(root.get("firstName")),"%"+name.toLowerCase()+"%"),
                                builder.like(builder.lower(root.get("lastName")),"%"+name.toLowerCase()+"%")
                        );
    }



    public static Specification<Employee> hasDepartment(String department){
        return (root,query,builder)->
                department==null ? null :
                        builder.equal(root.get("department").get("name"),department);
    }



    public static Specification<Employee> hasStatus(Status status){
        return (root,query,builder)->
                status==null ? null :
                        builder.equal(root.get("status"),status);
    }


    public static Specification<Employee> hasAgeBetween(Integer minAge,Integer maxAge){
        return (root,query,builder)->{
            if (minAge==null && maxAge==null) return null;

            LocalDate today=LocalDate.now();
            Date maxBirthDate=maxAge!=null
                    ?Date.from(today.minusYears(minAge).atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : null;

            Date minBirthDate=minAge!=null
                    ?Date.from(today.minusYears(maxAge).atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : null;

            if (minBirthDate!=null && maxBirthDate!=null){
                return builder.between(root.get("birthDate"),minBirthDate,maxBirthDate);
            } else if (minBirthDate!=null) {
                return builder.greaterThanOrEqualTo(root.get("birthDate"),maxBirthDate);
            }
            else {
                return builder.lessThanOrEqualTo(root.get("birthDate"),maxBirthDate);
            }
        };
    }

}
