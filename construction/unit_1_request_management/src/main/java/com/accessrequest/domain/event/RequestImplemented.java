package com.accessrequest.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * RequestImplemented Domain Event.
 * 
 * Published when a request is marked as implemented by Administrator.
 */
@Getter
public class RequestImplemented extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID requestId;
    private final UUID implementerId;
    private final String implementerName;
    private final String notes;

    public RequestImplemented(UUID requestId, UUID implementerId, String implementerName, 
                             String notes) {
        super();
        this.requestId = requestId;
        this.implementerId = implementerId;
        this.implementerName = implementerName;
        this.notes = notes;
    }

}
