package com.accessrequest.infrastructure.persistence.jpa;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity for Request aggregate persistence.
 * Maps the Request domain aggregate to the access_requests table.
 */
@Entity
@Table(name = "access_requests", indexes = {
    @Index(name = "idx_requestor_id", columnList = "requestor_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_access_type_id", columnList = "access_type_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_submitted_at", columnList = "submitted_at"),
    @Index(name = "idx_status_created_at", columnList = "status,created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestJpaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "request_id", columnDefinition = "UUID")
    private UUID requestId;

    @Column(name = "requestor_id", nullable = false, columnDefinition = "UUID")
    private UUID requestorId;

    @Column(name = "access_type_id", nullable = false, columnDefinition = "UUID")
    private UUID accessTypeId;

    @Column(name = "system_name", nullable = false)
    private String systemName;

    @Column(name = "justification", nullable = false, columnDefinition = "TEXT")
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatusEnum status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "implemented_at")
    private Instant implementedAt;

    @Column(name = "created_by", columnDefinition = "UUID")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "UUID")
    private UUID updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "request_id")
    @Builder.Default
    private List<RequestApprovalJpaEntity> approvals = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "request_id")
    @Builder.Default
    private List<RequestHistoryJpaEntity> history = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "request_id")
    @Builder.Default
    private List<RequestDocumentJpaEntity> documents = new ArrayList<>();

    /**
     * Enum for request status values.
     */
    public enum RequestStatusEnum {
        DRAFT,
        PENDING_INITIAL_APPROVAL,
        PENDING_REVIEW,
        PENDING_FINAL_APPROVAL,
        RETURNED_TO_REVIEWER,
        APPROVED,
        DECLINED,
        IMPLEMENTED
    }
}
