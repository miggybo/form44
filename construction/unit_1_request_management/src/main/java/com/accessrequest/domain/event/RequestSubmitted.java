package com.accessrequest.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * RequestSubmitted Domain Event.
 * 
 * Published when a request is submitted for approval.
 */
@Getter
public class RequestSubmitted extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID requestId;
    private final UUID requestorId;
    private final UUID headOfOfficeId;
    private final String accessType;
    private final String systemName;

    public RequestSubmitted(UUID requestId, UUID requestorId, UUID headOfOfficeId, 
                           String accessType, String systemName) {
        super();
        this.requestId = requestId;
        this.requestorId = requestorId;
        this.headOfOfficeId = headOfOfficeId;
        this.accessType = accessType;
        this.systemName = systemName;
    }

}
