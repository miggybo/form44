package com.accessrequest.domain.policy;

import com.accessrequest.domain.aggregate.Request;
import com.accessrequest.domain.valueobject.RequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RequestRoutingPolicy Domain Policy.
 * 
 * Determines routing based on access type and status.
 * Encapsulates business rules for request routing.
 */
@Slf4j
@Component
public class RequestRoutingPolicy {

    /**
     * Get the next approval stage for a request.
     */
    public ApprovalStage getNextApprovalStage(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        RequestStatus status = request.getStatus();
        
        return switch (status) {
            case DRAFT -> ApprovalStage.HEAD_OF_OFFICE;
            case PENDING_INITIAL_APPROVAL -> ApprovalStage.REVIEWER;
            case PENDING_REVIEW -> ApprovalStage.HEAD;
            case PENDING_FINAL_APPROVAL -> ApprovalStage.ADMINISTRATOR;
            case RETURNED_TO_REVIEWER -> ApprovalStage.REVIEWER;
            case APPROVED -> ApprovalStage.ADMINISTRATOR;
            case DECLINED, IMPLEMENTED -> ApprovalStage.NONE;
        };
    }

    /**
     * Check if a status transition is valid.
     */
    public boolean isValidTransition(RequestStatus currentStatus, RequestStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            log.warn("Cannot validate transition: status is null");
            return false;
        }
        
        return currentStatus.canTransitionTo(newStatus);
    }

    /**
     * Get the routing path for a request.
     */
    public RoutingPath getRoutingPath(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        RoutingPath path = new RoutingPath();
        path.setCurrentStage(getNextApprovalStage(request));
        path.setCurrentStatus(request.getStatus());
        path.setApprovalCount(request.getApprovals().size());
        
        return path;
    }

    /**
     * Check if request is ready for implementation.
     */
    public boolean isReadyForImplementation(Request request) {
        if (request == null) {
            return false;
        }
        
        return request.getStatus() == RequestStatus.APPROVED;
    }

    /**
     * Check if request is in a terminal state.
     */
    public boolean isTerminal(Request request) {
        if (request == null) {
            return false;
        }
        
        return request.isTerminal();
    }

    /**
     * Approval stage enumeration.
     */
    public enum ApprovalStage {
        HEAD_OF_OFFICE("Head of Office"),
        REVIEWER("SMD/RDC Reviewer"),
        HEAD("SMD/RDC Head"),
        ADMINISTRATOR("Administrator"),
        NONE("None");

        private final String displayName;

        ApprovalStage(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Routing path information.
     */
    public static class RoutingPath {
        private ApprovalStage currentStage;
        private RequestStatus currentStatus;
        private int approvalCount;

        public ApprovalStage getCurrentStage() {
            return currentStage;
        }

        public void setCurrentStage(ApprovalStage currentStage) {
            this.currentStage = currentStage;
        }

        public RequestStatus getCurrentStatus() {
            return currentStatus;
        }

        public void setCurrentStatus(RequestStatus currentStatus) {
            this.currentStatus = currentStatus;
        }

        public int getApprovalCount() {
            return approvalCount;
        }

        public void setApprovalCount(int approvalCount) {
            this.approvalCount = approvalCount;
        }
    }

}
