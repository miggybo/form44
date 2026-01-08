package com.accessrequest.domain.entity;

import com.accessrequest.domain.valueobject.RequestStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * RequestHistory Entity.
 * 
 * Represents an audit trail entry for a request.
 * Append-only - immutable once created.
 */
@Getter
@EqualsAndHashCode
@ToString
public class RequestHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID historyId;
    private final String action;
    private final UUID actorId;
    private final RequestStatus previousStatus;
    private final RequestStatus newStatus;
    private final String comments;
    private final Instant createdAt;

    private RequestHistory(UUID historyId, String action, UUID actorId, 
                          RequestStatus previousStatus, RequestStatus newStatus, 
                          String comments, Instant createdAt) {
        this.historyId = historyId;
        this.action = action;
        this.actorId = actorId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    /**
     * Create a new RequestHistory entry.
     */
    public static RequestHistory create(String action, UUID actorId, 
                                       RequestStatus previousStatus, RequestStatus newStatus, 
                                       String comments) {
        return new RequestHistory(
            UUID.randomUUID(),
            action,
            actorId,
            previousStatus,
            newStatus,
            comments,
            Instant.now()
        );
    }

    /**
     * Create a RequestHistory entry for a state transition.
     */
    public static RequestHistory stateTransition(UUID actorId, RequestStatus previousStatus, 
                                                 RequestStatus newStatus, String comments) {
        return create(
            "State transition from " + previousStatus.getDisplayName() + " to " + newStatus.getDisplayName(),
            actorId,
            previousStatus,
            newStatus,
            comments
        );
    }

    /**
     * Create a RequestHistory entry for an action without state change.
     */
    public static RequestHistory action(String action, UUID actorId, RequestStatus currentStatus, 
                                       String comments) {
        return create(action, actorId, currentStatus, currentStatus, comments);
    }

}
