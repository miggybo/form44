package com.accessrequest.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

/**
 * AccessTypeId Value Object.
 * 
 * Represents a unique identifier for an access type.
 * Encapsulates UUID generation and validation.
 */
@Getter
@EqualsAndHashCode
@ToString
public class AccessTypeId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID value;

    private AccessTypeId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("AccessTypeId value cannot be null");
        }
        this.value = value;
    }

    /**
     * Create a new AccessTypeId with a generated UUID.
     */
    public static AccessTypeId generate() {
        return new AccessTypeId(UUID.randomUUID());
    }

    /**
     * Create an AccessTypeId from an existing UUID.
     */
    public static AccessTypeId of(UUID uuid) {
        return new AccessTypeId(uuid);
    }

    /**
     * Create an AccessTypeId from a string representation of UUID.
     */
    public static AccessTypeId of(String uuidString) {
        try {
            return new AccessTypeId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }
    }

    /**
     * Get the UUID value as a string.
     */
    public String asString() {
        return value.toString();
    }

}
