package com.accessrequest.infrastructure.event;

import com.accessrequest.domain.event.DomainEvent;

import java.util.List;

/**
 * Interface for publishing domain events.
 * Implementations handle event serialization and delivery.
 */
public interface DomainEventPublisher {

    /**
     * Publish a single domain event.
     *
     * @param event the domain event to publish
     */
    void publish(DomainEvent event);

    /**
     * Publish multiple domain events.
     *
     * @param events the list of domain events to publish
     */
    void publishAll(List<DomainEvent> events);
}
