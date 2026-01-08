package com.accessrequest.domain.entity;

import com.accessrequest.domain.valueobject.ApprovalComment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * RequestApproval Entity.
 * 
 * Represents an approval at a specific stage of the request workflow.
 * Immutable once created - tracks approval history.
 */
@Getter
@EqualsAndHashCode
@ToString
public class RequestApproval implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID approvalId;
    private final UUID approverId;
    private final ApprovalType approvalType;
    private final ApprovalStatus status;
    private final ApprovalComment comment;
    private final Instant createdAt;

    private RequestApproval(UUID approvalId, UUID approverId, ApprovalType approvalType, 
                           ApprovalStatus status, ApprovalComment comment, Instant createdAt) {
        this.approvalId = approvalId;
        this.approverId = approverId;
        this.approvalType = approvalType;
        this.status = status;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    /**
     * Create a new RequestApproval.
     */
    public static RequestApproval create(UUID approverId, ApprovalType approvalType, 
                                        ApprovalStatus status, ApprovalComment comment) {
        return new RequestApproval(
            UUID.randomUUID(),
            approverId,
            approvalType,
            status,
            comment,
            Instant.now()
        );
    }

    /**
     * Approval Type enumeration.
     */
    public enum ApprovalType {
        HEAD_OF_OFFICE("Head of Office"),
        REVIEWER("SMD/RDC Reviewer"),
        HEAD("SMD/RDC Head");

        private final String displayName;

        ApprovalType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Approval Status enumeration.
     */
    public enum ApprovalStatus {
        APPROVED("Approved"),
        DECLINED("Declined"),
        PENDING("Pending");

        private final String displayName;

        ApprovalStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

}
