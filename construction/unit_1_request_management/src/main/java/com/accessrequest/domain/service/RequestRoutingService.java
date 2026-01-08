package com.accessrequest.domain.service;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.valueobject.RequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * RequestRoutingService Domain Service.
 * 
 * Determines the next approver based on request state and access type.
 * Encapsulates routing logic for the approval workflow.
 */
@Slf4j
@Service
public class RequestRoutingService {

    /**
     * Get the next approver for a request based on its current status.
     * 
     * This would typically call the Administration Service to get user information.
     * For now, returns null as it requires external service integration.
     */
    public UUID getNextApprover(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        RequestStatus status = request.getStatus();
        
        return switch (status) {
            case DRAFT -> null; // No approver needed for draft
            case PENDING_INITIAL_APPROVAL -> null; // Would get Head of Office from Administration Service
            case PENDING_REVIEW -> null; // Would get SMD/RDC Head from Administration Service
            case PENDING_FINAL_APPROVAL -> null; // Would get Administrator from Administration Service
            case RETURNED_TO_REVIEWER -> null; // Would get SMD/RDC Reviewer from Administration Service
            case APPROVED, DECLINED, IMPLEMENTED -> null; // Terminal states
        };
    }

    /**
     * Get the next approval stage for a request.
     */
    public String getNextApprovalStage(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        RequestStatus status = request.getStatus();
        
        return switch (status) {
            case DRAFT -> "HeadOfOffice";
            case PENDING_INITIAL_APPROVAL -> "Reviewer";
            case PENDING_REVIEW -> "Head";
            case PENDING_FINAL_APPROVAL -> "Administrator";
            case RETURNED_TO_REVIEWER -> "Reviewer";
            case APPROVED -> "Administrator";
            case DECLINED, IMPLEMENTED -> "None";
        };
    }

    /**
     * Check if a status transition is valid.
     */
    public boolean isValidTransition(RequestStatus currentStatus, RequestStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        return currentStatus.canTransitionTo(newStatus);
    }

    /**
     * Get the Head of Office for a requestor.
     * 
     * This would call the Administration Service to get the office hierarchy.
     * For now, returns null as it requires external service integration.
     */
    public UUID getHeadOfOfficeForRequestor(UUID requestorId) {
        if (requestorId == null) {
            throw new IllegalArgumentException("Requestor ID cannot be null");
        }
        
        // TODO: Call Administration Service to get Head of Office
        log.debug("Getting Head of Office for requestor: {}", requestorId);
        return null;
    }

    /**
     * Get the Reviewer for an access type.
     * 
     * This would call the Administration Service to get the reviewer.
     * For now, returns null as it requires external service integration.
     */
    public UUID getReviewerForAccessType(UUID accessTypeId) {
        if (accessTypeId == null) {
            throw new IllegalArgumentException("Access type ID cannot be null");
        }
        
        // TODO: Call Administration Service to get Reviewer
        log.debug("Getting Reviewer for access type: {}", accessTypeId);
        return null;
    }

    /**
     * Get the SMD/RDC Head.
     * 
     * This would call the Administration Service to get the head.
     * For now, returns null as it requires external service integration.
     */
    public UUID getSmdHeadId() {
        // TODO: Call Administration Service to get SMD/RDC Head
        log.debug("Getting SMD/RDC Head");
        return null;
    }

    /**
     * Get the Administrator for an access type.
     * 
     * This would call the Administration Service to get the administrator.
     * For now, returns null as it requires external service integration.
     */
    public UUID getAdministratorForAccessType(UUID accessTypeId) {
        if (accessTypeId == null) {
            throw new IllegalArgumentException("Access type ID cannot be null");
        }
        
        // TODO: Call Administration Service to get Administrator
        log.debug("Getting Administrator for access type: {}", accessTypeId);
        return null;
    }

}
