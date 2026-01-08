package com.accessrequest.infrastructure.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Client for integrating with Notification Service.
 * Provides methods to send notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceClient {

    private final RestTemplate restTemplate;

    @Value("${external.services.notification.url:http://localhost:8083}")
    private String notificationServiceUrl;

    /**
     * Send a notification.
     *
     * @param notification the notification to send
     */
    public void sendNotification(NotificationRequest notification) {
        log.debug("Sending notification to: {}", notification.getRecipientId());
        try {
            String url = notificationServiceUrl + "/api/v1/notifications";
            restTemplate.postForObject(url, notification, Void.class);
            log.info("Notification sent successfully to: {}", notification.getRecipientId());
        } catch (Exception e) {
            log.error("Error sending notification to: {}", notification.getRecipientId(), e);
            throw new ExternalServiceException("Failed to send notification", e);
        }
    }

    /**
     * Send email notification.
     *
     * @param recipientEmail the recipient email
     * @param subject the email subject
     * @param body the email body
     */
    public void sendEmailNotification(String recipientEmail, String subject, String body) {
        log.debug("Sending email notification to: {}", recipientEmail);
        try {
            NotificationRequest notification = NotificationRequest.builder()
                .recipientEmail(recipientEmail)
                .subject(subject)
                .body(body)
                .notificationType("EMAIL")
                .build();
            sendNotification(notification);
        } catch (Exception e) {
            log.error("Error sending email notification to: {}", recipientEmail, e);
            throw new ExternalServiceException("Failed to send email notification", e);
        }
    }

    /**
     * Send SMS notification.
     *
     * @param recipientPhone the recipient phone number
     * @param message the SMS message
     */
    public void sendSmsNotification(String recipientPhone, String message) {
        log.debug("Sending SMS notification to: {}", recipientPhone);
        try {
            NotificationRequest notification = NotificationRequest.builder()
                .recipientPhone(recipientPhone)
                .body(message)
                .notificationType("SMS")
                .build();
            sendNotification(notification);
        } catch (Exception e) {
            log.error("Error sending SMS notification to: {}", recipientPhone, e);
            throw new ExternalServiceException("Failed to send SMS notification", e);
        }
    }

    /**
     * DTO for notification request to Notification Service.
     */
    @Data
    public static class NotificationRequest {
        private UUID recipientId;
        private String recipientEmail;
        private String recipientPhone;
        private String subject;
        private String body;
        private String notificationType;
        private String priority;

        public static NotificationRequestBuilder builder() {
            return new NotificationRequestBuilder();
        }

        public static class NotificationRequestBuilder {
            private UUID recipientId;
            private String recipientEmail;
            private String recipientPhone;
            private String subject;
            private String body;
            private String notificationType;
            private String priority = "NORMAL";

            public NotificationRequestBuilder recipientId(UUID recipientId) {
                this.recipientId = recipientId;
                return this;
            }

            public NotificationRequestBuilder recipientEmail(String recipientEmail) {
                this.recipientEmail = recipientEmail;
                return this;
            }

            public NotificationRequestBuilder recipientPhone(String recipientPhone) {
                this.recipientPhone = recipientPhone;
                return this;
            }

            public NotificationRequestBuilder subject(String subject) {
                this.subject = subject;
                return this;
            }

            public NotificationRequestBuilder body(String body) {
                this.body = body;
                return this;
            }

            public NotificationRequestBuilder notificationType(String notificationType) {
                this.notificationType = notificationType;
                return this;
            }

            public NotificationRequestBuilder priority(String priority) {
                this.priority = priority;
                return this;
            }

            public NotificationRequest build() {
                NotificationRequest request = new NotificationRequest();
                request.recipientId = this.recipientId;
                request.recipientEmail = this.recipientEmail;
                request.recipientPhone = this.recipientPhone;
                request.subject = this.subject;
                request.body = this.body;
                request.notificationType = this.notificationType;
                request.priority = this.priority;
                return request;
            }
        }
    }

    /**
     * Exception thrown when external service call fails.
     */
    public static class ExternalServiceException extends RuntimeException {
        public ExternalServiceException(String message) {
            super(message);
        }

        public ExternalServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
