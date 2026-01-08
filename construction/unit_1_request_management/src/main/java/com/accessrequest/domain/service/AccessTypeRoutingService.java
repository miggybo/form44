package com.accessrequest.domain.service;

import com.accessrequest.domain.aggregate.AccessType;
import com.accessrequest.domain.entity.AccessTypeRouting;
import com.accessrequest.domain.repository.AccessTypeRepository;
import com.accessrequest.domain.valueobject.AccessTypeDescription;
import com.accessrequest.domain.valueobject.AccessTypeName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AccessTypeRoutingService Domain Service.
 * 
 * Manages access type routing configuration.
 * Encapsulates routing validation and configuration logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTypeRoutingService {

    private final AccessTypeRepository accessTypeRepository;

    /**
     * Create a new access type with routing configuration.
     */
    public AccessType createAccessType(String name, String description, 
                                      List<String> administratorRoles, UUID createdBy) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Access type name cannot be null or blank");
        }
        if (administratorRoles == null || administratorRoles.isEmpty()) {
            throw new IllegalArgumentException("At least one administrator role must be provided");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Created by cannot be null");
        }
        
        // Check if name already exists
        Optional<AccessType> existing = accessTypeRepository.findByName(name);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Access type with name '" + name + "' already exists");
        }
        
        // Create access type
        AccessType accessType = AccessType.create(name, description, createdBy);
        
        // Add routing rules (first one is default)
        for (int i = 0; i < administratorRoles.size(); i++) {
            String role = administratorRoles.get(i);
            boolean isDefault = (i == 0); // First role is default
            accessType.addRoutingRule(role, isDefault);
        }
        
        log.info("Created access type: {} with {} routing rules", name, administratorRoles.size());
        return accessType;
    }

    /**
     * Configure routing for an access type.
     */
    public void configureRouting(AccessType accessType, List<String> administratorRoles, 
                                String defaultRole) {
        if (accessType == null) {
            throw new IllegalArgumentException("Access type cannot be null");
        }
        if (administratorRoles == null || administratorRoles.isEmpty()) {
            throw new IllegalArgumentException("At least one administrator role must be provided");
        }
        if (defaultRole == null || defaultRole.isBlank()) {
            throw new IllegalArgumentException("Default role cannot be null or blank");
        }
        
        // Validate that default role is in the list
        if (!administratorRoles.contains(defaultRole)) {
            throw new IllegalArgumentException("Default role must be in the administrator roles list");
        }
        
        // Clear existing routing rules
        accessType.getRoutingRules().clear();
        
        // Add new routing rules
        for (String role : administratorRoles) {
            boolean isDefault = role.equals(defaultRole);
            accessType.addRoutingRule(role, isDefault);
        }
        
        log.info("Configured routing for access type: {} with default role: {}", 
                 accessType.getName().getName(), defaultRole);
    }

    /**
     * Get routing for an access type.
     */
    public List<AccessTypeRouting> getRoutingForAccessType(AccessType accessType) {
        if (accessType == null) {
            throw new IllegalArgumentException("Access type cannot be null");
        }
        
        return accessType.getRoutingRules();
    }

    /**
     * Validate routing configuration for an access type.
     */
    public boolean validateRoutingConfiguration(AccessType accessType) {
        if (accessType == null) {
            throw new IllegalArgumentException("Access type cannot be null");
        }
        
        // Must have at least one routing rule
        if (accessType.getRoutingRules().isEmpty()) {
            log.warn("Access type {} has no routing rules", accessType.getName().getName());
            return false;
        }
        
        // Must have a default routing rule
        if (accessType.getDefaultRouting().isEmpty()) {
            log.warn("Access type {} has no default routing rule", accessType.getName().getName());
            return false;
        }
        
        return true;
    }

    /**
     * Get the default administrator role for an access type.
     */
    public Optional<String> getDefaultAdministratorRole(AccessType accessType) {
        if (accessType == null) {
            throw new IllegalArgumentException("Access type cannot be null");
        }
        
        return accessType.getDefaultRouting()
            .map(AccessTypeRouting::getAdministratorRole);
    }

    /**
     * Check if an access type has a specific routing role.
     */
    public boolean hasRoutingRole(AccessType accessType, String administratorRole) {
        if (accessType == null) {
            throw new IllegalArgumentException("Access type cannot be null");
        }
        if (administratorRole == null || administratorRole.isBlank()) {
            throw new IllegalArgumentException("Administrator role cannot be null or blank");
        }
        
        return accessType.getRoutingForRole(administratorRole).isPresent();
    }

}
