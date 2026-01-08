package com.accessrequest.domain.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * RequestDocument Entity.
 * 
 * Represents a document reference associated with a request.
 * Immutable once created.
 */
@Getter
@EqualsAndHashCode
@ToString
public class RequestDocument implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID documentId;
    private final String documentName;
    private final Long documentSize;
    private final UUID uploadedBy;
    private final Instant uploadedAt;

    private RequestDocument(UUID documentId, String documentName, Long documentSize, 
                           UUID uploadedBy, Instant uploadedAt) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.documentSize = documentSize;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    /**
     * Create a new RequestDocument.
     */
    public static RequestDocument create(UUID documentId, String documentName, 
                                        Long documentSize, UUID uploadedBy) {
        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        if (documentName == null || documentName.isBlank()) {
            throw new IllegalArgumentException("Document name cannot be null or blank");
        }
        if (uploadedBy == null) {
            throw new IllegalArgumentException("Uploaded by cannot be null");
        }
        
        return new RequestDocument(
            documentId,
            documentName,
            documentSize,
            uploadedBy,
            Instant.now()
        );
    }

}
