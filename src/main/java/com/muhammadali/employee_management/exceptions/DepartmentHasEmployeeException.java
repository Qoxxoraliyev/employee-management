package com.muhammadali.employee_management.exceptions;

import lombok.Getter;

@Getter
public class DepartmentHasEmployeeException extends RuntimeException {


    private final String errorCode;

    private final String details;


    public DepartmentHasEmployeeException(String message){
        super(message);
        this.errorCode=null;
        this.details=null;
    }


    public DepartmentHasEmployeeException(String message,String errorCode){
        super(message);
        this.errorCode=errorCode;
        this.details=null;
    }


    public DepartmentHasEmployeeException(String message,String errorCode,String details){
        super(message);
        this.errorCode=errorCode;
        this.details=details;
    }


    public DepartmentHasEmployeeException(String message,Throwable cause){
        super(message,cause);
        this.errorCode=null;
        this.details=null;
    }


    public DepartmentHasEmployeeException(String message,String errorCode,String details,Throwable cause){
        super(message,cause);
        this.errorCode=errorCode;
        this.details=details;
    }


}
