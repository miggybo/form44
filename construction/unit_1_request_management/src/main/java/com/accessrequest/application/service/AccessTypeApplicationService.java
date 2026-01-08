package com.accessrequest.application.service;

import com.accessrequest.application.dto.AccessTypeDTO;
import com.accessrequest.application.dto.ConfigureRoutingRequest;
import com.accessrequest.application.dto.CreateAccessTypeRequest;
import com.accessrequest.domain.aggregate.AccessType;
import com.accessrequest.domain.factory.AccessTypeFactory;
import com.accessrequest.domain.repository.AccessTypeRepository;
import com.accessrequest.domain.valueobject.AccessTypeDescription;
import com.accessrequest.domain.valueobject.AccessTypeName;
import com.accessrequest.infrastructure.event.DomainEventPublisher;
import com.accessrequest.infrastructure.exception.ConflictException;
import com.accessrequest.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for access type operations.
 * Manages access type creation and routing configuration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccessTypeApplicationService {

    private final AccessTypeRepository accessTypeRepository;
    private final AccessTypeFactory accessTypeFactory;
    private final DomainEventPublisher eventPublisher;

    /**
     * Create a new access type.
     *
     * @param request the create access type request DTO
     * @return the created access type DTO
     */
    @Transactional
    public AccessTypeDTO createAccessType(CreateAccessTypeRequest request) {
        log.debug("Creating new access type: {}", request.getName());

        // Check if name already exists
        if (accessTypeRepository.existsByName(request.getName())) {
            throw new ConflictException("Access type with name '" + request.getName() + "' already exists");
        }

        // Create domain aggregate
        AccessType accessType = accessTypeFactory.createAccessType(
            new AccessTypeName(request.getName()),
            request.getDescription() != null ? new AccessTypeDescription(request.getDescription()) : null,
            request.getAdministratorRoles()
        );

        // Save to repository
        accessTypeRepository.save(accessType);

        // Publish events
        eventPublisher.publishAll(accessType.getDomainEvents());

        log.info("Access type created successfully: {}", accessType.getAccessTypeId());
        return mapToDTO(accessType);
    }

    /**
     * Get access type by ID.
     *
     * @param accessTypeId the access type ID
     * @return the access type DTO
     */
    @Transactional(readOnly = true)
    public AccessTypeDTO getAccessType(UUID accessTypeId) {
        log.debug("Getting access type: {}", accessTypeId);

        AccessType accessType = accessTypeRepository.findById(accessTypeId)
            .orElseThrow(() -> new ResourceNotFoundException("AccessType", accessTypeId.toString()));

        return mapToDTO(accessType);
    }

    /**
     * Get access type by name.
     *
     * @param name the access type name
     * @return the access type DTO
     */
    @Transactional(readOnly = true)
    public AccessTypeDTO getAccessTypeByName(String name) {
        log.debug("Getting access type by name: {}", name);

        AccessType accessType = accessTypeRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("AccessType", name));

        return mapToDTO(accessType);
    }

    /**
     * List all access types.
     *
     * @return list of access type DTOs
     */
    @Transactional(readOnly = true)
    public List<AccessTypeDTO> listAccessTypes() {
        log.debug("Listing all access types");
        return accessTypeRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Configure routing for an access type.
     *
     * @param accessTypeId the access type ID
     * @param request the configure routing request DTO
     * @return the updated access type DTO
     */
    @Transactional
    public AccessTypeDTO configureRouting(UUID accessTypeId, ConfigureRoutingRequest request) {
        log.debug("Configuring routing for access type: {}", accessTypeId);

        AccessType accessType = accessTypeRepository.findById(accessTypeId)
            .orElseThrow(() -> new ResourceNotFoundException("AccessType", accessTypeId.toString()));

        // Configure routing
        accessType.configureRouting(request.getAdministratorRoles(), request.getDefaultRole());

        // Save updated access type
        accessTypeRepository.update(accessType);

        // Publish events
        eventPublisher.publishAll(accessType.getDomainEvents());

        log.info("Routing configured successfully for access type: {}", accessTypeId);
        return mapToDTO(accessType);
    }

    /**
     * Map domain AccessType to AccessTypeDTO.
     *
     * @param accessType the domain access type
     * @return the access type DTO
     */
    private AccessTypeDTO mapToDTO(AccessType accessType) {
        return AccessTypeDTO.builder()
            .accessTypeId(accessType.getAccessTypeId().getValue())
            .name(accessType.getName().getValue())
            .description(accessType.getDescription() != null ? accessType.getDescription().getValue() : null)
            .createdAt(accessType.getCreatedAt())
            .updatedAt(accessType.getUpdatedAt())
            .build();
    }
}
