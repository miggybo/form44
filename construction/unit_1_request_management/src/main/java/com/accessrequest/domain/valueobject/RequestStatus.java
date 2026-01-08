package com.accessrequest.domain.valueobject;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * RequestStatus Value Object.
 * 
 * Represents the status of a request with state machine logic.
 * Encapsulates valid state transitions.
 */
@Getter
public enum RequestStatus {
    DRAFT("Draft", "Initial state"),
    PENDING_INITIAL_APPROVAL("PendingInitialApproval", "Awaiting Head of Office approval"),
    PENDING_REVIEW("PendingReview", "Awaiting SMD/RDC Reviewer review"),
    PENDING_FINAL_APPROVAL("PendingFinalApproval", "Awaiting SMD/RDC Head approval"),
    RETURNED_TO_REVIEWER("ReturnedToReviewer", "Returned by Head for more review"),
    APPROVED("Approved", "Approved and ready for implementation"),
    DECLINED("Declined", "Declined at any stage"),
    IMPLEMENTED("Implemented", "Access implemented");

    private final String displayName;
    private final String description;

    RequestStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Check if a transition from current status to target status is valid.
     */
    public boolean canTransitionTo(RequestStatus targetStatus) {
        return getValidTransitions().contains(targetStatus);
    }

    /**
     * Get all valid transitions from current status.
     */
    public Set<RequestStatus> getValidTransitions() {
        return switch (this) {
            case DRAFT -> new HashSet<>(Arrays.asList(PENDING_INITIAL_APPROVAL));
            case PENDING_INITIAL_APPROVAL -> new HashSet<>(Arrays.asList(PENDING_REVIEW, DECLINED));
            case PENDING_REVIEW -> new HashSet<>(Arrays.asList(PENDING_FINAL_APPROVAL, DECLINED));
            case PENDING_FINAL_APPROVAL -> new HashSet<>(Arrays.asList(APPROVED, RETURNED_TO_REVIEWER));
            case RETURNED_TO_REVIEWER -> new HashSet<>(Arrays.asList(PENDING_FINAL_APPROVAL, DECLINED));
            case APPROVED -> new HashSet<>(Arrays.asList(IMPLEMENTED));
            case DECLINED, IMPLEMENTED -> new HashSet<>(); // Terminal states
        };
    }

    /**
     * Check if this status is a terminal state (no further transitions possible).
     */
    public boolean isTerminal() {
        return this == DECLINED || this == IMPLEMENTED;
    }

    /**
     * Check if this status is pending (awaiting action).
     */
    public boolean isPending() {
        return this == PENDING_INITIAL_APPROVAL || 
               this == PENDING_REVIEW || 
               this == PENDING_FINAL_APPROVAL ||
               this == RETURNED_TO_REVIEWER;
    }

    /**
     * Get RequestStatus from string value.
     */
    public static RequestStatus fromString(String value) {
        try {
            return RequestStatus.valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid request status: " + value, e);
        }
    }

}
