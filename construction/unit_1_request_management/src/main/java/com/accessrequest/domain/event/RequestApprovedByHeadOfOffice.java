package com.accessrequest.domain.event;

import lombok.Getter;

import java.util.UUID;

/**
 * RequestApprovedByHeadOfOffice Domain Event.
 * 
 * Published when a request is approved by Head of Office.
 */
@Getter
public class RequestApprovedByHeadOfOffice extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID requestId;
    private final UUID approverId;
    private final String approverName;
    private final String comments;

    public RequestApprovedByHeadOfOffice(UUID requestId, UUID approverId, String approverName, 
                                        String comments) {
        super();
        this.requestId = requestId;
        this.approverId = approverId;
        this.approverName = approverName;
        this.comments = comments;
    }

}
