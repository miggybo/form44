package com.accessrequest.domain.factory;

import com.accessrequest.domain.entity.RequestHistory;
import com.accessrequest.domain.valueobject.RequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RequestHistoryFactory Domain Factory.
 * 
 * Creates RequestHistory entries for audit trail.
 * Encapsulates history entry creation logic.
 */
@Slf4j
@Component
public class RequestHistoryFactory {

    /**
     * Create a history entry for a state transition.
     */
    public RequestHistory createStateTransitionEntry(UUID requestId, UUID actorId, 
                                                     RequestStatus previousStatus, 
                                                     RequestStatus newStatus, 
                                                     String comments) {
        // Validate inputs
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null");
        }
        if (actorId == null) {
            throw new IllegalArgumentException("Actor ID cannot be null");
        }
        if (previousStatus == null) {
            throw new IllegalArgumentException("Previous status cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        
        // Validate transition
        if (!previousStatus.canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                String.format("Invalid transition from %s to %s", 
                    previousStatus.getDisplayName(), newStatus.getDisplayName())
            );
        }
        
        RequestHistory entry = RequestHistory.stateTransition(actorId, previousStatus, newStatus, comments);
        
        log.debug("Created history entry for request {}: {} -> {}", 
                requestId, previousStatus.getDisplayName(), newStatus.getDisplayName());
        
        return entry;
    }

    /**
     * Create a history entry for an action without state change.
     */
    public RequestHistory createActionEntry(UUID requestId, String action, UUID actorId, 
                                           RequestStatus currentStatus, String comments) {
        // Validate inputs
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be null or blank");
        }
        if (actorId == null) {
            throw new IllegalArgumentException("Actor ID cannot be null");
        }
        if (currentStatus == null) {
            throw new IllegalArgumentException("Current status cannot be null");
        }
        
        RequestHistory entry = RequestHistory.action(action, actorId, currentStatus, comments);
        
        log.debug("Created history entry for request {}: {}", requestId, action);
        
        return entry;
    }

    /**
     * Create a history entry for approval.
     */
    public RequestHistory createApprovalEntry(UUID requestId, UUID approverId, 
                                             String approvalType, String comments) {
        // Validate inputs
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null");
        }
        if (approverId == null) {
            throw new IllegalArgumentException("Approver ID cannot be null");
        }
        if (approvalType == null || approvalType.isBlank()) {
            throw new IllegalArgumentException("Approval type cannot be null or blank");
        }
        
        String action = "Approved by " + approvalType;
        RequestHistory entry = RequestHistory.create(action, approverId, null, null, comments);
        
        log.debug("Created approval history entry for request {}: {}", requestId, approvalType);
        
        return entry;
    }

    /**
     * Create a history entry for decline.
     */
    public RequestHistory createDeclineEntry(UUID requestId, UUID declinerId, 
                                            String declineReason) {
        // Validate inputs
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null");
        }
        if (declinerId == null) {
            throw new IllegalArgumentException("Decliner ID cannot be null");
        }
        if (declineReason == null || declineReason.isBlank()) {
            throw new IllegalArgumentException("Decline reason cannot be null or blank");
        }
        
        String action = "Declined";
        RequestHistory entry = RequestHistory.create(action, declinerId, null, null, declineReason);
        
        log.debug("Created decline history entry for request {}", requestId);
        
        return entry;
    }

}
