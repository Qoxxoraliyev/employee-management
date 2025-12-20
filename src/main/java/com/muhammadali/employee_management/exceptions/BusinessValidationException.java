package com.muhammadali.employee_management.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessValidationException extends RuntimeException {
    private final String code;
    private final Map<String, Object> details;
    private final List<String> errors;

    public BusinessValidationException(String message) {
        super(message);
        this.code = "VALIDATION_ERROR";
        this.errors = Collections.singletonList(message);
        this.details = Collections.emptyMap();
    }

    public BusinessValidationException(List<String> errors) {
        super("Validation failed: " + errors.size() + " error(s)");
        this.code = "VALIDATION_ERROR";
        this.errors = errors;
        this.details = Collections.emptyMap();
    }

    public BusinessValidationException(String code, String message) {
        super(message);
        this.code = code;
        this.errors = Collections.singletonList(message);
        this.details = Collections.emptyMap();
    }

    public BusinessValidationException(String message, Map<String, Object> details) {
        super(message);
        this.code = "VALIDATION_ERROR";
        this.errors = Collections.singletonList(message);
        this.details = details != null ? Map.copyOf(details) : Collections.emptyMap();
    }

    public BusinessValidationException(String code, String message, List<String> errors, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
        this.details = details != null ? Map.copyOf(details) : Collections.emptyMap();
    }
}