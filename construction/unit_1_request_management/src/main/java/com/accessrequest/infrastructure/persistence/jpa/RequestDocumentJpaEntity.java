package com.accessrequest.infrastructure.persistence.jpa;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for RequestDocument entity persistence.
 * Maps to the request_documents table.
 */
@Entity
@Table(name = "request_documents", indexes = {
    @Index(name = "idx_document_request_id", columnList = "request_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDocumentJpaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "document_id", columnDefinition = "UUID")
    private UUID documentId;

    @Column(name = "request_id", nullable = false, columnDefinition = "UUID")
    private UUID requestId;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "document_size")
    private Long documentSize;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column(name = "uploaded_by", columnDefinition = "UUID")
    private UUID uploadedBy;
}
