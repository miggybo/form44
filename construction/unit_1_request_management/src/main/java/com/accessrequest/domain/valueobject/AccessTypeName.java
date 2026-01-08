package com.accessrequest.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * AccessTypeName Value Object.
 * 
 * Represents the name of an access type.
 * Encapsulates validation rules: non-blank, unique.
 */
@Getter
@EqualsAndHashCode
@ToString
public class AccessTypeName implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 255;
    
    private final String value;

    private AccessTypeName(String value) {
        validate(value);
        this.value = value.trim();
    }

    /**
     * Create an AccessTypeName from a string.
     */
    public static AccessTypeName of(String name) {
        return new AccessTypeName(name);
    }

    /**
     * Validate name constraints.
     */
    private static void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Access type name cannot be null or blank");
        }
        
        if (name.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Access type name must be at least %d character long", MIN_LENGTH)
            );
        }
        
        if (name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Access type name cannot exceed %d characters", MAX_LENGTH)
            );
        }
    }

    /**
     * Get the name value.
     */
    public String getName() {
        return value;
    }

}
