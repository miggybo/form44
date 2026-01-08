package com.accessrequest.infrastructure.exception;

/**
 * Exception thrown when a resource conflict occurs.
 */
public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super("CONFLICT", message);
    }

    public ConflictException(String message, Throwable cause) {
        super("CONFLICT", message, cause);
    }
}
