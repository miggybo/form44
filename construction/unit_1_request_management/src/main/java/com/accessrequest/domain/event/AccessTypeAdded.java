package com.accessrequest.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * AccessTypeAdded Domain Event.
 * 
 * Published when a new access type is added.
 */
@Getter
public class AccessTypeAdded extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID accessTypeId;
    private final String name;
    private final String description;

    public AccessTypeAdded(UUID accessTypeId, String name, String description) {
        super();
        this.accessTypeId = accessTypeId;
        this.name = name;
        this.description = description;
    }

}
