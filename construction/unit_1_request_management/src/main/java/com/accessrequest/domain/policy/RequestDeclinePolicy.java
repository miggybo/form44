package com.accessrequest.domain.policy;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.valueobject.RequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RequestDeclinePolicy Domain Policy.
 * 
 * Validates decline rules and constraints.
 * Encapsulates business rules for request decline.
 */
@Slf4j
@Component
public class RequestDeclinePolicy {

    private static final int MIN_REASON_LENGTH = 10;
    private static final int MAX_REASON_LENGTH = 5000;

    /**
     * Check if a request can be declined.
     */
    public boolean canDecline(Request request, UUID declinerId) {
        if (request == null) {
            log.warn("Cannot decline: request is null");
            return false;
        }
        if (declinerId == null) {
            log.warn("Cannot decline: decliner ID is null");
            return false;
        }
        
        // Request must not be in a terminal state
        if (request.isTerminal()) {
            log.warn("Cannot decline request {}: already in terminal state {}", 
                    request.getRequestId().asString(), request.getStatus().getDisplayName());
            return false;
        }
        
        // Request must be in a pending state
        if (!request.isPending()) {
            log.warn("Cannot decline request {}: not in pending state {}", 
                    request.getRequestId().asString(), request.getStatus().getDisplayName());
            return false;
        }
        
        return true;
    }

    /**
     * Validate decline data.
     */
    public ValidationResult validateDeclineData(Request request, UUID declinerId, String reason) {
        ValidationResult result = new ValidationResult();
        
        if (request == null) {
            result.addError("request", "Request cannot be null");
            return result;
        }
        
        if (declinerId == null) {
            result.addError("declinerId", "Decliner ID cannot be null");
        }
        
        // Validate reason (mandatory)
        if (reason == null || reason.isBlank()) {
            result.addError("reason", "Decline reason is mandatory");
        } else if (reason.length() < MIN_REASON_LENGTH) {
            result.addError("reason", 
                String.format("Decline reason must be at least %d characters", MIN_REASON_LENGTH));
        } else if (reason.length() > MAX_REASON_LENGTH) {
            result.addError("reason", 
                String.format("Decline reason cannot exceed %d characters", MAX_REASON_LENGTH));
        }
        
        // Validate request state
        if (!canDecline(request, declinerId)) {
            result.addError("request", "Request cannot be declined in current state");
        }
        
        return result;
    }

    /**
     * Check if a declined request can be resubmitted.
     */
    public boolean canResubmitAfterDecline(Request request) {
        if (request == null) {
            return false;
        }
        
        // Declined requests cannot be resubmitted
        return false;
    }

    /**
     * Validation result holder.
     */
    public static class ValidationResult {
        private final Map<String, String> errors = new HashMap<>();
        
        public void addError(String field, String message) {
            errors.put(field, message);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public Map<String, String> getErrors() {
            return errors;
        }
    }

}
