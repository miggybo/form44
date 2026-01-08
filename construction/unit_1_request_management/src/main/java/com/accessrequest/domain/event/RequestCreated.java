package com.accessrequest.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * RequestCreated Domain Event.
 * 
 * Published when a new request is created.
 */
@Getter
public class RequestCreated extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID requestId;
    private final UUID requestorId;
    private final String requestorName;
    private final String accessType;
    private final String systemName;
    private final String justification;

    public RequestCreated(UUID requestId, UUID requestorId, String requestorName, 
                         String accessType, String systemName, String justification) {
        super();
        this.requestId = requestId;
        this.requestorId = requestorId;
        this.requestorName = requestorName;
        this.accessType = accessType;
        this.systemName = systemName;
        this.justification = justification;
    }

}
