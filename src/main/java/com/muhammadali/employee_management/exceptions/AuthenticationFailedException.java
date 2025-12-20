package com.muhammadali.employee_management.exceptions;

import lombok.Getter;

@Getter
public class AuthenticationFailedException extends RuntimeException {

    private final String errorCode;
    private final String details;

    public AuthenticationFailedException(String message) {
        super(message);
        this.errorCode = "AUTH_FAILED";
        this.details = null;
    }

    public AuthenticationFailedException(String message, String details) {
        super(message);
        this.errorCode = "AUTH_FAILED";
        this.details = details;
    }

    public AuthenticationFailedException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_FAILED";
        this.details = null;
    }

    public AuthenticationFailedException(String message, String errorCode, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}
