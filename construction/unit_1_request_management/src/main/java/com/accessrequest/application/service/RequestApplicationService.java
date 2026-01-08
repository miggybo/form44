package com.accessrequest.application.service;

import com.accessrequest.application.dto.*;
import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.factory.RequestFactory;
import com.accessrequest.domain.repository.RequestRepository;
import com.accessrequest.domain.valueobject.RequestId;
import com.accessrequest.domain.valueobject.RequestJustification;
import com.accessrequest.infrastructure.event.DomainEventPublisher;
import com.accessrequest.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for request operations.
 * Orchestrates domain logic and manages transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestApplicationService {

    private final RequestRepository requestRepository;
    private final RequestFactory requestFactory;
    private final DomainEventPublisher eventPublisher;

    /**
     * Create a new request.
     *
     * @param request the create request DTO
     * @return the created request DTO
     */
    @Transactional
    public RequestDTO createRequest(CreateRequestRequest request) {
        log.debug("Creating new request for requestor: {}", request.getRequestorId());

        // Create domain aggregate
        Request newRequest = requestFactory.createRequest(
            request.getRequestorId(),
            request.getAccessType(),
            request.getSystemName(),
            new RequestJustification(request.getJustification())
        );

        // Save to repository
        requestRepository.save(newRequest);

        // Publish events
        eventPublisher.publishAll(newRequest.getDomainEvents());

        log.info("Request created successfully: {}", newRequest.getRequestId());
        return mapToDTO(newRequest);
    }

    /**
     * Get request by ID.
     *
     * @param requestId the request ID
     * @return the request DTO
     */
    @Transactional(readOnly = true)
    public RequestDTO getRequest(UUID requestId) {
        log.debug("Getting request: {}", requestId);

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request", requestId.toString()));

        return mapToDTO(request);
    }

    /**
     * List all requests with pagination.
     *
     * @param pageable pagination information
     * @return page of request DTOs
     */
    @Transactional(readOnly = true)
    public Page<RequestDTO> listRequests(Pageable pageable) {
        log.debug("Listing requests with pagination: {}", pageable);
        return requestRepository.findAll(pageable)
            .map(this::mapToDTO);
    }

    /**
     * Search requests by system name.
     *
     * @param query search query
     * @param pageable pagination information
     * @return page of request DTOs
     */
    @Transactional(readOnly = true)
    public Page<RequestDTO> searchRequests(String query, Pageable pageable) {
        log.debug("Searching requests with query: {}", query);
        return requestRepository.search(query, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Get request history.
     *
     * @param requestId the request ID
     * @return the request DTO with history
     */
    @Transactional(readOnly = true)
    public RequestDTO getRequestHistory(UUID requestId) {
        log.debug("Getting request history: {}", requestId);

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request", requestId.toString()));

        return mapToDTO(request);
    }

    /**
     * Map domain Request to RequestDTO.
     *
     * @param request the domain request
     * @return the request DTO
     */
    private RequestDTO mapToDTO(Request request) {
        return RequestDTO.builder()
            .requestId(request.getRequestId().getValue())
            .requestorId(request.getRequestorId())
            .accessType(request.getAccessTypeId().toString())
            .systemName(request.getSystemName())
            .justification(request.getJustification().getText())
            .status(request.getStatus().getValue())
            .createdAt(request.getCreatedAt())
            .submittedAt(request.getSubmittedAt())
            .approvedAt(request.getApprovedAt())
            .implementedAt(request.getImplementedAt())
            .build();
    }
}
