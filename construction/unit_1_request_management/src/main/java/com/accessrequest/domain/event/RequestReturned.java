package com.accessrequest.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * RequestReturned Domain Event.
 * 
 * Published when a request is returned to reviewer by SMD/RDC Head.
 */
@Getter
public class RequestReturned extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID requestId;
    private final UUID returnerId;
    private final String returnerName;
    private final String reason;

    public RequestReturned(UUID requestId, UUID returnerId, String returnerName, 
                          String reason) {
        super();
        this.requestId = requestId;
        this.returnerId = returnerId;
        this.returnerName = returnerName;
        this.reason = reason;
    }

}
