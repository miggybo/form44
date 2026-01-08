package com.accessrequest.domain.aggregate;

import com.accessrequest.domain.entity.RequestApproval;
import com.accessrequest.domain.entity.RequestDocument;
import com.accessrequest.domain.entity.RequestHistory;
import com.accessrequest.domain.event.*;
import com.accessrequest.domain.valueobject.ApprovalComment;
import com.accessrequest.domain.valueobject.RequestId;
import com.accessrequest.domain.valueobject.RequestJustification;
import com.accessrequest.domain.valueobject.RequestStatus;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/**
 * Request Aggregate Root.
 * 
 * Represents a complete access request with its lifecycle management.
 * Encapsulates all business logic for request state transitions and approvals.
 */
@Getter
@ToString
public class Request implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final RequestId requestId;
    private final UUID requestorId;
    private final UUID accessTypeId;
    private final String systemName;
    private final RequestJustification justification;
    private RequestStatus status;
    private final Instant createdAt;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant implementedAt;
    private final UUID createdBy;
    private UUID updatedBy;
    private Instant updatedAt;
    
    private final List<RequestApproval> approvals;
    private final List<RequestHistory> history;
    private final List<RequestDocument> documents;
    private final List<DomainEvent> domainEvents;

    private Request(RequestId requestId, UUID requestorId, UUID accessTypeId, 
                   String systemName, RequestJustification justification) {
        this.requestId = requestId;
        this.requestorId = requestorId;
        this.accessTypeId = accessTypeId;
        this.systemName = systemName;
        this.justification = justification;
        this.status = RequestStatus.DRAFT;
        this.createdAt = Instant.now();
        this.createdBy = requestorId;
        this.updatedAt = Instant.now();
        this.approvals = new ArrayList<>();
        this.history = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Create a new Request.
     */
    public static Request create(UUID requestorId, UUID accessTypeId, String systemName, 
                                String justification) {
        if (requestorId == null) {
            throw new IllegalArgumentException("Requestor ID cannot be null");
        }
        if (accessTypeId == null) {
            throw new IllegalArgumentException("Access type ID cannot be null");
        }
        if (systemName == null || systemName.isBlank()) {
            throw new IllegalArgumentException("System name cannot be null or blank");
        }
        
        RequestId id = RequestId.generate();
        RequestJustification justif = RequestJustification.of(justification);
        
        Request request = new Request(id, requestorId, accessTypeId, systemName, justif);
        
        // Publish RequestCreated event
        request.domainEvents.add(new RequestCreated(
            id.getValue(),
            requestorId,
            "Requestor", // Will be populated by application service
            accessTypeId.toString(),
            systemName,
            justification
        ));
        
        return request;
    }

    /**
     * Submit request for initial approval.
     */
    public void submit(UUID headOfOfficeId) {
        if (!status.canTransitionTo(RequestStatus.PENDING_INITIAL_APPROVAL)) {
            throw new IllegalStateException(
                "Cannot submit request in " + status.getDisplayName() + " status"
            );
        }
        
        RequestStatus previousStatus = status;
        status = RequestStatus.PENDING_INITIAL_APPROVAL;
        submittedAt = Instant.now();
        updatedAt = Instant.now();
        
        // Add to history
        history.add(RequestHistory.stateTransition(
            requestorId,
            previousStatus,
            status,
            "Request submitted for approval"
        ));
        
        // Publish event
        domainEvents.add(new RequestSubmitted(
            requestId.getValue(),
            requestorId,
            headOfOfficeId,
            accessTypeId.toString(),
            systemName
        ));
    }

    /**
     * Approve by Head of Office.
     */
    public void approveByHeadOfOffice(UUID approverId, String comments) {
        if (!status.canTransitionTo(RequestStatus.PENDING_REVIEW)) {
            throw new IllegalStateException(
                "Cannot approve request in " + status.getDisplayName() + " status"
            );
        }
        
        RequestStatus previousStatus = status;
        status = RequestStatus.PENDING_REVIEW;
        updatedAt = Instant.now();
        updatedBy = approverId;
        
        // Add approval record
        ApprovalComment comment = comments != null && !comments.isBlank() 
            ? ApprovalComment.of(comments, approverId) 
            : ApprovalComment.empty();
        approvals.add(RequestApproval.create(
            approverId,
            RequestApproval.ApprovalType.HEAD_OF_OFFICE,
            RequestApproval.ApprovalStatus.APPROVED,
            comment
        ));
        
        // Add to history
        history.add(RequestHistory.stateTransition(
            approverId,
            previousStatus,
            status,
            comments
        ));
        
        // Publish event
        domainEvents.add(new RequestApprovedByHeadOfOffice(
            requestId.getValue(),
            approverId,
            "Head of Office", // Will be populated by application service
            comments
        ));
    }

    /**
     * Decline by Head of Office.
     */
    public void declineByHeadOfOffice(UUID declinerId, String reason) {
        if (!status.canTransitionTo(RequestStatus.DECLINED)) {
            throw new IllegalStateException(
                "Cannot decline request in " + status.getDisplayName() + " status"
            );
        }
        
        if (reason == null || reason.length() < 10) {
            throw new IllegalArgumentException("Decline reason must be at least 10 characters");
        }
        
        RequestStatus previousStatus = status;
        status = RequestStatus.DECLINED;
        updatedAt = Instant.now();
        updatedBy = declinerId;
        
        // Add approval record
        approvals.add(RequestApproval.create(
            declinerId,
            RequestApproval.ApprovalType.HEAD_OF_OFFICE,
            RequestApproval.ApprovalStatus.DECLINED,
            ApprovalComment.of(reason, declinerId)
        ));
        
        // Add to history
        history.add(RequestHistory.stateTransition(
            declinerId,
            previousStatus,
            status,
            reason
        ));
        
        // Publish event
        domainEvents.add(new RequestDeclined(
            requestId.getValue(),
            declinerId,
            "Head of Office",
            reason
        ));
    }

    /**
     * Endorse by Reviewer.
     */
    public void endorseByReviewer(UUID reviewerId, String comments) {
        if (!status.canTransitionTo(RequestStatus.PENDING_FINAL_APPROVAL)) {
            throw new IllegalStateException(
                "Cannot endorse request in " + status.getDisplayName() + " status"
            );
        }
        
        RequestStatus previousStatus = status;
        status = RequestStatus.PENDING_FINAL_APPROVAL;
        updatedAt = Instant.now();
        updatedBy = reviewerId;
        
        // Add approval record
        ApprovalComment comment = comments != null && !comments.isBlank() 
            ? ApprovalComment.of(comments, reviewerId) 
            : ApprovalComment.empty();
        approvals.add(RequestApproval.create(
            reviewerId,
            RequestApproval.ApprovalType.REVIEWER,
            RequestApproval.ApprovalStatus.APPROVED,
            comment
        ));
        
        // Add to history
        history.add(RequestHistory.stateTransition(
            reviewerId,
            previousStatus,
            status,
            comments
        ));
        
        // Publish event
        domainEvents.add(new RequestEndorsed(
            requestId.getValue(),
            reviewerId,
            "Reviewer",
            comments
        ));
    }

    /**
     * Decline by Reviewer.
     */
    public void declineByReviewer(UUID declinerId, String reason) {
        if (!status.canTransitionTo(RequestStatus.DECLINED)) {
            throw new IllegalStateException(
                "Cannot decline request in " + status.getDisplayName() + " status"
            );
        }
        
        if (reason == null || reason.length() < 10) {
            throw new IllegalArgumentException("Decline reason must be at least 10 characters");
        }
        
        RequestStatus previousStatus = status;
        status = RequestStatus.DECLINED;
        updatedAt = Instant.now();
        updatedBy = declinerId;
        
        // Add approval record
        approvals.add(RequestApproval.create(
            declinerId,
            RequestApproval.ApprovalType.REVIEWER,
            RequestApproval.ApprovalStatus.DECLINED,
            ApprovalComment.of(reason, declinerId)
        ));
        
        // Add to history
        history.add(RequestHistory.stateTransition(
            declinerId,
            previousStatus,
            status,
            reason
        ));
        
        // Publish event
        domainEvents.add(new RequestDeclined(
            requestId.getValue(),
            declinerId,
            "Reviewer",
            reason
        ));
    }

    /**
     * Approve by Head (final approval).
     */
    public void approveByHead(UUID approverId, String comments) {
        if (!status.canTransitionTo(RequestStatus.APPROVED)) {
            throw new IllegalStateException(
                "Cannot approve request in " + status.getDisplayName() + " status"
            );
        }
        
        RequestStatus previousStatus = status;
        status = RequestStatus.APPROVED;
        approvedAt = Instant.now();
        updatedAt = Instant.now();
        updatedBy = approverId;
        
        // Add approval record
        ApprovalComment comment = comments != null && !comments.isBlank() 
            ? ApprovalComment.of(comments, approverId) 
            : ApprovalComment.empty();
        approvals.add(RequestApproval.create(
            approverId,
            RequestApproval.ApprovalType.HEAD,
            RequestApproval.ApprovalStatus.APPROVED,
            comment
        ));
        
        // Add to history
        history.add(RequestHistory.stateTransition(
            approverId,
            previousStatus,
            status,
            comments
        ));
        
        // Publish event
        domainEvents.add(new RequestFinallyApproved(
            requestId.getValue(),
            approverId,
            "Head",
            comments
        ));
    }

    /**
     * Return to Reviewer.
     */
    public void returnToReviewer(UUID returnerId, String reason) {
        if (!status.canTransitionTo(RequestStatus.RETURNED_TO_REVIEWER)) {
            throw new IllegalStateException(
                "Cannot return request in " + status.getDisplayName() + " status"
            );
        }
        
        if (reason == null || reason.length() < 10) {
            throw new IllegalArgumentException("Return reason must be at least 10 characters");
        }
        
        RequestStatus previousStatus = status;
        status = RequestStatus.RETURNED_TO_REVIEWER;
        updatedAt = Instant.now();
        updatedBy = returnerId;
        
        // Add to history
        history.add(RequestHistory.stateTransition(
            returnerId,
            previousStatus,
            status,
            reason
        ));
        
        // Publish event
        domainEvents.add(new RequestReturned(
            requestId.getValue(),
            returnerId,
            "Head",
            reason
        ));
    }

    /**
     * Mark as Implemented.
     */
    public void markAsImplemented(UUID implementerId, String notes) {
        if (!status.canTransitionTo(RequestStatus.IMPLEMENTED)) {
            throw new IllegalStateException(
                "Cannot implement request in " + status.getDisplayName() + " status"
            );
        }
        
        RequestStatus previousStatus = status;
        status = RequestStatus.IMPLEMENTED;
        implementedAt = Instant.now();
        updatedAt = Instant.now();
        updatedBy = implementerId;
        
        // Add to history
        history.add(RequestHistory.stateTransition(
            implementerId,
            previousStatus,
            status,
            notes
        ));
        
        // Publish event
        domainEvents.add(new RequestImplemented(
            requestId.getValue(),
            implementerId,
            "Administrator",
            notes
        ));
    }

    /**
     * Add a document to the request.
     */
    public void addDocument(RequestDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        documents.add(document);
        updatedAt = Instant.now();
    }

    /**
     * Remove a document from the request.
     */
    public void removeDocument(UUID documentId) {
        documents.removeIf(doc -> doc.getDocumentId().equals(documentId));
        updatedAt = Instant.now();
    }

    /**
     * Get all domain events and clear the list.
     */
    public List<DomainEvent> getDomainEventsAndClear() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    /**
     * Check if request is in a terminal state.
     */
    public boolean isTerminal() {
        return status.isTerminal();
    }

    /**
     * Check if request is pending approval.
     */
    public boolean isPending() {
        return status.isPending();
    }

}
