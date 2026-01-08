package com.accessrequest.infrastructure.exception;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Converts exceptions to standardized HTTP responses.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle ApplicationException.
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(
        ApplicationException ex,
        WebRequest request) {
        
        log.error("Application exception: {}", ex.getErrorCode(), ex);
        
        HttpStatus status = switch (ex.getErrorCode()) {
            case "RESOURCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INVALID_INPUT" -> HttpStatus.BAD_REQUEST;
            case "CONFLICT" -> HttpStatus.CONFLICT;
            case "BUSINESS_RULE_VIOLATION" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        if (ex instanceof ValidationException validationEx) {
            errorResponse.setDetails(validationEx.getFieldErrors());
        }

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        WebRequest request) {
        
        log.error("Validation exception", ex);
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("INVALID_INPUT")
            .message("Input validation failed")
            .details(fieldErrors)
            .timestamp(Instant.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        WebRequest request) {
        
        log.error("Unexpected exception", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .timestamp(Instant.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Error response DTO.
     */
    @Data
    public static class ErrorResponse {
        private String code;
        private String message;
        private Map<String, String> details;
        private Instant timestamp;
        private String path;

        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        public static class ErrorResponseBuilder {
            private String code;
            private String message;
            private Map<String, String> details;
            private Instant timestamp;
            private String path;

            public ErrorResponseBuilder code(String code) {
                this.code = code;
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ErrorResponseBuilder details(Map<String, String> details) {
                this.details = details;
                return this;
            }

            public ErrorResponseBuilder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public ErrorResponseBuilder path(String path) {
                this.path = path;
                return this;
            }

            public ErrorResponse build() {
                ErrorResponse response = new ErrorResponse();
                response.code = this.code;
                response.message = this.message;
                response.details = this.details;
                response.timestamp = this.timestamp;
                response.path = this.path;
                return response;
            }
        }
    }
}
