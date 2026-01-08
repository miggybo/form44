package com.accessrequest.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

/**
 * RequestId Value Object.
 * 
 * Represents a unique identifier for a request.
 * Encapsulates UUID generation and validation.
 */
@Getter
@EqualsAndHashCode
@ToString
public class RequestId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID value;

    private RequestId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("RequestId value cannot be null");
        }
        this.value = value;
    }

    /**
     * Create a new RequestId with a generated UUID.
     */
    public static RequestId generate() {
        return new RequestId(UUID.randomUUID());
    }

    /**
     * Create a RequestId from an existing UUID.
     */
    public static RequestId of(UUID uuid) {
        return new RequestId(uuid);
    }

    /**
     * Create a RequestId from a string representation of UUID.
     */
    public static RequestId of(String uuidString) {
        try {
            return new RequestId(UUID.fromString(uuidString));
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
