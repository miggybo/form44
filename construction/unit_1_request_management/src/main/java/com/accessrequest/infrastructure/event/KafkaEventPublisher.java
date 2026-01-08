package com.accessrequest.infrastructure.event;

import com.accessrequest.domain.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka implementation of DomainEventPublisher.
 * Publishes domain events to Kafka topics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements DomainEventPublisher {

    private static final String TOPIC = "request.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing event: {} with ID: {}", event.getClass().getSimpleName(), event.getEventId());
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            Message<String> message = MessageBuilder
                .withPayload(eventJson)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(KafkaHeaders.MESSAGE_KEY, event.getEventId().toString())
                .setHeader("eventType", event.getClass().getSimpleName())
                .build();

            kafkaTemplate.send(message);
            log.info("Event published successfully: {} with ID: {}", event.getClass().getSimpleName(), event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getClass().getSimpleName(), e);
            throw new EventPublishingException("Failed to publish event: " + event.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        log.debug("Publishing {} events", events.size());
        for (DomainEvent event : events) {
            publish(event);
        }
        log.info("All {} events published successfully", events.size());
    }

    /**
     * Exception thrown when event publishing fails.
     */
    public static class EventPublishingException extends RuntimeException {
        public EventPublishingException(String message) {
            super(message);
        }

        public EventPublishingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
