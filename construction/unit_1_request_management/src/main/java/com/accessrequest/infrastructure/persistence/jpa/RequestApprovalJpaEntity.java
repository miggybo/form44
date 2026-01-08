package com.accessrequest.infrastructure.persistence.jpa;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for RequestApproval entity persistence.
 * Maps to the request_approvals table.
 */
@Entity
@Table(name = "request_approvals", indexes = {
    @Index(name = "idx_approval_request_id", columnList = "request_id"),
    @Index(name = "idx_approval_approver_id", columnList = "approver_id"),
    @Index(name = "idx_approval_request_type", columnList = "request_id,approval_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestApprovalJpaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "approval_id", columnDefinition = "UUID")
    private UUID approvalId;

    @Column(name = "request_id", nullable = false, columnDefinition = "UUID")
    private UUID requestId;

    @Column(name = "approver_id", nullable = false, columnDefinition = "UUID")
    private UUID approverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", nullable = false)
    private ApprovalTypeEnum approvalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStatusEnum status;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Enum for approval type values.
     */
    public enum ApprovalTypeEnum {
        HEAD_OF_OFFICE,
        REVIEWER,
        HEAD
    }

    /**
     * Enum for approval status values.
     */
    public enum ApprovalStatusEnum {
        APPROVED,
        DECLINED,
        PENDING
    }
}
