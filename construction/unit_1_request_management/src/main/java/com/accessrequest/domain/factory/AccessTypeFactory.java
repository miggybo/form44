package com.accessrequest.domain.factory;

import com.accessrequest.domain.aggregate.AccessType;
import com.accessrequest.domain.repository.AccessTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AccessTypeFactory Domain Factory.
 * 
 * Creates new AccessType aggregates with validation.
 * Encapsulates access type creation logic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTypeFactory {

    private final AccessTypeRepository accessTypeRepository;

    /**
     * Create a new access type.
     */
    public AccessType createAccessType(String name, String description, 
                                      List<String> administratorRoles, UUID createdBy) {
        // Validate inputs
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
        if (accessTypeRepository.findByName(name).isPresent()) {
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
        
        log.info("Created new access type: {} with {} routing rules", name, administratorRoles.size());
        
        return accessType;
    }

    /**
     * Validate access type creation data.
     */
    public ValidationResult validateAccessTypeCreation(String name, String description, 
                                                       List<String> administratorRoles) {
        ValidationResult result = new ValidationResult();
        
        // Validate name
        if (name == null || name.isBlank()) {
            result.addError("name", "Access type name cannot be null or blank");
        } else if (name.length() > 255) {
            result.addError("name", "Access type name cannot exceed 255 characters");
        } else {
            // Check uniqueness
            if (accessTypeRepository.findByName(name).isPresent()) {
                result.addError("name", "Access type with this name already exists");
            }
        }
        
        // Validate description
        if (description != null && description.length() > 1000) {
            result.addError("description", "Description cannot exceed 1000 characters");
        }
        
        // Validate administrator roles
        if (administratorRoles == null || administratorRoles.isEmpty()) {
            result.addError("administratorRoles", "At least one administrator role must be provided");
        } else {
            // Check for duplicates
            long uniqueRoles = administratorRoles.stream().distinct().count();
            if (uniqueRoles != administratorRoles.size()) {
                result.addError("administratorRoles", "Duplicate administrator roles are not allowed");
            }
            
            // Check for blank roles
            if (administratorRoles.stream().anyMatch(role -> role == null || role.isBlank())) {
                result.addError("administratorRoles", "Administrator roles cannot be blank");
            }
        }
        
        return result;
    }

    /**
     * Validation result holder.
     */
    public static class ValidationResult {
        private final Map<String, String> errors = new HashMap<>();
        
        public void addError(String field, String message) {
            errors.put(field, message);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public Map<String, String> getErrors() {
            return errors;
        }
    }

}
