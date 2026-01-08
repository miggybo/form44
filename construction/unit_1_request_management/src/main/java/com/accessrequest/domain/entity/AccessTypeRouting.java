package com.accessrequest.domain.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * AccessTypeRouting Entity.
 * 
 * Represents routing configuration for an access type.
 * Specifies which administrator role handles this access type.
 * Immutable once created.
 */
@Getter
@EqualsAndHashCode
@ToString
public class AccessTypeRouting implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID routingId;
    private final String administratorRole;
    private final Boolean isDefault;
    private final Instant createdAt;

    private AccessTypeRouting(UUID routingId, String administratorRole, Boolean isDefault, 
                             Instant createdAt) {
        this.routingId = routingId;
        this.administratorRole = administratorRole;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }

    /**
     * Create a new AccessTypeRouting.
     */
    public static AccessTypeRouting create(String administratorRole, Boolean isDefault) {
        if (administratorRole == null || administratorRole.isBlank()) {
            throw new IllegalArgumentException("Administrator role cannot be null or blank");
        }
        if (isDefault == null) {
            throw new IllegalArgumentException("isDefault cannot be null");
        }
        
        return new AccessTypeRouting(
            UUID.randomUUID(),
            administratorRole,
            isDefault,
            Instant.now()
        );
    }

    /**
     * Check if this is the default routing.
     */
    public boolean isDefaultRouting() {
        return Boolean.TRUE.equals(isDefault);
    }

}
