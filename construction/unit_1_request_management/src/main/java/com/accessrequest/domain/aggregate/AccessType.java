package com.accessrequest.domain.aggregate;

import com.accessrequest.domain.entity.AccessTypeRouting;
import com.accessrequest.domain.event.AccessTypeAdded;
import com.accessrequest.domain.event.AccessTypeRoutingConfigured;
import com.accessrequest.domain.event.DomainEvent;
import com.accessrequest.domain.valueobject.AccessTypeDescription;
import com.accessrequest.domain.valueobject.AccessTypeId;
import com.accessrequest.domain.valueobject.AccessTypeName;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/**
 * AccessType Aggregate Root.
 * 
 * Represents an access type (OS, WebApp, Database) with routing configuration.
 * Encapsulates routing rules and administrator role assignments.
 */
@Getter
@ToString
public class AccessType implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final AccessTypeId accessTypeId;
    private final AccessTypeName name;
    private final AccessTypeDescription description;
    private final List<AccessTypeRouting> routingRules;
    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private final List<DomainEvent> domainEvents;

    private AccessType(AccessTypeId accessTypeId, AccessTypeName name, 
                      AccessTypeDescription description, UUID createdBy) {
        this.accessTypeId = accessTypeId;
        this.name = name;
        this.description = description;
        this.createdAt = Instant.now();
        this.createdBy = createdBy;
        this.updatedAt = Instant.now();
        this.routingRules = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Create a new AccessType.
     */
    public static AccessType create(String name, String description, UUID createdBy) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Access type name cannot be null or blank");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Created by cannot be null");
        }
        
        AccessTypeId id = AccessTypeId.generate();
        AccessTypeName typeName = AccessTypeName.of(name);
        AccessTypeDescription typeDescription = description != null && !description.isBlank() 
            ? AccessTypeDescription.of(description) 
            : AccessTypeDescription.empty();
        
        AccessType accessType = new AccessType(id, typeName, typeDescription, createdBy);
        
        // Publish AccessTypeAdded event
        accessType.domainEvents.add(new AccessTypeAdded(
            id.getValue(),
            name,
            description
        ));
        
        return accessType;
    }

    /**
     * Add routing rule for this access type.
     */
    public void addRoutingRule(String administratorRole, Boolean isDefault) {
        if (administratorRole == null || administratorRole.isBlank()) {
            throw new IllegalArgumentException("Administrator role cannot be null or blank");
        }
        
        // If this is the default, remove default flag from other rules
        if (Boolean.TRUE.equals(isDefault)) {
            routingRules.forEach(rule -> {
                if (rule.isDefaultRouting()) {
                    // Create new rule without default flag
                    routingRules.remove(rule);
                    routingRules.add(AccessTypeRouting.create(rule.getAdministratorRole(), false));
                }
            });
        }
        
        // Check if rule already exists
        boolean ruleExists = routingRules.stream()
            .anyMatch(rule -> rule.getAdministratorRole().equals(administratorRole));
        
        if (ruleExists) {
            throw new IllegalArgumentException(
                "Routing rule for role " + administratorRole + " already exists"
            );
        }
        
        routingRules.add(AccessTypeRouting.create(administratorRole, isDefault));
        updatedAt = Instant.now();
        
        // Publish event
        domainEvents.add(new AccessTypeRoutingConfigured(
            accessTypeId.getValue(),
            administratorRole,
            isDefault
        ));
    }

    /**
     * Remove routing rule.
     */
    public void removeRoutingRule(String administratorRole) {
        if (administratorRole == null || administratorRole.isBlank()) {
            throw new IllegalArgumentException("Administrator role cannot be null or blank");
        }
        
        boolean removed = routingRules.removeIf(rule -> 
            rule.getAdministratorRole().equals(administratorRole)
        );
        
        if (!removed) {
            throw new IllegalArgumentException(
                "Routing rule for role " + administratorRole + " not found"
            );
        }
        
        updatedAt = Instant.now();
    }

    /**
     * Get routing for a specific role.
     */
    public Optional<AccessTypeRouting> getRoutingForRole(String administratorRole) {
        return routingRules.stream()
            .filter(rule -> rule.getAdministratorRole().equals(administratorRole))
            .findFirst();
    }

    /**
     * Get default routing rule.
     */
    public Optional<AccessTypeRouting> getDefaultRouting() {
        return routingRules.stream()
            .filter(AccessTypeRouting::isDefaultRouting)
            .findFirst();
    }

    /**
     * Check if access type has valid routing configuration.
     */
    public boolean hasValidRouting() {
        return !routingRules.isEmpty() && getDefaultRouting().isPresent();
    }

    /**
     * Get all domain events and clear the list.
     */
    public List<DomainEvent> getDomainEventsAndClear() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

}
