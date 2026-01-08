package com.accessrequest.api.controller;

import com.accessrequest.application.dto.*;
import com.accessrequest.application.service.RequestApplicationService;
import com.accessrequest.application.service.RequestApprovalApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST Controller for Request Management operations.
 * Handles all request-related endpoints including creation, submission, approval, and querying.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
@Tag(name = "Request Management", description = "APIs for managing access requests")
@SecurityRequirement(name = "bearer-jwt")
public class RequestController {

    private final RequestApplicationService requestApplicationService;
    private final RequestApprovalApplicationService requestApprovalApplicationService;

    /**
     * Create a new access request.
     *
     * @param createRequestRequest the request creation details
     * @return the created request with 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create a new access request", description = "Creates a new access request in Draft status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Request created successfully",
                    content = @Content(schema = @Schema(implementation = RequestDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<RequestDTO> createRequest(@Valid @RequestBody CreateRequestRequest createRequestRequest) {
        log.info("Creating new request for requestor: {}", createRequestRequest.getRequestorId());
        RequestDTO createdRequest = requestApplicationService.createRequest(createRequestRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    /**
     * Get request details by ID.
     *
     * @param requestId the request ID
     * @return the request details
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Get request details", description = "Retrieves detailed information about a specific request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request found",
                    content = @Content(schema = @Schema(implementation = RequestDTO.class))),
            @ApiResponse(responseCode = "404", description = "Request not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<RequestDTO> getRequest(
            @Parameter(description = "Request ID", required = true)
            @PathVariable UUID requestId) {
        log.info("Fetching request: {}", requestId);
        RequestDTO request = requestApplicationService.getRequest(requestId);
        return ResponseEntity.ok(request);
    }

    /**
     * List requests with filtering and pagination.
     *
     * @param status filter by status
     * @param requestorId filter by requestor
     * @param accessType filter by access type
     * @param fromDate filter by start date
     * @param toDate filter by end date
     * @param page page number (0-indexed)
     * @param size page size
     * @param sortBy sort field
     * @param sortOrder sort order (ASC/DESC)
     * @return paginated list of requests
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "List requests", description = "Retrieves a paginated list of requests with optional filtering")
    @ApiResponse(responseCode = "200", description = "Requests retrieved successfully")
    public ResponseEntity<Page<RequestDTO>> listRequests(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by requestor ID")
            @RequestParam(required = false) UUID requestorId,
            @Parameter(description = "Filter by access type")
            @RequestParam(required = false) String accessType,
            @Parameter(description = "Filter from date (ISO-8601)")
            @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "Filter to date (ISO-8601)")
            @RequestParam(required = false) LocalDateTime toDate,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort order")
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        log.info("Listing requests with filters - status: {}, requestorId: {}, page: {}, size: {}", 
                status, requestorId, page, size);

        Sort.Direction direction = Sort.Direction.fromString(sortOrder.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<RequestDTO> requests = requestApplicationService.listRequests(
                status, requestorId, accessType, fromDate, toDate, pageable);

        return ResponseEntity.ok(requests);
    }

    /**
     * Submit a request for approval.
     *
     * @param requestId the request ID
     * @param submitRequest the submission details
     * @return the updated request
     */
    @PutMapping("/{requestId}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Submit request for approval", description = "Submits a draft request to the Head of Office for initial approval")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid state transition"),
            @ApiResponse(responseCode = "404", description = "Request not found"),
            @ApiResponse(responseCode = "409", description = "Request already submitted")
    })
    public ResponseEntity<RequestDTO> submitRequest(
            @Parameter(description = "Request ID", required = true)
            @PathVariable UUID requestId,
            @Valid @RequestBody CreateRequestRequest submitRequest) {

        log.info("Submitting request: {} to Head of Office: {}", requestId, submitRequest.getRequestorId());
        RequestDTO updatedRequest = requestApplicationService.submitRequest(requestId, submitRequest);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Approve a request.
     *
     * @param requestId the request ID
     * @param approveRequest the approval details
     * @return the updated request
     */
    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasAnyRole('HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Approve request", description = "Approves a request at the current approval stage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request approved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid approval"),
            @ApiResponse(responseCode = "404", description = "Request not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to approve")
    })
    public ResponseEntity<RequestDTO> approveRequest(
            @Parameter(description = "Request ID", required = true)
            @PathVariable UUID requestId,
            @Valid @RequestBody ApproveRequestRequest approveRequest) {

        log.info("Approving request: {} by approver: {}", requestId, approveRequest.getApproverId());
        RequestDTO updatedRequest = requestApprovalApplicationService.approveRequest(requestId, approveRequest);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Decline a request.
     *
     * @param requestId the request ID
     * @param declineRequest the decline details
     * @return the updated request
     */
    @PutMapping("/{requestId}/decline")
    @PreAuthorize("hasAnyRole('HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Decline request", description = "Declines a request with a reason")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request declined successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid decline"),
            @ApiResponse(responseCode = "404", description = "Request not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to decline")
    })
    public ResponseEntity<RequestDTO> declineRequest(
            @Parameter(description = "Request ID", required = true)
            @PathVariable UUID requestId,
            @Valid @RequestBody DeclineRequestRequest declineRequest) {

        log.info("Declining request: {} by decliner: {}", requestId, declineRequest.getDeclinerId());
        RequestDTO updatedRequest = requestApprovalApplicationService.declineRequest(requestId, declineRequest);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Endorse a request.
     *
     * @param requestId the request ID
     * @param endorseRequest the endorsement details
     * @return the updated request
     */
    @PutMapping("/{requestId}/endorse")
    @PreAuthorize("hasRole('REVIEWER')")
    @Operation(summary = "Endorse request", description = "Endorses a request as SMD/RDC Reviewer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request endorsed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid endorsement"),
            @ApiResponse(responseCode = "404", description = "Request not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to endorse")
    })
    public ResponseEntity<RequestDTO> endorseRequest(
            @Parameter(description = "Request ID", required = true)
            @PathVariable UUID requestId,
            @Valid @RequestBody EndorseRequestRequest endorseRequest) {

        log.info("Endorsing request: {} by reviewer: {}", requestId, endorseRequest.getReviewerId());
        RequestDTO updatedRequest = requestApprovalApplicationService.endorseRequest(requestId, endorseRequest);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Return a request to reviewer.
     *
     * @param requestId the request ID
     * @param returnRequest the return details
     * @return the updated request
     */
    @PutMapping("/{requestId}/return")
    @PreAuthorize("hasRole('HEAD')")
    @Operation(summary = "Return request to reviewer", description = "Returns a request to SMD/RDC Reviewer for revision")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid return"),
            @ApiResponse(responseCode = "404", description = "Request not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to return")
    })
    public ResponseEntity<RequestDTO> returnRequest(
            @Parameter(description = "Request ID", required = true)
            @PathVariable UUID requestId,
            @Valid @RequestBody ReturnRequestRequest returnRequest) {

        log.info("Returning request: {} by head: {}", requestId, returnRequest.getReturnerId());
        RequestDTO updatedRequest = requestApprovalApplicationService.returnRequest(requestId, returnRequest);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Mark a request as implemented.
     *
     * @param requestId the request ID
     * @param implementRequest the implementation details
     * @return the updated request
     */
    @PutMapping("/{requestId}/implement")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark request as implemented", description = "Marks an approved request as implemented by administrator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request marked as implemented"),
            @ApiResponse(responseCode = "400", description = "Invalid implementation"),
            @ApiResponse(responseCode = "404", description = "Request not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to implement")
    })
    public ResponseEntity<RequestDTO> implementRequest(
            @Parameter(description = "Request ID", required = true)
            @PathVariable UUID requestId,
            @Valid @RequestBody ImplementRequestRequest implementRequest) {

        log.info("Implementing request: {} by admin: {}", requestId, implementRequest.getImplementerId());
        RequestDTO updatedRequest = requestApprovalApplicationService.implementRequest(requestId, implementRequest);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Get request history/audit trail.
     *
     * @param requestId the request ID
     * @return the request history
     */
    @GetMapping("/{requestId}/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Get request history", description = "Retrieves the complete audit trail of a request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<Page<HistoryDTO>> getRequestHistory(
            @Parameter(description = "Request ID", required = true)
            @PathVariable UUID requestId,
            @Parameter(description = "Page number")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "50") int size) {

        log.info("Fetching history for request: {}", requestId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<HistoryDTO> history = requestApplicationService.getRequestHistory(requestId, pageable);
        return ResponseEntity.ok(history);
    }
}
