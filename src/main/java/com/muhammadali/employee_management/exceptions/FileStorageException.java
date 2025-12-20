package com.muhammadali.employee_management.exceptions;

import lombok.Getter;

@Getter
public class FileStorageException extends RuntimeException {

    private final String errorCode;
    private final String details;


    public FileStorageException(String message){
        super(message);
        this.errorCode=null;
        this.details=null;
    }


    public FileStorageException(String message,String errorCode){
        super(message);
        this.errorCode=errorCode;
        this.details=null;
    }


    public FileStorageException(String message,String errorCode,String details){
        super(message);
        this.errorCode=errorCode;
        this.details=details;
    }


    public FileStorageException(String message,Throwable cause){
        super(message,cause);
        this.errorCode=null;
        this.details=null;
    }


    public FileStorageException(String message,String errorCode,String details,Throwable cause){
        super(message,cause);
        this.errorCode=errorCode;
        this.details=details;
    }



}
