package com.accessrequest.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * RequestDeclined Domain Event.
 * 
 * Published when a request is declined at any stage.
 */
@Getter
public class RequestDeclined extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID requestId;
    private final UUID declinerId;
    private final String declinerName;
    private final String reason;

    public RequestDeclined(UUID requestId, UUID declinerId, String declinerName, 
                          String reason) {
        super();
        this.requestId = requestId;
        this.declinerId = declinerId;
        this.declinerName = declinerName;
        this.reason = reason;
    }

}
