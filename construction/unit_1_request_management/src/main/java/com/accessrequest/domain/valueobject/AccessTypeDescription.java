package com.accessrequest.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * AccessTypeDescription Value Object.
 * 
 * Represents the description of an access type.
 * Optional field with maximum length constraint.
 */
@Getter
@EqualsAndHashCode
@ToString
public class AccessTypeDescription implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int MAX_LENGTH = 1000;
    
    private final String value;

    private AccessTypeDescription(String value) {
        validate(value);
        this.value = value;
    }

    /**
     * Create an AccessTypeDescription from a string.
     */
    public static AccessTypeDescription of(String description) {
        return new AccessTypeDescription(description);
    }

    /**
     * Create an empty AccessTypeDescription.
     */
    public static AccessTypeDescription empty() {
        return new AccessTypeDescription("");
    }

    /**
     * Validate description constraints.
     */
    private static void validate(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        
        if (description.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Description cannot exceed %d characters", MAX_LENGTH)
            );
        }
    }

    /**
     * Check if description is empty.
     */
    public boolean isEmpty() {
        return value == null || value.isBlank();
    }

    /**
     * Get the description value.
     */
    public String getDescription() {
        return value;
    }

}
