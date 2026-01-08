package com.accessrequest.domain.factory;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.valueobject.RequestJustification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RequestFactory Domain Factory.
 * 
 * Creates new Request aggregates with validation.
 * Encapsulates request creation logic.
 */
@Slf4j
@Component
public class RequestFactory {

    /**
     * Create a new request.
     */
    public Request createRequest(UUID requestorId, UUID accessTypeId, String systemName, 
                                String justification) {
        // Validate inputs
        if (requestorId == null) {
            throw new IllegalArgumentException("Requestor ID cannot be null");
        }
        if (accessTypeId == null) {
            throw new IllegalArgumentException("Access type ID cannot be null");
        }
        if (systemName == null || systemName.isBlank()) {
            throw new IllegalArgumentException("System name cannot be null or blank");
        }
        if (justification == null || justification.isBlank()) {
            throw new IllegalArgumentException("Justification cannot be null or blank");
        }
        
        // Validate justification length
        try {
            RequestJustification.of(justification);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid justification: " + e.getMessage(), e);
        }
        
        // Create request
        Request request = Request.create(requestorId, accessTypeId, systemName, justification);
        
        log.info("Created new request: {} for requestor: {}", 
                request.getRequestId().asString(), requestorId);
        
        return request;
    }

    /**
     * Validate request creation data.
     */
    public ValidationResult validateRequestCreation(UUID requestorId, UUID accessTypeId, 
                                                    String systemName, String justification) {
        ValidationResult result = new ValidationResult();
        
        // Validate requestor ID
        if (requestorId == null) {
            result.addError("requestorId", "Requestor ID cannot be null");
        }
        
        // Validate access type ID
        if (accessTypeId == null) {
            result.addError("accessTypeId", "Access type ID cannot be null");
        }
        
        // Validate system name
        if (systemName == null || systemName.isBlank()) {
            result.addError("systemName", "System name cannot be null or blank");
        } else if (systemName.length() > 255) {
            result.addError("systemName", "System name cannot exceed 255 characters");
        }
        
        // Validate justification
        if (justification == null || justification.isBlank()) {
            result.addError("justification", "Justification cannot be null or blank");
        } else if (justification.length() < 10) {
            result.addError("justification", "Justification must be at least 10 characters");
        } else if (justification.length() > 10 * 1024 * 1024) {
            result.addError("justification", "Justification cannot exceed 10MB");
        }
        
        return result;
    }

    /**
     * Validation result holder.
     */
    public static class ValidationResult {
        private final java.util.Map<String, String> errors = new java.util.HashMap<>();
        
        public void addError(String field, String message) {
            errors.put(field, message);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public java.util.Map<String, String> getErrors() {
            return errors;
        }
    }

}
