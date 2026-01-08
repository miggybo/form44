package com.accessrequest.api.controller;

import com.accessrequest.application.dto.RequestDTO;
import com.accessrequest.application.service.RequestApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST Controller for advanced search operations.
 * Provides specialized search endpoints for requests with complex filtering criteria.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Advanced search APIs for requests")
@SecurityRequirement(name = "bearer-jwt")
public class SearchController {

    private final RequestApplicationService requestApplicationService;

    /**
     * Search requests with advanced filtering.
     *
     * @param query search query string
     * @param status filter by status
     * @param requestorId filter by requestor
     * @param accessType filter by access type
     * @param fromDate filter by start date
     * @param toDate filter by end date
     * @param approverId filter by approver
     * @param page page number
     * @param size page size
     * @param sortBy sort field
     * @param sortOrder sort order
     * @return paginated search results
     */
    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Search requests", description = "Performs advanced search on requests with multiple filter criteria")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    public ResponseEntity<Page<RequestDTO>> searchRequests(
            @Parameter(description = "Search query (searches in justification and system name)")
            @RequestParam(required = false) String query,
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
            @Parameter(description = "Filter by approver ID")
            @RequestParam(required = false) UUID approverId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort order (ASC/DESC)")
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        log.info("Searching requests with query: {}, status: {}, requestorId: {}", query, status, requestorId);

        Sort.Direction direction = Sort.Direction.fromString(sortOrder.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<RequestDTO> results = requestApplicationService.searchRequests(
                query, status, requestorId, accessType, fromDate, toDate, approverId, pageable);

        return ResponseEntity.ok(results);
    }

    /**
     * Get pending requests for a specific approver.
     *
     * @param approverId the approver ID
     * @param page page number
     * @param size page size
     * @return paginated list of pending requests
     */
    @GetMapping("/pending-for-approver/{approverId}")
    @PreAuthorize("hasAnyRole('HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Get pending requests for approver", description = "Retrieves all pending requests awaiting approval from a specific approver")
    @ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully")
    public ResponseEntity<Page<RequestDTO>> getPendingRequestsForApprover(
            @Parameter(description = "Approver ID", required = true)
            @PathVariable UUID approverId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching pending requests for approver: {}", approverId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<RequestDTO> pendingRequests = requestApplicationService.getPendingRequestsForApprover(approverId, pageable);

        return ResponseEntity.ok(pendingRequests);
    }

    /**
     * Get requests by requestor.
     *
     * @param requestorId the requestor ID
     * @param page page number
     * @param size page size
     * @return paginated list of requests
     */
    @GetMapping("/by-requestor/{requestorId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Get requests by requestor", description = "Retrieves all requests submitted by a specific requestor")
    @ApiResponse(responseCode = "200", description = "Requests retrieved successfully")
    public ResponseEntity<Page<RequestDTO>> getRequestsByRequestor(
            @Parameter(description = "Requestor ID", required = true)
            @PathVariable UUID requestorId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching requests for requestor: {}", requestorId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RequestDTO> requests = requestApplicationService.getRequestsByRequestor(requestorId, pageable);

        return ResponseEntity.ok(requests);
    }

    /**
     * Get requests by access type.
     *
     * @param accessType the access type
     * @param page page number
     * @param size page size
     * @return paginated list of requests
     */
    @GetMapping("/by-access-type/{accessType}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Get requests by access type", description = "Retrieves all requests for a specific access type")
    @ApiResponse(responseCode = "200", description = "Requests retrieved successfully")
    public ResponseEntity<Page<RequestDTO>> getRequestsByAccessType(
            @Parameter(description = "Access Type", required = true)
            @PathVariable String accessType,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching requests for access type: {}", accessType);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RequestDTO> requests = requestApplicationService.getRequestsByAccessType(accessType, pageable);

        return ResponseEntity.ok(requests);
    }

    /**
     * Get overdue requests (requests pending for more than 7 days).
     *
     * @param page page number
     * @param size page size
     * @return paginated list of overdue requests
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Get overdue requests", description = "Retrieves requests that have been pending for more than 7 days")
    @ApiResponse(responseCode = "200", description = "Overdue requests retrieved successfully")
    public ResponseEntity<Page<RequestDTO>> getOverdueRequests(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching overdue requests");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<RequestDTO> overdueRequests = requestApplicationService.getOverdueRequests(pageable);

        return ResponseEntity.ok(overdueRequests);
    }

    /**
     * Get requests by date range.
     *
     * @param fromDate start date (ISO-8601)
     * @param toDate end date (ISO-8601)
     * @param page page number
     * @param size page size
     * @return paginated list of requests
     */
    @GetMapping("/by-date-range")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Get requests by date range", description = "Retrieves requests created within a specific date range")
    @ApiResponse(responseCode = "200", description = "Requests retrieved successfully")
    public ResponseEntity<Page<RequestDTO>> getRequestsByDateRange(
            @Parameter(description = "From date (ISO-8601)", required = true)
            @RequestParam LocalDateTime fromDate,
            @Parameter(description = "To date (ISO-8601)", required = true)
            @RequestParam LocalDateTime toDate,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching requests between {} and {}", fromDate, toDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RequestDTO> requests = requestApplicationService.getRequestsByDateRange(fromDate, toDate, pageable);

        return ResponseEntity.ok(requests);
    }
}
