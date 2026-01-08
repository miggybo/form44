package com.accessrequest.domain.policy;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.valueobject.RequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RequestApprovalPolicy Domain Policy.
 * 
 * Validates approval rules and constraints.
 * Encapsulates business rules for request approval.
 */
@Slf4j
@Component
public class RequestApprovalPolicy {

    /**
     * Check if a request can be approved.
     */
    public boolean canApprove(Request request, UUID approverId) {
        if (request == null) {
            log.warn("Cannot approve: request is null");
            return false;
        }
        if (approverId == null) {
            log.warn("Cannot approve: approver ID is null");
            return false;
        }
        
        // Request must have valid justification
        if (request.getJustification() == null) {
            log.warn("Cannot approve request {}: no justification", request.getRequestId().asString());
            return false;
        }
        
        // Request must be in a valid state for approval
        RequestStatus status = request.getStatus();
        boolean validStatus = status == RequestStatus.PENDING_INITIAL_APPROVAL ||
                            status == RequestStatus.PENDING_REVIEW ||
                            status == RequestStatus.PENDING_FINAL_APPROVAL ||
                            status == RequestStatus.RETURNED_TO_REVIEWER;
        
        if (!validStatus) {
            log.warn("Cannot approve request {}: invalid status {}", 
                    request.getRequestId().asString(), status.getDisplayName());
            return false;
        }
        
        return true;
    }

    /**
     * Validate approval data.
     */
    public ValidationResult validateApprovalData(Request request, UUID approverId, String comments) {
        ValidationResult result = new ValidationResult();
        
        if (request == null) {
            result.addError("request", "Request cannot be null");
            return result;
        }
        
        if (approverId == null) {
            result.addError("approverId", "Approver ID cannot be null");
        }
        
        // Validate comments if provided
        if (comments != null && comments.length() > 5000) {
            result.addError("comments", "Comments cannot exceed 5000 characters");
        }
        
        // Validate request state
        if (!canApprove(request, approverId)) {
            result.addError("request", "Request cannot be approved in current state");
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
