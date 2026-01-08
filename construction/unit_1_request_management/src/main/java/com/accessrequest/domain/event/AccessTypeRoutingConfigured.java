package com.accessrequest.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * AccessTypeRoutingConfigured Domain Event.
 * 
 * Published when access type routing is configured.
 */
@Getter
public class AccessTypeRoutingConfigured extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID accessTypeId;
    private final String administratorRole;
    private final Boolean isDefault;

    public AccessTypeRoutingConfigured(UUID accessTypeId, String administratorRole, 
                                      Boolean isDefault) {
        super();
        this.accessTypeId = accessTypeId;
        this.administratorRole = administratorRole;
        this.isDefault = isDefault;
    }

}
