package com.accessrequest.domain.policy;

import com.accessrequest.domain.aggregate.AccessType;
import com.accessrequest.domain.entity.AccessTypeRouting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AccessTypeRoutingPolicy Domain Policy.
 * 
 * Validates access type routing configuration.
 * Encapsulates business rules for access type routing.
 */
@Slf4j
@Component
public class AccessTypeRoutingPolicy {

    /**
     * Check if access type routing configuration is valid.
     */
    public boolean isValidRoutingConfiguration(AccessType accessType) {
        if (accessType == null) {
            log.warn("Cannot validate routing: access type is null");
            return false;
        }
        
        // Must have at least one routing rule
        if (accessType.getRoutingRules().isEmpty()) {
            log.warn("Access type {} has no routing rules", accessType.getName().getName());
            return false;
        }
        
        // Must have a default routing rule
        Optional<AccessTypeRouting> defaultRouting = accessType.getDefaultRouting();
        if (defaultRouting.isEmpty()) {
            log.warn("Access type {} has no default routing rule", accessType.getName().getName());
            return false;
        }
        
        return true;
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
     * Validate routing configuration update.
     */
    public ValidationResult validateRoutingUpdate(AccessType accessType, 
                                                  List<String> administratorRoles, 
                                                  String defaultRole) {
        ValidationResult result = new ValidationResult();
        
        if (accessType == null) {
            result.addError("accessType", "Access type cannot be null");
            return result;
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
        
        // Validate default role
        if (defaultRole == null || defaultRole.isBlank()) {
            result.addError("defaultRole", "Default role cannot be null or blank");
        } else if (administratorRoles != null && !administratorRoles.contains(defaultRole)) {
            result.addError("defaultRole", "Default role must be in the administrator roles list");
        }
        
        return result;
    }

    /**
     * Check if access type name is unique.
     */
    public boolean isNameUnique(String name, AccessType existingAccessType) {
        if (name == null || name.isBlank()) {
            return false;
        }
        
        // If updating existing access type, allow same name
        if (existingAccessType != null && existingAccessType.getName().getName().equals(name)) {
            return true;
        }
        
        // TODO: Check against repository for uniqueness
        return true;
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
