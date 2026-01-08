package com.accessrequest.domain.aggregate;

import com.accessrequest.domain.event.*;
import com.accessrequest.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Request Aggregate.
 * Tests state transitions, invariants, and business rules.
 */
@DisplayName("Request Aggregate Tests")
class RequestAggregateTest {

    private Request request;
    private RequestId requestId;
    private UUID requestorId;
    private UUID headOfOfficeId;
    private UUID reviewerId;
    private UUID headId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        requestId = RequestId.generate();
        requestorId = UUID.randomUUID();
        headOfOfficeId = UUID.randomUUID();
        reviewerId = UUID.randomUUID();
        headId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        request = Request.builder()
                .requestId(requestId)
                .requestorId(requestorId)
                .accessType("OS")
                .systemName("Linux Server")
                .justification(new RequestJustification("Need access for development work"))
                .status(RequestStatus.DRAFT)
                .build();
    }

    @Test
    @DisplayName("Should create request in Draft status")
    void testCreateRequest() {
        assertEquals(RequestStatus.DRAFT, request.getStatus());
        assertEquals(requestorId, request.getRequestorId());
        assertNotNull(request.getCreatedAt());
        assertTrue(request.getDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Should submit request and transition to PendingInitialApproval")
    void testSubmitRequest() {
        request.submit(headOfOfficeId);

        assertEquals(RequestStatus.PENDING_INITIAL_APPROVAL, request.getStatus());
        assertNotNull(request.getSubmittedAt());
        assertEquals(1, request.getDomainEvents().size());
        assertTrue(request.getDomainEvents().get(0) instanceof RequestSubmitted);
    }

    @Test
    @DisplayName("Should fail to submit request twice")
    void testSubmitRequestTwice() {
        request.submit(headOfOfficeId);
        
        assertThrows(IllegalStateException.class, () -> request.submit(headOfOfficeId));
    }

    @Test
    @DisplayName("Should approve by Head of Office and transition to PendingReview")
    void testApproveByHeadOfOffice() {
        request.submit(headOfOfficeId);
        request.getDomainEvents().clear();

        request.approveByHeadOfOffice(headOfOfficeId, new ApprovalComment("Approved"));

        assertEquals(RequestStatus.PENDING_REVIEW, request.getStatus());
        assertEquals(1, request.getDomainEvents().size());
        assertTrue(request.getDomainEvents().get(0) instanceof RequestApprovedByHeadOfOffice);
    }

    @Test
    @DisplayName("Should decline by Head of Office and transition to Declined")
    void testDeclineByHeadOfOffice() {
        request.submit(headOfOfficeId);
        request.getDomainEvents().clear();

        request.declineByHeadOfOffice(headOfOfficeId, "Does not meet requirements");

        assertEquals(RequestStatus.DECLINED, request.getStatus());
        assertEquals(1, request.getDomainEvents().size());
        assertTrue(request.getDomainEvents().get(0) instanceof RequestDeclined);
    }

    @Test
    @DisplayName("Should endorse by Reviewer and transition to PendingFinalApproval")
    void testEndorseByReviewer() {
        request.submit(headOfOfficeId);
        request.approveByHeadOfOffice(headOfOfficeId, new ApprovalComment("Approved"));
        request.getDomainEvents().clear();

        request.endorseByReviewer(reviewerId, new ApprovalComment("Endorsed"));

        assertEquals(RequestStatus.PENDING_FINAL_APPROVAL, request.getStatus());
        assertEquals(1, request.getDomainEvents().size());
        assertTrue(request.getDomainEvents().get(0) instanceof RequestEndorsed);
    }

    @Test
    @DisplayName("Should decline by Reviewer and transition to Declined")
    void testDeclineByReviewer() {
        request.submit(headOfOfficeId);
        request.approveByHeadOfOffice(headOfOfficeId, new ApprovalComment("Approved"));
        request.getDomainEvents().clear();

        request.declineByReviewer(reviewerId, "Needs more information");

        assertEquals(RequestStatus.DECLINED, request.getStatus());
        assertEquals(1, request.getDomainEvents().size());
        assertTrue(request.getDomainEvents().get(0) instanceof RequestDeclined);
    }

    @Test
    @DisplayName("Should approve by Head and transition to Approved")
    void testApproveByHead() {
        request.submit(headOfOfficeId);
        request.approveByHeadOfOffice(headOfOfficeId, new ApprovalComment("Approved"));
        request.endorseByReviewer(reviewerId, new ApprovalComment("Endorsed"));
        request.getDomainEvents().clear();

        request.approveByHead(headId, new ApprovalComment("Finally approved"));

        assertEquals(RequestStatus.APPROVED, request.getStatus());
        assertEquals(1, request.getDomainEvents().size());
        assertTrue(request.getDomainEvents().get(0) instanceof RequestFinallyApproved);
    }

    @Test
    @DisplayName("Should return to Reviewer and transition to ReturnedToReviewer")
    void testReturnToReviewer() {
        request.submit(headOfOfficeId);
        request.approveByHeadOfOffice(headOfOfficeId, new ApprovalComment("Approved"));
        request.endorseByReviewer(reviewerId, new ApprovalComment("Endorsed"));
        request.getDomainEvents().clear();

        request.returnToReviewer(headId, "Need more information");

        assertEquals(RequestStatus.RETURNED_TO_REVIEWER, request.getStatus());
        assertEquals(1, request.getDomainEvents().size());
        assertTrue(request.getDomainEvents().get(0) instanceof RequestReturned);
    }

    @Test
    @DisplayName("Should mark as Implemented and transition to Implemented")
    void testMarkAsImplemented() {
        request.submit(headOfOfficeId);
        request.approveByHeadOfOffice(headOfOfficeId, new ApprovalComment("Approved"));
        request.endorseByReviewer(reviewerId, new ApprovalComment("Endorsed"));
        request.approveByHead(headId, new ApprovalComment("Finally approved"));
        request.getDomainEvents().clear();

        request.markAsImplemented(adminId, "Access granted");

        assertEquals(RequestStatus.IMPLEMENTED, request.getStatus());
        assertNotNull(request.getImplementedAt());
        assertEquals(1, request.getDomainEvents().size());
        assertTrue(request.getDomainEvents().get(0) instanceof RequestImplemented);
    }

    @Test
    @DisplayName("Should not allow invalid state transitions")
    void testInvalidStateTransition() {
        request.submit(headOfOfficeId);
        
        // Cannot endorse without approval
        assertThrows(IllegalStateException.class, () -> 
            request.endorseByReviewer(reviewerId, new ApprovalComment("Endorsed")));
    }

    @Test
    @DisplayName("Should track approval chain")
    void testApprovalChain() {
        request.submit(headOfOfficeId);
        request.approveByHeadOfOffice(headOfOfficeId, new ApprovalComment("Approved"));
        request.endorseByReviewer(reviewerId, new ApprovalComment("Endorsed"));
        request.approveByHead(headId, new ApprovalComment("Finally approved"));

        assertEquals(3, request.getApprovals().size());
    }

    @Test
    @DisplayName("Should maintain audit trail")
    void testAuditTrail() {
        request.submit(headOfOfficeId);
        request.approveByHeadOfOffice(headOfOfficeId, new ApprovalComment("Approved"));

        assertEquals(2, request.getHistory().size());
    }

    @Test
    @DisplayName("Should identify terminal states")
    void testTerminalStates() {
        request.submit(headOfOfficeId);
        request.approveByHeadOfOffice(headOfOfficeId, new ApprovalComment("Approved"));
        request.endorseByReviewer(reviewerId, new ApprovalComment("Endorsed"));
        request.approveByHead(headId, new ApprovalComment("Finally approved"));
        request.markAsImplemented(adminId, "Access granted");

        assertTrue(request.isTerminal());
    }

    @Test
    @DisplayName("Should identify pending states")
    void testPendingStates() {
        request.submit(headOfOfficeId);
        
        assertTrue(request.isPending());
    }

    @Test
    @DisplayName("Should validate justification")
    void testJustificationValidation() {
        assertThrows(IllegalArgumentException.class, () ->
            new RequestJustification("Short"));
    }

    @Test
    @DisplayName("Should generate domain events")
    void testDomainEventGeneration() {
        request.submit(headOfOfficeId);
        
        assertEquals(1, request.getDomainEvents().size());
        RequestSubmitted event = (RequestSubmitted) request.getDomainEvents().get(0);
        assertEquals(requestId, event.getRequestId());
    }

    @Test
    @DisplayName("Should clear domain events after publishing")
    void testClearDomainEvents() {
        request.submit(headOfOfficeId);
        assertEquals(1, request.getDomainEvents().size());
        
        request.clearDomainEvents();
        assertEquals(0, request.getDomainEvents().size());
    }
}
