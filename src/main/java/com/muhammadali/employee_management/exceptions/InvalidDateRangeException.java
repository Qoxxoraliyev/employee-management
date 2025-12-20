package com.muhammadali.employee_management.exceptions;

import lombok.Getter;

@Getter
public class InvalidDateRangeException extends RuntimeException{

    private final String errorCode;
    private final String details;

    public InvalidDateRangeException(String message) {
        super(message);
        this.errorCode = null;
        this.details = null;
    }

    public InvalidDateRangeException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public InvalidDateRangeException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public InvalidDateRangeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.details = null;
    }

    public InvalidDateRangeException(String message, String errorCode, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}
