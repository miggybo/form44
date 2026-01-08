package com.accessrequest.application.service;

import com.accessrequest.application.dto.*;
import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.repository.RequestRepository;
import com.accessrequest.domain.service.RequestWorkflowService;
import com.accessrequest.domain.valueobject.ApprovalComment;
import com.accessrequest.infrastructure.event.DomainEventPublisher;
import com.accessrequest.infrastructure.exception.BusinessRuleException;
import com.accessrequest.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for request approval operations.
 * Manages the approval workflow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestApprovalApplicationService {

    private final RequestRepository requestRepository;
    private final RequestWorkflowService workflowService;
    private final DomainEventPublisher eventPublisher;

    /**
     * Approve a request.
     *
     * @param requestId the request ID
     * @param request the approve request DTO
     * @return the updated request DTO
     */
    @Transactional
    public RequestDTO approveRequest(UUID requestId, ApproveRequestRequest request) {
        log.debug("Approving request: {} by approver: {}", requestId, request.getApproverId());

        Request domainRequest = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request", requestId.toString()));

        try {
            // Approve by Head of Office
            ApprovalComment comment = request.getComments() != null 
                ? new ApprovalComment(request.getComments()) 
                : null;
            
            workflowService.approveByHeadOfOffice(domainRequest, request.getApproverId(), comment);

            // Save updated request
            requestRepository.update(domainRequest);

            // Publish events
            eventPublisher.publishAll(domainRequest.getDomainEvents());

            log.info("Request approved successfully: {}", requestId);
            return mapToDTO(domainRequest);
        } catch (Exception e) {
            log.error("Error approving request: {}", requestId, e);
            throw new BusinessRuleException("Failed to approve request: " + e.getMessage());
        }
    }

    /**
     * Decline a request.
     *
     * @param requestId the request ID
     * @param request the decline request DTO
     * @return the updated request DTO
     */
    @Transactional
    public RequestDTO declineRequest(UUID requestId, DeclineRequestRequest request) {
        log.debug("Declining request: {} by decliner: {}", requestId, request.getDeclinerId());

        Request domainRequest = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request", requestId.toString()));

        try {
            workflowService.declineByHeadOfOffice(domainRequest, request.getDeclinerId(), request.getReason());

            // Save updated request
            requestRepository.update(domainRequest);

            // Publish events
            eventPublisher.publishAll(domainRequest.getDomainEvents());

            log.info("Request declined successfully: {}", requestId);
            return mapToDTO(domainRequest);
        } catch (Exception e) {
            log.error("Error declining request: {}", requestId, e);
            throw new BusinessRuleException("Failed to decline request: " + e.getMessage());
        }
    }

    /**
     * Endorse a request.
     *
     * @param requestId the request ID
     * @param request the endorse request DTO
     * @return the updated request DTO
     */
    @Transactional
    public RequestDTO endorseRequest(UUID requestId, EndorseRequestRequest request) {
        log.debug("Endorsing request: {} by reviewer: {}", requestId, request.getReviewerId());

        Request domainRequest = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request", requestId.toString()));

        try {
            ApprovalComment comment = request.getComments() != null 
                ? new ApprovalComment(request.getComments()) 
                : null;
            
            workflowService.endorseByReviewer(domainRequest, request.getReviewerId(), comment);

            // Save updated request
            requestRepository.update(domainRequest);

            // Publish events
            eventPublisher.publishAll(domainRequest.getDomainEvents());

            log.info("Request endorsed successfully: {}", requestId);
            return mapToDTO(domainRequest);
        } catch (Exception e) {
            log.error("Error endorsing request: {}", requestId, e);
            throw new BusinessRuleException("Failed to endorse request: " + e.getMessage());
        }
    }

    /**
     * Return a request to reviewer.
     *
     * @param requestId the request ID
     * @param request the return request DTO
     * @return the updated request DTO
     */
    @Transactional
    public RequestDTO returnRequest(UUID requestId, ReturnRequestRequest request) {
        log.debug("Returning request: {} by returner: {}", requestId, request.getReturnerId());

        Request domainRequest = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request", requestId.toString()));

        try {
            workflowService.returnToReviewer(domainRequest, request.getReturnerId(), request.getReason());

            // Save updated request
            requestRepository.update(domainRequest);

            // Publish events
            eventPublisher.publishAll(domainRequest.getDomainEvents());

            log.info("Request returned successfully: {}", requestId);
            return mapToDTO(domainRequest);
        } catch (Exception e) {
            log.error("Error returning request: {}", requestId, e);
            throw new BusinessRuleException("Failed to return request: " + e.getMessage());
        }
    }

    /**
     * Mark a request as implemented.
     *
     * @param requestId the request ID
     * @param request the implement request DTO
     * @return the updated request DTO
     */
    @Transactional
    public RequestDTO implementRequest(UUID requestId, ImplementRequestRequest request) {
        log.debug("Implementing request: {} by implementer: {}", requestId, request.getImplementerId());

        Request domainRequest = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request", requestId.toString()));

        try {
            workflowService.markAsImplemented(domainRequest, request.getImplementerId(), request.getNotes());

            // Save updated request
            requestRepository.update(domainRequest);

            // Publish events
            eventPublisher.publishAll(domainRequest.getDomainEvents());

            log.info("Request implemented successfully: {}", requestId);
            return mapToDTO(domainRequest);
        } catch (Exception e) {
            log.error("Error implementing request: {}", requestId, e);
            throw new BusinessRuleException("Failed to implement request: " + e.getMessage());
        }
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
