package com.accessrequest.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * ApprovalComment Value Object.
 * 
 * Represents a comment made during approval/decline.
 * Immutable once created.
 */
@Getter
@EqualsAndHashCode
@ToString
public class ApprovalComment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int MAX_LENGTH = 5000;
    
    private final String text;
    private final UUID createdBy;
    private final Instant createdAt;

    private ApprovalComment(String text, UUID createdBy, Instant createdAt) {
        validate(text);
        this.text = text;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    /**
     * Create an ApprovalComment.
     */
    public static ApprovalComment of(String text, UUID createdBy) {
        return new ApprovalComment(text, createdBy, Instant.now());
    }

    /**
     * Create an ApprovalComment with specific timestamp.
     */
    public static ApprovalComment of(String text, UUID createdBy, Instant createdAt) {
        return new ApprovalComment(text, createdBy, createdAt);
    }

    /**
     * Create an empty ApprovalComment (for optional comments).
     */
    public static ApprovalComment empty() {
        return new ApprovalComment("", null, Instant.now());
    }

    /**
     * Validate comment constraints.
     */
    private static void validate(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Comment text cannot be null");
        }
        
        if (text.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Comment cannot exceed %d characters", MAX_LENGTH)
            );
        }
    }

    /**
     * Check if comment is empty.
     */
    public boolean isEmpty() {
        return text == null || text.isBlank();
    }

    /**
     * Get the comment text.
     */
    public String getText() {
        return text;
    }

}
