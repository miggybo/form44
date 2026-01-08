package com.accessrequest.domain.service;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.valueobject.RequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * RequestWorkflowService Domain Service.
 * 
 * Orchestrates request approval workflow transitions.
 * Encapsulates complex business logic for state management.
 */
@Slf4j
@Service
public class RequestWorkflowService {

    /**
     * Submit request for initial approval.
     */
    public void submitForInitialApproval(Request request, UUID headOfOfficeId) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (headOfOfficeId == null) {
            throw new IllegalArgumentException("Head of Office ID cannot be null");
        }
        
        if (!request.getStatus().equals(RequestStatus.DRAFT)) {
            throw new IllegalStateException(
                "Cannot submit request in " + request.getStatus().getDisplayName() + " status"
            );
        }
        
        request.submit(headOfOfficeId);
        log.info("Request {} submitted for initial approval", request.getRequestId().asString());
    }

    /**
     * Approve request by Head of Office.
     */
    public void approveByHeadOfOffice(Request request, UUID approverId, String comments) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (approverId == null) {
            throw new IllegalArgumentException("Approver ID cannot be null");
        }
        
        if (!request.getStatus().equals(RequestStatus.PENDING_INITIAL_APPROVAL)) {
            throw new IllegalStateException(
                "Cannot approve request in " + request.getStatus().getDisplayName() + " status"
            );
        }
        
        request.approveByHeadOfOffice(approverId, comments);
        log.info("Request {} approved by Head of Office", request.getRequestId().asString());
    }

    /**
     * Decline request by Head of Office.
     */
    public void declineByHeadOfOffice(Request request, UUID declinerId, String reason) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (declinerId == null) {
            throw new IllegalArgumentException("Decliner ID cannot be null");
        }
        
        if (!request.getStatus().equals(RequestStatus.PENDING_INITIAL_APPROVAL)) {
            throw new IllegalStateException(
                "Cannot decline request in " + request.getStatus().getDisplayName() + " status"
            );
        }
        
        request.declineByHeadOfOffice(declinerId, reason);
        log.info("Request {} declined by Head of Office", request.getRequestId().asString());
    }

    /**
     * Endorse request by Reviewer.
     */
    public void endorseByReviewer(Request request, UUID reviewerId, String comments) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (reviewerId == null) {
            throw new IllegalArgumentException("Reviewer ID cannot be null");
        }
        
        if (!request.getStatus().equals(RequestStatus.PENDING_REVIEW)) {
            throw new IllegalStateException(
                "Cannot endorse request in " + request.getStatus().getDisplayName() + " status"
            );
        }
        
        request.endorseByReviewer(reviewerId, comments);
        log.info("Request {} endorsed by Reviewer", request.getRequestId().asString());
    }

    /**
     * Decline request by Reviewer.
     */
    public void declineByReviewer(Request request, UUID declinerId, String reason) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (declinerId == null) {
            throw new IllegalArgumentException("Decliner ID cannot be null");
        }
        
        if (!request.getStatus().equals(RequestStatus.PENDING_REVIEW)) {
            throw new IllegalStateException(
                "Cannot decline request in " + request.getStatus().getDisplayName() + " status"
            );
        }
        
        request.declineByReviewer(declinerId, reason);
        log.info("Request {} declined by Reviewer", request.getRequestId().asString());
    }

    /**
     * Approve request by Head (final approval).
     */
    public void approveByHead(Request request, UUID approverId, String comments) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (approverId == null) {
            throw new IllegalArgumentException("Approver ID cannot be null");
        }
        
        if (!request.getStatus().equals(RequestStatus.PENDING_FINAL_APPROVAL)) {
            throw new IllegalStateException(
                "Cannot approve request in " + request.getStatus().getDisplayName() + " status"
            );
        }
        
        request.approveByHead(approverId, comments);
        log.info("Request {} finally approved by Head", request.getRequestId().asString());
    }

    /**
     * Return request to Reviewer.
     */
    public void returnToReviewer(Request request, UUID returnerId, String reason) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (returnerId == null) {
            throw new IllegalArgumentException("Returner ID cannot be null");
        }
        
        if (!request.getStatus().equals(RequestStatus.PENDING_FINAL_APPROVAL)) {
            throw new IllegalStateException(
                "Cannot return request in " + request.getStatus().getDisplayName() + " status"
            );
        }
        
        request.returnToReviewer(returnerId, reason);
        log.info("Request {} returned to Reviewer", request.getRequestId().asString());
    }

    /**
     * Mark request as implemented.
     */
    public void markAsImplemented(Request request, UUID implementerId, String notes) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (implementerId == null) {
            throw new IllegalArgumentException("Implementer ID cannot be null");
        }
        
        if (!request.getStatus().equals(RequestStatus.APPROVED)) {
            throw new IllegalStateException(
                "Cannot implement request in " + request.getStatus().getDisplayName() + " status"
            );
        }
        
        request.markAsImplemented(implementerId, notes);
        log.info("Request {} marked as implemented", request.getRequestId().asString());
    }

}
