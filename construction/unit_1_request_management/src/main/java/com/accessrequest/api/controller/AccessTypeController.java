package com.accessrequest.api.controller;

import com.accessrequest.application.dto.*;
import com.accessrequest.application.service.AccessTypeApplicationService;
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
import java.util.UUID;

/**
 * REST Controller for Access Type Management operations.
 * Handles access type creation, configuration, and querying.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access-types")
@RequiredArgsConstructor
@Tag(name = "Access Type Management", description = "APIs for managing access types and routing rules")
@SecurityRequirement(name = "bearer-jwt")
public class AccessTypeController {

    private final AccessTypeApplicationService accessTypeApplicationService;

    /**
     * Create a new access type.
     *
     * @param createAccessTypeRequest the access type creation details
     * @return the created access type with 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create access type", description = "Creates a new access type (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Access type created successfully",
                    content = @Content(schema = @Schema(implementation = AccessTypeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (admin only)"),
            @ApiResponse(responseCode = "409", description = "Access type already exists")
    })
    public ResponseEntity<AccessTypeDTO> createAccessType(
            @Valid @RequestBody CreateAccessTypeRequest createAccessTypeRequest) {

        log.info("Creating new access type: {}", createAccessTypeRequest.getName());
        AccessTypeDTO createdAccessType = accessTypeApplicationService.createAccessType(createAccessTypeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccessType);
    }

    /**
     * Get access type details by ID.
     *
     * @param accessTypeId the access type ID
     * @return the access type details
     */
    @GetMapping("/{accessTypeId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Get access type details", description = "Retrieves detailed information about a specific access type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access type found",
                    content = @Content(schema = @Schema(implementation = AccessTypeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Access type not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AccessTypeDTO> getAccessType(
            @Parameter(description = "Access Type ID", required = true)
            @PathVariable UUID accessTypeId) {

        log.info("Fetching access type: {}", accessTypeId);
        AccessTypeDTO accessType = accessTypeApplicationService.getAccessType(accessTypeId);
        return ResponseEntity.ok(accessType);
    }

    /**
     * Get access type by name.
     *
     * @param name the access type name
     * @return the access type details
     */
    @GetMapping("/by-name/{name}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "Get access type by name", description = "Retrieves an access type by its name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access type found"),
            @ApiResponse(responseCode = "404", description = "Access type not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AccessTypeDTO> getAccessTypeByName(
            @Parameter(description = "Access Type Name", required = true)
            @PathVariable String name) {

        log.info("Fetching access type by name: {}", name);
        AccessTypeDTO accessType = accessTypeApplicationService.getAccessTypeByName(name);
        return ResponseEntity.ok(accessType);
    }

    /**
     * List all access types with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @param sortBy sort field
     * @param sortOrder sort order (ASC/DESC)
     * @return paginated list of access types
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HEAD_OF_OFFICE', 'REVIEWER', 'HEAD', 'ADMIN')")
    @Operation(summary = "List access types", description = "Retrieves a paginated list of all access types")
    @ApiResponse(responseCode = "200", description = "Access types retrieved successfully")
    public ResponseEntity<Page<AccessTypeDTO>> listAccessTypes(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort order")
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        log.info("Listing access types - page: {}, size: {}", page, size);

        Sort.Direction direction = Sort.Direction.fromString(sortOrder.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AccessTypeDTO> accessTypes = accessTypeApplicationService.listAccessTypes(pageable);
        return ResponseEntity.ok(accessTypes);
    }

    /**
     * Configure routing rules for an access type.
     *
     * @param accessTypeId the access type ID
     * @param configureRoutingRequest the routing configuration details
     * @return the updated access type
     */
    @PutMapping("/{accessTypeId}/routing")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Configure routing rules", description = "Configures approval routing rules for an access type (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routing configured successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid routing configuration"),
            @ApiResponse(responseCode = "404", description = "Access type not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden (admin only)")
    })
    public ResponseEntity<AccessTypeDTO> configureRouting(
            @Parameter(description = "Access Type ID", required = true)
            @PathVariable UUID accessTypeId,
            @Valid @RequestBody ConfigureRoutingRequest configureRoutingRequest) {

        log.info("Configuring routing for access type: {}", accessTypeId);
        AccessTypeDTO updatedAccessType = accessTypeApplicationService.configureRouting(
                accessTypeId, configureRoutingRequest);
        return ResponseEntity.ok(updatedAccessType);
    }
}
