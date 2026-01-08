package com.accessrequest.domain.event;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events.
 * 
 * Domain events represent something that has happened in the domain.
 * They are published to the event bus for asynchronous processing.
 */
@Getter
public abstract class DomainEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final UUID eventId;
    private final Instant timestamp;
    private final String eventType;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.eventType = this.getClass().getSimpleName();
    }

    /**
     * Get the event type name.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Get the event ID.
     */
    public UUID getEventId() {
        return eventId;
    }

    /**
     * Get the event timestamp.
     */
    public Instant getTimestamp() {
        return timestamp;
    }

}
