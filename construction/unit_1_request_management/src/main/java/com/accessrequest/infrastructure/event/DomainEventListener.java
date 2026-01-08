package com.accessrequest.infrastructure.event;

import com.accessrequest.domain.event.DomainEvent;

/**
 * Interface for listening to and handling domain events.
 * Implementations handle specific event types.
 */
public interface DomainEventListener {

    /**
     * Handle a domain event.
     *
     * @param event the domain event to handle
     */
    void handle(DomainEvent event);

    /**
     * Get the event type this listener handles.
     *
     * @return the event type class name
     */
    String getEventType();
}
