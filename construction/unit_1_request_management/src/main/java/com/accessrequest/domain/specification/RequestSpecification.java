package com.accessrequest.domain.specification;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.valueobject.RequestStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for Request specifications.
 * 
 * Specifications are used to encapsulate query logic.
 * They define criteria for filtering requests.
 */
public interface RequestSpecification {

    /**
     * Check if a request satisfies this specification.
     */
    boolean isSatisfiedBy(Request request);

}

/**
 * Specification for requests by status.
 */
class RequestsByStatusSpecification implements RequestSpecification {
    private final RequestStatus status;

    public RequestsByStatusSpecification(RequestStatus status) {
        this.status = status;
    }

    @Override
    public boolean isSatisfiedBy(Request request) {
        return request != null && request.getStatus() == status;
    }
}

/**
 * Specification for requests by requestor.
 */
class RequestsByRequestorSpecification implements RequestSpecification {
    private final UUID requestorId;

    public RequestsByRequestorSpecification(UUID requestorId) {
        this.requestorId = requestorId;
    }

    @Override
    public boolean isSatisfiedBy(Request request) {
        return request != null && request.getRequestorId().equals(requestorId);
    }
}

/**
 * Specification for pending requests for approver.
 */
class PendingRequestsForApproverSpecification implements RequestSpecification {
    private final UUID approverId;
    private final String approvalType;

    public PendingRequestsForApproverSpecification(UUID approverId, String approvalType) {
        this.approverId = approverId;
        this.approvalType = approvalType;
    }

    @Override
    public boolean isSatisfiedBy(Request request) {
        if (request == null || !request.isPending()) {
            return false;
        }
        
        // Check if approver has already approved this request
        return request.getApprovals().stream()
            .noneMatch(approval -> approval.getApproverId().equals(approverId));
    }
}

/**
 * Specification for requests by date range.
 */
class RequestsByDateRangeSpecification implements RequestSpecification {
    private final Instant startDate;
    private final Instant endDate;

    public RequestsByDateRangeSpecification(Instant startDate, Instant endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public boolean isSatisfiedBy(Request request) {
        if (request == null || request.getCreatedAt() == null) {
            return false;
        }
        
        Instant createdAt = request.getCreatedAt();
        return !createdAt.isBefore(startDate) && !createdAt.isAfter(endDate);
    }
}

/**
 * Specification for requests by access type.
 */
class RequestsByAccessTypeSpecification implements RequestSpecification {
    private final UUID accessTypeId;

    public RequestsByAccessTypeSpecification(UUID accessTypeId) {
        this.accessTypeId = accessTypeId;
    }

    @Override
    public boolean isSatisfiedBy(Request request) {
        return request != null && request.getAccessTypeId().equals(accessTypeId);
    }
}

/**
 * Specification for overdue requests.
 */
class OverdueRequestsSpecification implements RequestSpecification {
    private final Instant threshold;

    public OverdueRequestsSpecification(Instant threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isSatisfiedBy(Request request) {
        if (request == null || !request.isPending()) {
            return false;
        }
        
        // Request is overdue if submitted before threshold
        return request.getSubmittedAt() != null && request.getSubmittedAt().isBefore(threshold);
    }
}
