package com.muhammadali.employee_management.exceptions;

import lombok.Getter;

@Getter
public class FileUploadException extends RuntimeException {

    private final String errorCode;
    private final String details;

    public FileUploadException(String message){
        super(message);
        this.errorCode=null;
        this.details=null;
    }

    public FileUploadException(String message,String errorCode){
        super(message);
        this.errorCode=errorCode;
        this.details=null;
    }

    public FileUploadException(String message,String errorCode,String details){
        super(message);
        this.errorCode=errorCode;
        this.details=details;
    }

}
