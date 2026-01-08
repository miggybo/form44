package com.accessrequest.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * RequestEndorsed Domain Event.
 * 
 * Published when a request is endorsed by SMD/RDC Reviewer.
 */
@Getter
public class RequestEndorsed extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID requestId;
    private final UUID reviewerId;
    private final String reviewerName;
    private final String comments;

    public RequestEndorsed(UUID requestId, UUID reviewerId, String reviewerName, 
                          String comments) {
        super();
        this.requestId = requestId;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.comments = comments;
    }

}
