package com.accessrequest.infrastructure.persistence.jpa;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for RequestHistory entity persistence.
 * Maps to the request_history table for audit trail.
 */
@Entity
@Table(name = "request_history", indexes = {
    @Index(name = "idx_history_request_id", columnList = "request_id"),
    @Index(name = "idx_history_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestHistoryJpaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "history_id", columnDefinition = "UUID")
    private UUID historyId;

    @Column(name = "request_id", nullable = false, columnDefinition = "UUID")
    private UUID requestId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "actor_id", nullable = false, columnDefinition = "UUID")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private RequestJpaEntity.RequestStatusEnum previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private RequestJpaEntity.RequestStatusEnum newStatus;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
