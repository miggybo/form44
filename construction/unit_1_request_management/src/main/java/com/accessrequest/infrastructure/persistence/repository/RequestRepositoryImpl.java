package com.accessrequest.infrastructure.persistence.repository;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.repository.RequestRepository;
import com.accessrequest.infrastructure.persistence.jpa.RequestJpaEntity;
import com.accessrequest.infrastructure.persistence.mapper.RequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of RequestRepository using Spring Data JPA.
 * Bridges the domain layer with the infrastructure layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestRepositoryImpl implements RequestRepository {

    private final RequestJpaRepository jpaRepository;
    private final RequestMapper mapper;

    @Override
    public void save(Request request) {
        log.debug("Saving request with ID: {}", request.getRequestId());
        RequestJpaEntity entity = mapper.toDomain(request);
        jpaRepository.save(entity);
        log.debug("Request saved successfully");
    }

    @Override
    public void update(Request request) {
        log.debug("Updating request with ID: {}", request.getRequestId());
        RequestJpaEntity entity = mapper.toDomain(request);
        jpaRepository.save(entity);
        log.debug("Request updated successfully");
    }

    @Override
    public void delete(UUID requestId) {
        log.debug("Deleting request with ID: {}", requestId);
        jpaRepository.deleteById(requestId);
        log.debug("Request deleted successfully");
    }

    @Override
    public Optional<Request> findById(UUID requestId) {
        log.debug("Finding request by ID: {}", requestId);
        return jpaRepository.findById(requestId)
            .map(mapper::toDomain)
            .map(entity -> {
                log.debug("Request found: {}", requestId);
                return entity;
            });
    }

    @Override
    public List<Request> findByRequestor(UUID requestorId) {
        log.debug("Finding requests by requestor ID: {}", requestorId);
        return jpaRepository.findByRequestorId(requestorId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Request> findByStatus(String status) {
        log.debug("Finding requests by status: {}", status);
        RequestJpaEntity.RequestStatusEnum statusEnum = RequestJpaEntity.RequestStatusEnum.valueOf(status);
        return jpaRepository.findByStatus(statusEnum).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Request> findPendingForApprover(UUID approverId, String approvalType, Pageable pageable) {
        log.debug("Finding pending requests for approver: {} with type: {}", approverId, approvalType);
        RequestJpaEntity.RequestStatusEnum status = RequestJpaEntity.RequestStatusEnum.PENDING_INITIAL_APPROVAL;
        return jpaRepository.findPendingForApprover(approverId, status, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Page<Request> findByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        log.debug("Finding requests by date range: {} to {}", startDate, endDate);
        return jpaRepository.findByDateRange(startDate, endDate, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Page<Request> findByAccessType(UUID accessTypeId, Pageable pageable) {
        log.debug("Finding requests by access type: {}", accessTypeId);
        return jpaRepository.findByAccessTypeId(accessTypeId, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Page<Request> search(String query, Pageable pageable) {
        log.debug("Searching requests with query: {}", query);
        return jpaRepository.searchBySystemName(query, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Page<Request> findAll(Pageable pageable) {
        log.debug("Finding all requests with pagination: {}", pageable);
        return jpaRepository.findAll(pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Page<Request> findAll(Specification<Request> specification, Pageable pageable) {
        log.debug("Finding requests with specification and pagination");
        // Note: This requires converting domain Specification to JPA Specification
        // For now, we'll use a simple approach
        return jpaRepository.findAll(pageable)
            .map(mapper::toDomain);
    }

    @Override
    public long countByStatus(String status) {
        log.debug("Counting requests by status: {}", status);
        RequestJpaEntity.RequestStatusEnum statusEnum = RequestJpaEntity.RequestStatusEnum.valueOf(status);
        return jpaRepository.countByStatus(statusEnum);
    }
}
