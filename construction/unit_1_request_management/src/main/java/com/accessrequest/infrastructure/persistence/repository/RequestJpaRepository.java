package com.accessrequest.infrastructure.persistence.repository;

import com.accessrequest.infrastructure.persistence.jpa.RequestJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Request aggregate persistence.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface RequestJpaRepository extends JpaRepository<RequestJpaEntity, UUID>, JpaSpecificationExecutor<RequestJpaEntity> {

    /**
     * Find all requests by requestor ID.
     *
     * @param requestorId the requestor ID
     * @param pageable pagination information
     * @return page of requests
     */
    Page<RequestJpaEntity> findByRequestorId(UUID requestorId, Pageable pageable);

    /**
     * Find all requests by status.
     *
     * @param status the request status
     * @param pageable pagination information
     * @return page of requests
     */
    Page<RequestJpaEntity> findByStatus(RequestJpaEntity.RequestStatusEnum status, Pageable pageable);

    /**
     * Find all requests by access type ID.
     *
     * @param accessTypeId the access type ID
     * @param pageable pagination information
     * @return page of requests
     */
    Page<RequestJpaEntity> findByAccessTypeId(UUID accessTypeId, Pageable pageable);

    /**
     * Find all requests created within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of requests
     */
    @Query("SELECT r FROM RequestJpaEntity r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    Page<RequestJpaEntity> findByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, Pageable pageable);

    /**
     * Find all pending requests for a specific approver.
     *
     * @param approverId the approver ID
     * @param status the request status
     * @param pageable pagination information
     * @return page of requests
     */
    @Query("SELECT DISTINCT r FROM RequestJpaEntity r " +
           "JOIN r.approvals a " +
           "WHERE a.approverId = :approverId AND r.status = :status")
    Page<RequestJpaEntity> findPendingForApprover(@Param("approverId") UUID approverId, 
                                                   @Param("status") RequestJpaEntity.RequestStatusEnum status, 
                                                   Pageable pageable);

    /**
     * Find all requests by system name (for search).
     *
     * @param systemName the system name (partial match)
     * @param pageable pagination information
     * @return page of requests
     */
    @Query("SELECT r FROM RequestJpaEntity r WHERE LOWER(r.systemName) LIKE LOWER(CONCAT('%', :systemName, '%'))")
    Page<RequestJpaEntity> searchBySystemName(@Param("systemName") String systemName, Pageable pageable);

    /**
     * Find all requests by requestor name (for search).
     *
     * @param requestorId the requestor ID
     * @param pageable pagination information
     * @return page of requests
     */
    Page<RequestJpaEntity> findByRequestorIdOrderByCreatedAtDesc(UUID requestorId, Pageable pageable);

    /**
     * Find all overdue requests (pending for more than specified days).
     *
     * @param status the request status
     * @param thresholdDate the threshold date
     * @param pageable pagination information
     * @return page of overdue requests
     */
    @Query("SELECT r FROM RequestJpaEntity r WHERE r.status = :status AND r.submittedAt < :thresholdDate")
    Page<RequestJpaEntity> findOverdueRequests(@Param("status") RequestJpaEntity.RequestStatusEnum status, 
                                               @Param("thresholdDate") Instant thresholdDate, 
                                               Pageable pageable);

    /**
     * Count requests by status.
     *
     * @param status the request status
     * @return count of requests
     */
    long countByStatus(RequestJpaEntity.RequestStatusEnum status);

    /**
     * Find all requests by requestor ID (unordered list).
     *
     * @param requestorId the requestor ID
     * @return list of requests
     */
    List<RequestJpaEntity> findByRequestorId(UUID requestorId);

    /**
     * Find all requests by status (unordered list).
     *
     * @param status the request status
     * @return list of requests
     */
    List<RequestJpaEntity> findByStatus(RequestJpaEntity.RequestStatusEnum status);
}
