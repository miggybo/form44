package com.accessrequest.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * RequestJustification Value Object.
 * 
 * Represents the justification for a request.
 * Encapsulates validation rules: minimum 10 characters, maximum 10MB.
 */
@Getter
@EqualsAndHashCode
@ToString
public class RequestJustification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 10 * 1024 * 1024; // 10MB in characters
    
    private final String value;

    private RequestJustification(String value) {
        validate(value);
        this.value = value;
    }

    /**
     * Create a RequestJustification from a string.
     */
    public static RequestJustification of(String justification) {
        return new RequestJustification(justification);
    }

    /**
     * Validate justification constraints.
     */
    private static void validate(String justification) {
        if (justification == null || justification.isBlank()) {
            throw new IllegalArgumentException("Justification cannot be null or blank");
        }
        
        if (justification.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Justification must be at least %d characters long", MIN_LENGTH)
            );
        }
        
        if (justification.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Justification cannot exceed %d characters", MAX_LENGTH)
            );
        }
    }

    /**
     * Get the justification text.
     */
    public String getText() {
        return value;
    }

    /**
     * Get the length of the justification.
     */
    public int getLength() {
        return value.length();
    }

}
