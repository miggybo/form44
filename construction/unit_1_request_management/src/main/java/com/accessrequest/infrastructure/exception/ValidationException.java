package com.accessrequest.infrastructure.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when input validation fails.
 */
public class ValidationException extends ApplicationException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super("INVALID_INPUT", message);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super("INVALID_INPUT", message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void addFieldError(String field, String error) {
        fieldErrors.put(field, error);
    }
}
