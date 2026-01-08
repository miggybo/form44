package com.accessrequest.domain.repository;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.valueobject.RequestId;
import com.accessrequest.domain.valueobject.RequestStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RequestRepository Interface.
 * 
 * Defines persistence operations for Request aggregates.
 */
public interface RequestRepository {

    /**
     * Save a request.
     */
    void save(Request request);

    /**
     * Update a request.
     */
    void update(Request request);

    /**
     * Find request by ID.
     */
    Optional<Request> findById(RequestId requestId);

    /**
     * Find request by UUID.
     */
    Optional<Request> findById(UUID requestId);

    /**
     * Find all requests by requestor.
     */
    List<Request> findByRequestor(UUID requestorId);

    /**
     * Find all requests by status.
     */
    List<Request> findByStatus(RequestStatus status);

    /**
     * Find pending requests for approver.
     */
    List<Request> findPendingForApprover(UUID approverId, String approvalType);

    /**
     * Find requests by date range.
     */
    List<Request> findByDateRange(Instant startDate, Instant endDate);

    /**
     * Find requests by access type.
     */
    List<Request> findByAccessType(UUID accessTypeId);

    /**
     * Search requests by criteria.
     */
    List<Request> search(String query);

    /**
     * Delete a request.
     */
    void delete(RequestId requestId);

}
