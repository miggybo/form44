# Unit 3: Notification Service - Logical Design

## Document Information
- **Service**: Notification Service (Unit 3)
- **Purpose**: Manage email notifications throughout the access request lifecycle with reliable delivery, retry logic, and template management
- **Technology Stack**: Java, Spring Boot, PostgreSQL, Kafka, Spring Security, SMTP, Redis
- **Architecture Pattern**: Event-Driven Architecture with Layered Design
- **Date**: January 8, 2025
- **Status**: Design Phase

---

## Table of Contents

1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Layered Architecture Design](#layered-architecture-design)
4. [API Layer Design](#api-layer-design)
5. [Application Layer Design](#application-layer-design)
6. [Domain Layer Design](#domain-layer-design)
7. [Infrastructure Layer Design](#infrastructure-layer-design)
8. [Data Models & Database Design](#data-models--database-design)
9. [Event Handling Design](#event-handling-design)
10. [Error Handling & Validation](#error-handling--validation)
11. [Security Design](#security-design)
12. [Performance & Scalability](#performance--scalability)
13. [Testing Strategy](#testing-strategy)
14. [Deployment & Operations](#deployment--operations)
15. [Architecture Diagrams](#architecture-diagrams)

---

## Overview

### Purpose
The Notification Service is an event-driven, consumer-only service that manages all email notifications throughout the access request lifecycle. It listens to domain events from the Request Management Service, creates notifications, manages email templates, sends emails via SMTP, implements retry logic for failed notifications, and publishes notification status events to the Administration Service for audit logging and alerting.

### Scope
This logical design covers:
- Event consumption from Request Management Service
- Notification creation and lifecycle management
- Email template management with versioning and caching
- Email sending via SMTP with rate limiting (100 emails/second)
- Retry logic with exponential backoff (max 3 retries, 5s initial delay, 2x multiplier)
- Batch processing for failed notification retries
- Notification archival for completed requests
- REST API design for notification and template queries
- Database schema and persistence strategy
- Integration with Request Management, Administration services
- Metrics tracking (successful/unsuccessful notifications)

### Key Responsibilities
1. Consume domain events from Request Management Service
2. Create notifications based on request lifecycle events
3. Determine appropriate recipients for each event type
4. Render email templates with dynamic variables
5. Send emails via SMTP with rate limiting
6. Implement retry logic with exponential backoff for failed notifications
7. Archive notifications when requests are completed or declined
8. Manage email templates (admin-only updates)
9. Cache email templates in Redis for performance
10. Publish notification status events to Administration Service
11. Maintain audit trail of all notification attempts
12. Track metrics on successful/unsuccessful notifications

### Design Principles
- **Domain-Driven Design**: Rich domain model with aggregates, entities, and value objects
- **Event-Driven Architecture**: Asynchronous communication via Kafka message queue
- **Layered Architecture**: Clear separation of concerns (Presentation, Application, Domain, Infrastructure)
- **SOLID Principles**: Single responsibility, Open/closed, Liskov substitution, Interface segregation, Dependency inversion
- **Reliability**: Retry logic with exponential backoff, idempotent processing, immutable audit trails
- **Performance**: Email template caching, batch processing, rate limiting
- **Scalability**: Stateless services, asynchronous processing, horizontal scaling support
- **Security**: Role-based access control, secure email transmission, audit logging

---

## System Architecture

### High-Level System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Access Request Processing System                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │            Notification Service (Unit 3)                         │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ REST API Layer (Spring MVC)                                │  │   │
│  │  │ - Notification query endpoints                             │  │   │
│  │  │ - Email template query endpoints                           │  │   │
│  │  │ - Manual notification sending endpoint                     │  │   │
│  │  │ - Metrics endpoints                                        │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Application Layer (Spring Services)                        │  │   │
│  │  │ - NotificationApplicationService                           │  │   │
│  │  │ - EmailTemplateApplicationService                          │  │   │
│  │  │ - NotificationRetryApplicationService                      │  │   │
│  │  │ - NotificationMetricsApplicationService                    │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Domain Layer (DDD)                                         │  │   │
│  │  │ - Notification Aggregate                                   │  │   │
│  │  │ - EmailTemplate Aggregate                                  │  │   │
│  │  │ - Domain Services                                          │  │   │
│  │  │ - Value Objects                                            │  │   │
│  │  │ - Domain Events                                            │  │   │
│  │  │ - Policies                                                 │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Infrastructure Layer                                       │  │   │
│  │  │ - NotificationRepository (JPA)                             │  │   │
│  │  │ - EmailTemplateRepository (JPA)                            │  │   │
│  │  │ - EventPublisher (Kafka)                                   │  │   │
│  │  │ - EventListener (Kafka)                                    │  │   │
│  │  │ - SmtpEmailClient                                          │  │   │
│  │  │ - RedisCache                                               │  │   │
│  │  │ - Database (PostgreSQL)                                    │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ External Services (via REST APIs & Kafka Events)                │   │
│  │ - Request Management Service (Event Consumer)                   │   │
│  │ - Administration Service (Event Publisher)                      │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Infrastructure Components                                        │   │
│  │ - Kafka Message Queue (Event Bus)                               │   │
│  │ - PostgreSQL Database                                           │   │
│  │ - Redis Cache (Email Templates)                                 │   │
│  │ - SMTP Server                                                   │   │
│  │ - Spring Security (Authentication/Authorization)                │   │
│  │ - Docker Container                                              │   │
│  │ - Kubernetes Orchestration                                      │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Component Interactions

```
Request Management Service (Event Publisher)
    ↓
Kafka Topic (request.events)
    ↓
Event Listener (Kafka Consumer)
    ↓
Event Handler (Route to appropriate handler)
    ↓
NotificationApplicationService
    ↓
NotificationFactory (Create Notification aggregate)
    ↓
NotificationService (Domain Service)
    ↓
EmailTemplateService (Render template)
    ↓
EmailService (Send via SMTP)
    ↓
Notification Repository (Persist)
    ↓
Event Publisher (Kafka)
    ↓
Kafka Topic (notification.events)
    ↓
Administration Service (Event Consumer)
```

---

## Layered Architecture Design

### Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│ Presentation Layer (REST API)                               │
│ - Controllers                                               │
│ - Request/Response DTOs                                     │
│ - Input Validation                                          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Application Layer (Business Logic Orchestration)            │
│ - Application Services                                      │
│ - Command/Query Handlers                                    │
│ - Event Publishing                                          │
│ - Transaction Management                                    │
│ - Metrics Collection                                        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Domain Layer (Business Rules)                               │
│ - Aggregates (Notification, EmailTemplate)                  │
│ - Entities                                                  │
│ - Value Objects                                             │
│ - Domain Services                                           │
│ - Domain Events                                             │
│ - Policies                                                  │
│ - Specifications                                            │
│ - Factories                                                 │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Infrastructure Layer (Technical Implementation)             │
│ - Repositories (JPA/Hibernate)                              │
│ - Event Bus (Kafka)                                         │
│ - Database Access                                           │
│ - SMTP Email Client                                         │
│ - Redis Cache Client                                        │
│ - External Service Clients                                  │
│ - Security (Spring Security)                                │
│ - Logging & Monitoring                                      │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### Presentation Layer
- Receive HTTP requests from clients
- Validate input data
- Convert DTOs to domain objects
- Call application services
- Return HTTP responses with appropriate status codes
- Handle authentication/authorization
- Provide metrics endpoints

#### Application Layer
- Orchestrate domain logic
- Manage transactions
- Publish domain events
- Coordinate between aggregates and services
- Handle cross-cutting concerns
- Implement use cases (send notifications, manage templates, retry failed notifications)
- Collect and aggregate metrics

#### Domain Layer
- Encapsulate business rules
- Manage notification and template aggregate state
- Validate invariants (email format, template content, retry limits)
- Publish domain events
- Provide domain services (notification sending, template rendering, retry logic)
- Define specifications for queries

#### Infrastructure Layer
- Persist aggregates to PostgreSQL database
- Send emails via SMTP
- Cache email templates in Redis
- Publish/consume events via Kafka
- Integrate with external services
- Implement security policies
- Provide logging and monitoring


---

## API Layer Design

### REST API Endpoints

#### Notification Query Endpoints

**1. List Notifications**
- **Endpoint**: `GET /api/v1/notifications`
- **Authentication**: Required (JWT Token)
- **Authorization**: Authenticated users (can see own notifications), Administrators (can see all)
- **Query Parameters**:
  - `userId`: Filter by recipient user ID (optional, admin only for other users)
  - `status`: Filter by status (Pending, Sent, Failed, Retrying, Abandoned)
  - `notificationType`: Filter by notification type (RequestCreated, RequestSubmitted, etc.)
  - `fromDate`: Filter by date range start (ISO-8601)
  - `toDate`: Filter by date range end (ISO-8601)
  - `page`: Page number (default: 1)
  - `size`: Page size (default: 20, max: 100)
  - `sortBy`: Sort field (createdAt, sentAt, status)
  - `sortOrder`: Sort order (ASC, DESC)
- **Response** (200 OK):
  ```
  {
    "notifications": [
      {
        "notificationId": "string (UUID)",
        "notificationType": "string",
        "recipientEmail": "string",
        "status": "string",
        "createdAt": "ISO-8601 datetime",
        "sentAt": "ISO-8601 datetime",
        "requestId": "string (UUID)"
      }
    ],
    "totalCount": "number",
    "page": "number",
    "size": "number",
    "totalPages": "number"
  }
  ```

**2. Get Notification Details**
- **Endpoint**: `GET /api/v1/notifications/{notificationId}`
- **Authentication**: Required
- **Authorization**: Recipient, Administrator
- **Response** (200 OK):
  ```
  {
    "notificationId": "string (UUID)",
    "notificationType": "string",
    "recipientId": "string (UUID)",
    "recipientEmail": "string",
    "requestId": "string (UUID)",
    "status": "string",
    "subject": "string",
    "body": "string",
    "createdAt": "ISO-8601 datetime",
    "sentAt": "ISO-8601 datetime",
    "failureReason": "string (if failed)",
    "retryCount": "number",
    "nextRetryAt": "ISO-8601 datetime (if retrying)",
    "deliveryLog": [
      {
        "attemptNumber": "number",
        "attemptedAt": "ISO-8601 datetime",
        "status": "Success|Failed",
        "errorMessage": "string (if failed)"
      }
    ]
  }
  ```

**3. Get User Notifications**
- **Endpoint**: `GET /api/v1/notifications/user/{userId}`
- **Authentication**: Required
- **Authorization**: User (own notifications), Administrator
- **Query Parameters**:
  - `status`: Filter by status
  - `page`: Page number
  - `size`: Page size
- **Response** (200 OK):
  ```
  {
    "userId": "string (UUID)",
    "notifications": [
      {
        "notificationId": "string",
        "notificationType": "string",
        "status": "string",
        "createdAt": "datetime",
        "sentAt": "datetime"
      }
    ],
    "totalCount": "number"
  }
  ```

**4. Search Notifications**
- **Endpoint**: `GET /api/v1/notifications/search`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `q`: Search query (notificationId, recipientEmail, requestId)
  - `status`: Filter by status
  - `notificationType`: Filter by notification type
  - `fromDate`: Date range start
  - `toDate`: Date range end
  - `page`: Page number
  - `size`: Page size
- **Response** (200 OK):
  ```
  {
    "results": [
      {
        "notificationId": "string",
        "notificationType": "string",
        "recipientEmail": "string",
        "status": "string",
        "createdAt": "datetime"
      }
    ],
    "totalCount": "number"
  }
  ```

#### Email Template Endpoints

**5. List Email Templates**
- **Endpoint**: `GET /api/v1/email-templates`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```
  {
    "templates": [
      {
        "templateId": "string (UUID)",
        "templateType": "string",
        "name": "string",
        "subject": "string",
        "version": "string",
        "createdAt": "ISO-8601 datetime",
        "updatedAt": "ISO-8601 datetime"
      }
    ]
  }
  ```

**6. Get Email Template by Type**
- **Endpoint**: `GET /api/v1/email-templates/{templateType}`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```
  {
    "templateId": "string (UUID)",
    "templateType": "string",
    "name": "string",
    "subject": "string",
    "body": "string (HTML)",
    "variables": [
      {
        "name": "string",
        "description": "string",
        "required": "boolean"
      }
    ],
    "version": "string",
    "createdAt": "ISO-8601 datetime",
    "updatedAt": "ISO-8601 datetime"
  }
  ```

**7. Preview Email Template**
- **Endpoint**: `POST /api/v1/email-templates/{templateType}/preview`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```
  {
    "variables": {
      "requestorName": "string",
      "requestId": "string",
      "systemName": "string"
    }
  }
  ```
- **Response** (200 OK):
  ```
  {
    "subject": "string",
    "body": "string (HTML rendered with variables)"
  }
  ```

**8. Reset Email Template to Default**
- **Endpoint**: `POST /api/v1/email-templates/{templateType}/reset`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```
  {
    "templateType": "string",
    "resetAt": "ISO-8601 datetime",
    "message": "Template reset to default version"
  }
  ```

#### Metrics Endpoints

**9. Get Notification Metrics**
- **Endpoint**: `GET /api/v1/metrics/notifications`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `fromDate`: Date range start (ISO-8601)
  - `toDate`: Date range end (ISO-8601)
  - `groupBy`: Group by (day, hour, notificationType)
- **Response** (200 OK):
  ```
  {
    "period": {
      "fromDate": "ISO-8601 datetime",
      "toDate": "ISO-8601 datetime"
    },
    "summary": {
      "totalNotifications": "number",
      "successfulNotifications": "number",
      "failedNotifications": "number",
      "retriedNotifications": "number",
      "abandonedNotifications": "number",
      "successRate": "number (percentage)"
    },
    "byType": [
      {
        "notificationType": "string",
        "total": "number",
        "successful": "number",
        "failed": "number",
        "successRate": "number"
      }
    ],
    "byStatus": [
      {
        "status": "string",
        "count": "number"
      }
    ]
  }
  ```

**10. Get Retry Metrics**
- **Endpoint**: `GET /api/v1/metrics/retries`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `fromDate`: Date range start
  - `toDate`: Date range end
- **Response** (200 OK):
  ```
  {
    "totalRetries": "number",
    "successfulRetries": "number",
    "failedRetries": "number",
    "abandonedNotifications": "number",
    "averageRetriesPerNotification": "number",
    "retrySuccessRate": "number (percentage)"
  }
  ```

### Error Response Format

All error responses follow this standard format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {
      "field": "error details"
    },
    "timestamp": "ISO-8601 datetime",
    "path": "/api/v1/notifications"
  }
}
```

### Common Error Codes

- `INVALID_INPUT`: Input validation failed
- `UNAUTHORIZED`: Authentication required
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Notification or template not found
- `INVALID_TEMPLATE_VARIABLES`: Required template variables missing
- `EMAIL_SEND_FAILED`: Email sending failed
- `RATE_LIMIT_EXCEEDED`: Email sending rate limit exceeded
- `INTERNAL_ERROR`: Internal server error


---

## Application Layer Design

### Application Services

The Application Layer contains Spring Services that orchestrate domain logic and manage transactions.

#### NotificationApplicationService

**Responsibilities**:
- Handle incoming domain events from Request Management Service
- Create notifications from events
- Determine recipients based on event type
- Coordinate notification sending
- Query notifications with various filters
- Manage transactions

**Key Methods**:
- `handleRequestCreated(RequestCreatedEvent): void`
  - Validates event
  - Calls NotificationFactory to create notification
  - Calls NotificationService to send notification to requestor
  - Saves notification to repository
  - Publishes NotificationSent or NotificationFailed event
  - Logs metrics

- `handleRequestSubmitted(RequestSubmittedEvent): void`
  - Creates notification for Head of Office
  - Sends notification
  - Publishes event

- `handleRequestApprovedByHeadOfOffice(RequestApprovedByHeadOfOfficeEvent): void`
  - Creates notifications for requestor and reviewer
  - Sends notifications
  - Publishes events

- `handleRequestEndorsed(RequestEndorsedEvent): void`
  - Creates notification for SMD/RDC Head
  - Sends notification
  - Publishes event

- `handleRequestFinallyApproved(RequestFinallyApprovedEvent): void`
  - Creates notification for administrator
  - Sends notification
  - Publishes event

- `handleRequestDeclined(RequestDeclinedEvent): void`
  - Creates notification for requestor
  - Sends notification
  - Archives related notifications
  - Publishes event

- `handleRequestReturned(RequestReturnedEvent): void`
  - Creates notification for reviewer
  - Sends notification
  - Publishes event

- `handleRequestImplemented(RequestImplementedEvent): void`
  - Creates notification for requestor
  - Sends notification
  - Archives related notifications
  - Publishes event

- `getNotifications(GetNotificationsQuery): Page<NotificationDTO>`
  - Applies filters and pagination
  - Uses NotificationRepository specifications
  - Returns paginated results

- `searchNotifications(SearchQuery): List<NotificationDTO>`
  - Performs full-text search
  - Applies filters
  - Returns search results

- `getUserNotifications(userId, filters): List<NotificationDTO>`
  - Queries notifications for specific user
  - Applies filters
  - Returns results

#### EmailTemplateApplicationService

**Responsibilities**:
- Manage email template queries
- Render templates with variables
- Preview templates
- Reset templates to defaults
- Cache templates in Redis

**Key Methods**:
- `getTemplate(templateType): EmailTemplateDTO`
  - Checks Redis cache first
  - If not cached, queries repository
  - Caches result in Redis (TTL: 1 hour)
  - Returns EmailTemplateDTO

- `renderTemplate(templateType, variables): RenderedEmailDTO`
  - Gets template from cache or repository
  - Validates all required variables are provided
  - Renders template with variables
  - Returns rendered subject and body

- `previewTemplate(templateType, variables): RenderedEmailDTO`
  - Calls renderTemplate()
  - Returns preview for admin review

- `resetTemplate(templateType): EmailTemplateDTO`
  - Loads default template
  - Saves to repository
  - Invalidates Redis cache
  - Publishes TemplateUpdated event
  - Returns updated template

- `listTemplates(): List<EmailTemplateDTO>`
  - Queries all templates
  - Returns list of templates

#### NotificationRetryApplicationService

**Responsibilities**:
- Handle failed notification retries
- Manage batch retry processing
- Determine retry eligibility
- Schedule retries
- Update retry status

**Key Methods**:
- `retryFailedNotifications(): void`
  - Queries failed notifications that are due for retry
  - Processes in batches (configurable batch size)
  - For each notification:
    - Checks if max retries exceeded
    - If not, calculates next retry time
    - Calls NotificationService to retry
    - Updates notification status
    - Publishes NotificationRetried or NotificationAbandoned event
  - Logs metrics

- `retryNotification(notificationId): NotificationDTO`
  - Loads notification from repository
  - Validates retry eligibility
  - Calls NotificationService to retry
  - Updates notification status
  - Saves to repository
  - Publishes event
  - Returns updated notification

- `abandonNotification(notificationId): NotificationDTO`
  - Loads notification from repository
  - Updates status to Abandoned
  - Saves to repository
  - Publishes NotificationAbandoned event
  - Logs alert
  - Returns updated notification

- `archiveNotifications(requestId): void`
  - Queries all notifications for request
  - Updates status to Archived
  - Saves to repository
  - Invalidates cache

#### NotificationMetricsApplicationService

**Responsibilities**:
- Collect and aggregate notification metrics
- Track successful/unsuccessful notifications
- Generate metrics reports

**Key Methods**:
- `getNotificationMetrics(fromDate, toDate, groupBy): MetricsDTO`
  - Queries notifications in date range
  - Aggregates by status
  - Calculates success rate
  - Groups by type if requested
  - Returns metrics

- `getRetryMetrics(fromDate, toDate): RetryMetricsDTO`
  - Queries retry attempts
  - Calculates success rate
  - Counts abandoned notifications
  - Returns retry metrics

- `recordNotificationSent(notificationId): void`
  - Increments successful notification counter
  - Updates metrics

- `recordNotificationFailed(notificationId): void`
  - Increments failed notification counter
  - Updates metrics

### Event Publishing Strategy

#### Event Publisher Interface

```
interface DomainEventPublisher {
  void publish(DomainEvent event);
  void publishAll(List<DomainEvent> events);
}
```

#### Kafka Event Publisher Implementation

- Converts domain events to JSON
- Publishes to Kafka topic `notification.events`
- Handles serialization/deserialization
- Implements retry logic with exponential backoff
- Logs all published events
- Tracks publishing metrics

#### Event Publishing Flow

1. Domain aggregate publishes event (added to event list)
2. Application service saves aggregate to repository
3. Application service calls EventPublisher.publishAll()
4. EventPublisher converts events to JSON
5. EventPublisher publishes to Kafka topic
6. Administration Service consumes events from Kafka

### Event Consumption Strategy

#### Event Listener Interface

```
interface DomainEventListener {
  void handle(DomainEvent event);
  String getEventType();
}
```

#### Kafka Event Listener Implementation

- Listens to Kafka topic `request.events` (for consumed events)
- Deserializes JSON to domain events
- Routes events to appropriate handlers
- Implements idempotency (prevents duplicate processing)
- Logs all consumed events
- Handles processing errors with dead letter queue

#### Consumed Events

**RequestCreated** (from Request Management Service)
- Handler: RequestCreatedEventHandler
- Action: Create notification for requestor
- Idempotency: Check if notification already created for this request

**RequestSubmitted** (from Request Management Service)
- Handler: RequestSubmittedEventHandler
- Action: Create notification for Head of Office
- Idempotency: Check if notification already created

**RequestApprovedByHeadOfOffice** (from Request Management Service)
- Handler: RequestApprovedByHeadOfOfficeEventHandler
- Action: Create notifications for requestor and reviewer
- Idempotency: Check if notifications already created

**RequestEndorsed** (from Request Management Service)
- Handler: RequestEndorsedEventHandler
- Action: Create notification for SMD/RDC Head
- Idempotency: Check if notification already created

**RequestFinallyApproved** (from Request Management Service)
- Handler: RequestFinallyApprovedEventHandler
- Action: Create notification for administrator
- Idempotency: Check if notification already created

**RequestDeclined** (from Request Management Service)
- Handler: RequestDeclinedEventHandler
- Action: Create notification for requestor, archive related notifications
- Idempotency: Check if notification already created

**RequestReturned** (from Request Management Service)
- Handler: RequestReturnedEventHandler
- Action: Create notification for reviewer
- Idempotency: Check if notification already created

**RequestImplemented** (from Request Management Service)
- Handler: RequestImplementedEventHandler
- Action: Create notification for requestor, archive related notifications
- Idempotency: Check if notification already created

### Transaction Management

#### Transaction Boundaries

- Each application service method is a transaction boundary
- Uses Spring @Transactional annotation
- Propagation: REQUIRED (join existing or create new)
- Isolation: READ_COMMITTED (default)
- Rollback on RuntimeException

#### Saga Pattern for Distributed Transactions

For operations spanning multiple services:
1. Notification Service publishes event
2. Administration Service consumes event and performs action
3. If any step fails, compensating transactions are triggered

### Command & Query Separation (CQRS)

#### Commands (Write Operations)

- SendNotificationCommand
- RetryNotificationCommand
- AbandonNotificationCommand
- ArchiveNotificationsCommand
- ResetTemplateCommand

#### Queries (Read Operations)

- GetNotificationQuery
- ListNotificationsQuery
- SearchNotificationsQuery
- GetTemplateQuery
- ListTemplatesQuery
- GetMetricsQuery
- GetRetryMetricsQuery

#### Command Handler Pattern

```
interface CommandHandler<C extends Command, R> {
  R handle(C command);
}
```

Each command has a dedicated handler that:
1. Validates command
2. Loads aggregate from repository
3. Calls domain service method
4. Saves aggregate
5. Publishes events
6. Returns result

### Caching Strategy

#### Redis Cache Configuration

- **Cache Key Pattern**: `template:{templateType}`, `templates:all`
- **TTL**: 1 hour for email templates
- **Invalidation**: On template reset or update
- **Serialization**: JSON format

#### Cache Layers

1. **Email Template Cache**: Caches email template details (subject, body, variables)
2. **Template List Cache**: Caches list of all templates

#### Cache Invalidation Strategy

- Invalidate on template reset
- Invalidate on template update
- Automatic expiration after TTL
- Manual invalidation on demand


---

## Domain Layer Design

### Aggregate Design

#### Notification Aggregate

**Aggregate Root**: Notification
- **Identifier**: NotificationId (UUID)
- **Lifecycle**: Pending → Sent/Failed → Retrying → Abandoned/Archived
- **Invariants**:
  - Notification must have valid recipient email
  - Notification must have valid notification type
  - Notification must have valid template
  - Notification cannot be sent without template variables
  - Notification cannot exceed maximum retry attempts (3)
  - Notification must have at least one delivery attempt
  - Notification status transitions must be valid

**Entities within Aggregate**:
- NotificationLog: Tracks notification delivery attempts (immutable, append-only)
  - Attributes: attemptNumber, attemptedAt, status (Success, Failed), errorMessage
  - Behavior: append-only, immutable

**Value Objects within Aggregate**:
- NotificationId: Unique identifier (UUID)
- NotificationType: Type of notification (immutable)
  - Values: RequestCreated, RequestSubmitted, RequestApprovedByHeadOfOffice, RequestEndorsed, RequestFinallyApproved, RequestDeclined, RequestReturned, RequestImplemented
  - Behavior: maps to email template
- NotificationStatus: State machine with validation (immutable)
  - Values: Pending, Sent, Failed, Retrying, Abandoned, Archived
  - Behavior: validates state transitions
- EmailAddress: Email address with validation (immutable)
  - Attributes: address
  - Behavior: validates email format (RFC 5322), normalizes to lowercase
- RetryStrategy: Encapsulates retry logic (immutable)
  - Attributes: maxRetries (3), retryDelayMs (5000), backoffMultiplier (2)
  - Behavior: calculates next retry time
- NotificationPayload: Encapsulates notification data (immutable)
  - Attributes: requestId, recipientId, templateVariables (map)
  - Behavior: validates required variables
- NotificationDeliveryResult: Encapsulates delivery result (immutable)
  - Attributes: success (boolean), sentAt, errorMessage (if failed)
  - Behavior: immutable

**Aggregate Behavior**:
- `send(): void`: Transition from Pending to Sent
- `fail(errorMessage): void`: Transition from Pending/Retrying to Failed
- `retry(): void`: Transition from Failed to Retrying
- `abandon(): void`: Transition from Retrying to Abandoned
- `archive(): void`: Transition to Archived
- `canRetry(): boolean`: Check if notification can be retried
- `logAttempt(status, errorMessage): void`: Record delivery attempt
- `getNextRetryTime(): DateTime`: Calculate next retry time

#### EmailTemplate Aggregate

**Aggregate Root**: EmailTemplate
- **Identifier**: TemplateId (UUID)
- **Invariants**:
  - Template must have name
  - Template must have subject (max 100 characters)
  - Template must have body (max 10000 characters)
  - Template must have at least one version
  - Template type must be unique
  - All template variables must be defined

**Entities within Aggregate**:
- TemplateVersion: Tracks template versions (immutable)
  - Attributes: version, subject, body, createdAt, createdBy
  - Behavior: immutable

**Value Objects within Aggregate**:
- TemplateId: Unique identifier (UUID)
- TemplateType: Type of template (immutable)
  - Values: RequestCreated, RequestSubmitted, RequestApprovedByHeadOfOffice, RequestEndorsed, RequestFinallyApproved, RequestDeclined, RequestReturned, RequestImplemented
  - Behavior: immutable
- TemplateContent: Encapsulates template content (immutable)
  - Attributes: subject, body (HTML)
  - Behavior: validates content
- TemplateVariable: Encapsulates template variable (immutable)
  - Attributes: name, description, required (boolean)
  - Behavior: immutable
- TemplateVersion: Version information (immutable)
  - Attributes: version (semantic versioning), createdAt, createdBy
  - Behavior: immutable

**Aggregate Behavior**:
- `renderTemplate(variables): RenderedEmail`: Render template with variables
- `validateVariables(variables): ValidationResult`: Validate required variables
- `resetToDefault(): void`: Reset to default version
- `getVersion(version): TemplateVersion`: Get specific version

### Domain Services

#### NotificationService

**Purpose**: Sends notifications based on events and manages notification lifecycle

**Methods**:
- `sendNotification(notification): NotificationDeliveryResult`
  - Validates notification state
  - Gets email template
  - Renders template with variables
  - Calls EmailService to send email
  - Updates notification status
  - Logs delivery attempt
  - Returns delivery result

- `handleRequestCreated(event): void`
  - Determines recipient (requestor)
  - Creates notification
  - Sends notification

- `handleRequestSubmitted(event): void`
  - Determines recipient (Head of Office)
  - Creates notification
  - Sends notification

- `handleRequestApprovedByHeadOfOffice(event): void`
  - Determines recipients (requestor, reviewer)
  - Creates notifications
  - Sends notifications

- `handleRequestEndorsed(event): void`
  - Determines recipient (SMD/RDC Head)
  - Creates notification
  - Sends notification

- `handleRequestFinallyApproved(event): void`
  - Determines recipient (administrator)
  - Creates notification
  - Sends notification

- `handleRequestDeclined(event): void`
  - Determines recipient (requestor)
  - Creates notification
  - Sends notification

- `handleRequestReturned(event): void`
  - Determines recipient (reviewer)
  - Creates notification
  - Sends notification

- `handleRequestImplemented(event): void`
  - Determines recipient (requestor)
  - Creates notification
  - Sends notification

#### EmailTemplateService

**Purpose**: Manages and renders email templates

**Methods**:
- `getTemplate(templateType): EmailTemplate`
  - Queries repository
  - Returns template

- `renderTemplate(templateType, variables): RenderedEmail`
  - Gets template
  - Validates variables
  - Renders template with variables
  - Returns rendered email

- `previewTemplate(templateType, variables): RenderedEmail`
  - Calls renderTemplate()
  - Returns preview

- `resetTemplate(templateType): EmailTemplate`
  - Loads default template
  - Saves to repository
  - Returns updated template

- `validateTemplateVariables(template, variables): ValidationResult`
  - Checks all required variables are provided
  - Returns validation result

#### NotificationRetryService

**Purpose**: Handles failed notification retries with exponential backoff

**Methods**:
- `retryFailedNotifications(): void`
  - Queries failed notifications due for retry
  - Processes in batches
  - For each notification:
    - Checks if max retries exceeded
    - If not, calculates next retry time
    - Calls NotificationService to retry
    - Updates notification status
    - Publishes event

- `shouldRetry(notification): boolean`
  - Checks if notification can be retried
  - Validates retry count < max retries
  - Validates current time >= next retry time
  - Returns boolean

- `calculateNextRetryTime(notification): DateTime`
  - Gets retry count
  - Calculates delay: initialDelay * (backoffMultiplier ^ retryCount)
  - Returns next retry time

- `retryNotification(notificationId): NotificationDeliveryResult`
  - Loads notification
  - Validates retry eligibility
  - Calls NotificationService to retry
  - Updates notification status
  - Returns delivery result

- `abandonNotification(notificationId): void`
  - Loads notification
  - Updates status to Abandoned
  - Publishes NotificationAbandoned event
  - Logs alert

#### EmailService

**Purpose**: Sends emails via SMTP with rate limiting

**Methods**:
- `sendEmail(to, subject, body): EmailDeliveryResult`
  - Validates email address
  - Checks rate limit (100 emails/second)
  - Sends email via SMTP
  - Handles SMTP errors
  - Returns delivery result

- `sendBulkEmails(recipients, subject, body): List<EmailDeliveryResult>`
  - Validates email addresses
  - Checks rate limit
  - Sends emails in batches
  - Handles errors
  - Returns delivery results

- `testConnection(): boolean`
  - Tests SMTP connection
  - Returns connection status

### Repositories

#### NotificationRepository

**Purpose**: Persists and retrieves Notification aggregates

**Query Methods**:
- `findById(notificationId): Notification`
- `findByRecipient(recipientId): List<Notification>`
- `findByStatus(status): List<Notification>`
- `findFailedNotifications(): List<Notification>`
- `findPendingNotifications(): List<Notification>`
- `findRetryingNotifications(): List<Notification>`
- `findByDateRange(startDate, endDate): List<Notification>`
- `findByNotificationType(notificationType): List<Notification>`
- `findByRequestId(requestId): List<Notification>`
- `search(criteria): List<Notification>`

**Persistence Methods**:
- `save(notification): void`
- `update(notification): void`
- `delete(notificationId): void`

#### EmailTemplateRepository

**Purpose**: Persists and retrieves EmailTemplate aggregates

**Query Methods**:
- `findById(templateId): EmailTemplate`
- `findByType(templateType): EmailTemplate`
- `findAll(): List<EmailTemplate>`
- `findByVersion(templateId, version): EmailTemplate`
- `getDefaultTemplate(templateType): EmailTemplate`

**Persistence Methods**:
- `save(template): void`
- `update(template): void`
- `delete(templateId): void`
- `saveVersion(templateId, version): void`

### Specifications (Query Objects)

#### FailedNotificationsSpecification

**Purpose**: Query failed notifications for retry

**Criteria**: status (Failed), attemptCount < maxRetries, nextRetryTime <= now

**Returns**: List<Notification>

#### NotificationsByTypeSpecification

**Purpose**: Query notifications by type

**Criteria**: notificationType

**Returns**: List<Notification>

#### PendingNotificationsSpecification

**Purpose**: Query pending notifications

**Criteria**: status (Pending)

**Returns**: List<Notification>

#### NotificationsByRecipientSpecification

**Purpose**: Query notifications for a specific recipient

**Criteria**: recipientId

**Returns**: List<Notification>

#### NotificationsByDateRangeSpecification

**Purpose**: Query notifications within a date range

**Criteria**: startDate, endDate

**Returns**: List<Notification>

#### OverdueNotificationsSpecification

**Purpose**: Query notifications that are overdue for retry

**Criteria**: status (Retrying), nextRetryTime < now

**Returns**: List<Notification>

#### ArchivedNotificationsSpecification

**Purpose**: Query archived notifications

**Criteria**: status (Archived), createdAt < archiveDate

**Returns**: List<Notification>

### Policies

#### NotificationRetryPolicy

**Purpose**: Defines retry logic for failed notifications

**Rules**:
- Maximum 3 retry attempts
- Initial retry delay: 5 seconds
- Exponential backoff: delay multiplied by 2 for each retry
- Retry delays: 5s, 10s, 20s
- After 3 failed attempts, notification is abandoned
- Abandoned notifications trigger alert

**Retry Logic**:
- Attempt 1: Immediate
- Attempt 2: After 5 seconds
- Attempt 3: After 10 seconds
- Attempt 4: After 20 seconds
- Abandoned: After 4th failure

**Validation Methods**:
- `shouldRetry(notification): boolean`
- `calculateNextRetryTime(notification): DateTime`
- `isMaxRetriesExceeded(notification): boolean`

#### NotificationDeliveryPolicy

**Purpose**: Determines recipients for each event

**Rules**:
- RequestCreated: Send to requestor
- RequestSubmitted: Send to Head of Office
- RequestApprovedByHeadOfOffice: Send to requestor and reviewer
- RequestEndorsed: Send to SMD/RDC Head
- RequestFinallyApproved: Send to administrator
- RequestDeclined: Send to requestor
- RequestReturned: Send to reviewer
- RequestImplemented: Send to requestor

**Recipient Determination**:
- Requestor: Employee who created request
- Head of Office: Manager of requestor's office
- Reviewer: Assigned SMD/RDC Reviewer
- SMD/RDC Head: Head of SMD/RDC division
- Administrator: Assigned System or Database Administrator

**Validation Methods**:
- `getRecipientsForEvent(eventType): List<UserId>`
- `isValidRecipient(userId, eventType): boolean`

#### TemplateValidationPolicy

**Purpose**: Validates email templates

**Rules**:
- Template must have subject
- Template must have body
- Subject must not exceed 100 characters
- Body must not exceed 10000 characters
- All template variables must be defined
- Template must have at least one version
- Template type must be unique

**Validation Methods**:
- `isValidTemplate(template): boolean`
- `validateTemplateContent(content): ValidationResult`
- `validateTemplateVariables(template): ValidationResult`

#### EmailSendingPolicy

**Purpose**: Enforces email sending rate limits and constraints

**Rules**:
- Maximum 100 emails per second
- Email addresses must be valid (RFC 5322)
- Subject must not exceed 100 characters
- Body must not exceed 10000 characters
- Retry on transient SMTP errors
- Fail on permanent SMTP errors

**Validation Methods**:
- `isRateLimitExceeded(): boolean`
- `isValidEmailAddress(email): boolean`
- `isTransientError(error): boolean`

### Factory Patterns

#### NotificationFactory

**Purpose**: Creates Notification aggregates from events

**Creation Methods**:
- `createFromRequestCreated(event): Notification`
- `createFromRequestSubmitted(event): Notification`
- `createFromRequestApprovedByHeadOfOffice(event): List<Notification>`
- `createFromRequestEndorsed(event): Notification`
- `createFromRequestFinallyApproved(event): Notification`
- `createFromRequestDeclined(event): Notification`
- `createFromRequestReturned(event): Notification`
- `createFromRequestImplemented(event): Notification`

**Responsibilities**:
- Create notifications from domain events
- Determine recipients
- Set up retry strategy
- Initialize notification in Pending status
- Validate notification invariants

#### EmailTemplateFactory

**Purpose**: Creates EmailTemplate aggregates with validation

**Creation Method**:
- `createTemplate(templateType, subject, body, variables): EmailTemplate`
  - Validates template content
  - Validates variables
  - Generates TemplateId
  - Creates initial version
  - Returns new EmailTemplate aggregate

**Responsibilities**:
- Create templates with validation
- Generate unique TemplateId
- Initialize template with default version
- Set up template variables

#### NotificationLogFactory

**Purpose**: Creates NotificationLog entries

**Creation Method**:
- `createLogEntry(notificationId, status, errorMessage): NotificationLog`
  - Captures current timestamp
  - Creates immutable entry
  - Returns NotificationLog entity

**Responsibilities**:
- Create immutable log entries
- Capture delivery attempt information
- Timestamp all entries


---

## Infrastructure Layer Design

### Repository Implementations

#### NotificationRepository (JPA/Hibernate)

**Implementation Details**:
- Uses Spring Data JPA for persistence
- Implements custom query methods using @Query annotations
- Uses Specifications pattern for complex queries
- Implements pagination and sorting
- Handles transaction management

**Key Implementation Patterns**:
- Entity mapping: Notification aggregate to JPA entity
- Cascade operations: Cascade delete for NotificationLog entities
- Lazy loading: Lazy load notification logs
- Indexing: Indexes on status, recipientId, createdAt, nextRetryAt

#### EmailTemplateRepository (JPA/Hibernate)

**Implementation Details**:
- Uses Spring Data JPA for persistence
- Caches frequently accessed templates in Redis
- Implements version management
- Handles template updates with versioning

**Key Implementation Patterns**:
- Entity mapping: EmailTemplate aggregate to JPA entity
- Cascade operations: Cascade delete for TemplateVersion entities
- Caching: Cache templates in Redis with 1-hour TTL
- Versioning: Maintain version history

### Event Publisher (Kafka)

**Implementation Details**:
- Uses Spring Kafka for event publishing
- Publishes to topic: `notification.events`
- Implements JSON serialization
- Handles publishing errors with retry logic
- Logs all published events

**Event Publishing Flow**:
1. Domain aggregate publishes event (added to event list)
2. Application service saves aggregate to repository
3. Application service calls EventPublisher.publishAll()
4. EventPublisher converts events to JSON
5. EventPublisher publishes to Kafka topic
6. Administration Service consumes events from Kafka

**Published Events**:
- NotificationSent
- NotificationFailed
- NotificationRetried
- NotificationAbandoned
- TemplateUpdated

### Event Listener (Kafka)

**Implementation Details**:
- Uses Spring Kafka for event consumption
- Listens to topic: `request.events`
- Implements JSON deserialization
- Routes events to appropriate handlers
- Implements idempotency checking
- Handles processing errors with dead letter queue

**Event Consumption Flow**:
1. Kafka consumer receives event
2. Event deserializer converts JSON to domain event
3. Event router routes to appropriate handler
4. Handler processes event
5. Handler publishes new events if needed
6. Idempotency check prevents duplicate processing

**Consumed Events**:
- RequestCreated
- RequestSubmitted
- RequestApprovedByHeadOfOffice
- RequestEndorsed
- RequestFinallyApproved
- RequestDeclined
- RequestReturned
- RequestImplemented

### SMTP Email Client

**Implementation Details**:
- Uses Spring Mail (JavaMailSender)
- Configures SMTP connection parameters
- Implements rate limiting (100 emails/second)
- Handles SMTP errors and retries
- Logs email sending attempts

**Configuration**:
- SMTP host, port, username, password
- TLS/SSL configuration
- Connection timeout
- Read timeout
- Rate limiter configuration

**Error Handling**:
- Transient errors: Retry with exponential backoff
- Permanent errors: Fail immediately
- Connection errors: Retry with backoff

### Redis Cache Client

**Implementation Details**:
- Uses Spring Data Redis
- Caches email templates
- Implements cache invalidation
- Handles cache misses

**Cache Configuration**:
- Host, port, password
- Connection pool size
- TTL: 1 hour for templates
- Serialization: JSON format

**Cache Keys**:
- `template:{templateType}`: Individual template
- `templates:all`: List of all templates

### Security Implementation (Spring Security)

**Authentication**:
- JWT token-based authentication
- Token validation on each request
- Token refresh mechanism

**Authorization**:
- Role-based access control (RBAC)
- Roles: Employee, Head of Office, Reviewer, Head, System Admin, DB Admin
- Method-level security annotations
- Endpoint-level authorization checks

**Data Protection**:
- Email addresses encrypted at rest
- Secure transmission via HTTPS
- Audit logging of sensitive operations

### Logging & Monitoring

**Logging Strategy**:
- Structured logging with SLF4J
- Log levels: DEBUG, INFO, WARN, ERROR
- Log aggregation with ELK stack
- Correlation IDs for request tracing

**Monitoring**:
- Metrics collection with Micrometer
- Prometheus metrics export
- Health checks
- Performance monitoring
- Alert configuration

**Metrics**:
- Notification sent count
- Notification failed count
- Notification retry count
- Notification abandoned count
- Email sending rate
- Template cache hit rate
- Event processing latency

---

## Data Models & Database Design

### Database Schema

#### notifications Table

**Purpose**: Stores notification aggregates

**Columns**:
- `notification_id` (UUID, PRIMARY KEY): Unique notification identifier
- `notification_type` (VARCHAR, NOT NULL): Type of notification
- `recipient_id` (UUID, NOT NULL): Recipient user ID
- `recipient_email` (VARCHAR, NOT NULL): Recipient email address
- `request_id` (UUID, NOT NULL): Associated request ID
- `status` (VARCHAR, NOT NULL): Current notification status
- `subject` (VARCHAR, NOT NULL): Email subject
- `body` (TEXT, NOT NULL): Email body (HTML)
- `created_at` (TIMESTAMP, NOT NULL): Creation timestamp
- `sent_at` (TIMESTAMP): Sent timestamp
- `retry_count` (INTEGER, DEFAULT 0): Number of retry attempts
- `next_retry_at` (TIMESTAMP): Next scheduled retry time
- `failure_reason` (TEXT): Reason for failure
- `archived_at` (TIMESTAMP): Archival timestamp

**Indexes**:
- PRIMARY KEY: notification_id
- FOREIGN KEY: recipient_id (references users table)
- FOREIGN KEY: request_id (references access_requests table)
- INDEX: status (for querying by status)
- INDEX: recipient_id (for querying by recipient)
- INDEX: created_at (for date range queries)
- INDEX: next_retry_at (for retry queries)
- INDEX: request_id (for request-based queries)

**Constraints**:
- NOT NULL: notification_id, notification_type, recipient_id, recipient_email, request_id, status, created_at
- UNIQUE: None (multiple notifications per request allowed)
- CHECK: status IN ('Pending', 'Sent', 'Failed', 'Retrying', 'Abandoned', 'Archived')

#### notification_log Table

**Purpose**: Stores immutable delivery attempt logs

**Columns**:
- `log_id` (UUID, PRIMARY KEY): Unique log entry identifier
- `notification_id` (UUID, NOT NULL, FOREIGN KEY): Reference to notification
- `attempt_number` (INTEGER, NOT NULL): Attempt number (1, 2, 3, etc.)
- `attempted_at` (TIMESTAMP, NOT NULL): Attempt timestamp
- `status` (VARCHAR, NOT NULL): Attempt status (Success, Failed)
- `error_message` (TEXT): Error message if failed
- `smtp_code` (INTEGER): SMTP error code if failed

**Indexes**:
- PRIMARY KEY: log_id
- FOREIGN KEY: notification_id (references notifications table)
- INDEX: notification_id (for querying logs by notification)
- INDEX: attempted_at (for date range queries)

**Constraints**:
- NOT NULL: log_id, notification_id, attempt_number, attempted_at, status
- FOREIGN KEY: notification_id references notifications(notification_id) ON DELETE CASCADE
- CHECK: status IN ('Success', 'Failed')

#### email_templates Table

**Purpose**: Stores email template aggregates

**Columns**:
- `template_id` (UUID, PRIMARY KEY): Unique template identifier
- `template_type` (VARCHAR, NOT NULL, UNIQUE): Type of template
- `name` (VARCHAR, NOT NULL): Template name
- `subject` (VARCHAR, NOT NULL): Email subject (max 100 chars)
- `body` (TEXT, NOT NULL): Email body (HTML, max 10000 chars)
- `variables` (JSON, NOT NULL): Template variables definition
- `version` (VARCHAR, NOT NULL): Current version
- `created_at` (TIMESTAMP, NOT NULL): Creation timestamp
- `updated_at` (TIMESTAMP, NOT NULL): Last update timestamp
- `created_by` (UUID, NOT NULL): Creator user ID
- `updated_by` (UUID): Last updater user ID

**Indexes**:
- PRIMARY KEY: template_id
- UNIQUE INDEX: template_type (for querying by type)
- INDEX: created_at (for date range queries)

**Constraints**:
- NOT NULL: template_id, template_type, name, subject, body, variables, version, created_at, updated_at, created_by
- UNIQUE: template_type
- CHECK: LENGTH(subject) <= 100
- CHECK: LENGTH(body) <= 10000

#### template_versions Table

**Purpose**: Stores template version history

**Columns**:
- `version_id` (UUID, PRIMARY KEY): Unique version identifier
- `template_id` (UUID, NOT NULL, FOREIGN KEY): Reference to template
- `version` (VARCHAR, NOT NULL): Version number (semantic versioning)
- `subject` (VARCHAR, NOT NULL): Subject for this version
- `body` (TEXT, NOT NULL): Body for this version
- `created_at` (TIMESTAMP, NOT NULL): Version creation timestamp
- `created_by` (UUID, NOT NULL): Creator user ID

**Indexes**:
- PRIMARY KEY: version_id
- FOREIGN KEY: template_id (references email_templates table)
- INDEX: template_id (for querying versions by template)
- INDEX: created_at (for date range queries)

**Constraints**:
- NOT NULL: version_id, template_id, version, subject, body, created_at, created_by
- FOREIGN KEY: template_id references email_templates(template_id) ON DELETE CASCADE

### Data Types & Validation

**Email Address**:
- Type: VARCHAR(255)
- Validation: RFC 5322 format
- Normalization: Lowercase

**Notification Type**:
- Type: VARCHAR(50)
- Values: RequestCreated, RequestSubmitted, RequestApprovedByHeadOfOffice, RequestEndorsed, RequestFinallyApproved, RequestDeclined, RequestReturned, RequestImplemented

**Notification Status**:
- Type: VARCHAR(20)
- Values: Pending, Sent, Failed, Retrying, Abandoned, Archived

**Template Variables**:
- Type: JSON
- Format: Array of objects with name, description, required fields
- Example: [{"name": "requestorName", "description": "Name of requestor", "required": true}]

---

## Event Handling Design

### Consumed Events from Request Management Service

#### RequestCreated Event

**Trigger**: New access request is created

**Event Payload**:
- requestId
- requestorId
- requestorName
- requestorEmail
- accessType
- systemName
- justification
- officeId
- createdAt

**Handler**: RequestCreatedEventHandler

**Processing**:
1. Validate event
2. Check idempotency (notification not already created)
3. Create notification for requestor
4. Send notification
5. Publish NotificationSent or NotificationFailed event
6. Log metrics

#### RequestSubmitted Event

**Trigger**: Request is submitted for approval

**Event Payload**:
- requestId
- requestorId
- requestorName
- requestorEmail
- headOfOfficeId
- headOfOfficeName
- headOfOfficeEmail
- accessType
- systemName
- submittedAt

**Handler**: RequestSubmittedEventHandler

**Processing**:
1. Validate event
2. Check idempotency
3. Create notification for Head of Office
4. Send notification
5. Publish event
6. Log metrics

#### RequestApprovedByHeadOfOffice Event

**Trigger**: Head of Office approves request

**Event Payload**:
- requestId
- requestorId
- requestorEmail
- approverId
- approverName
- comments
- reviewerId
- reviewerName
- reviewerEmail
- approvedAt

**Handler**: RequestApprovedByHeadOfOfficeEventHandler

**Processing**:
1. Validate event
2. Check idempotency
3. Create notifications for requestor and reviewer
4. Send notifications
5. Publish events
6. Log metrics

#### RequestEndorsed Event

**Trigger**: SMD/RDC Reviewer endorses request

**Event Payload**:
- requestId
- reviewerId
- reviewerName
- comments
- smdHeadId
- smdHeadName
- smdHeadEmail
- endorsedAt

**Handler**: RequestEndorsedEventHandler

**Processing**:
1. Validate event
2. Check idempotency
3. Create notification for SMD/RDC Head
4. Send notification
5. Publish event
6. Log metrics

#### RequestFinallyApproved Event

**Trigger**: SMD/RDC Head gives final approval

**Event Payload**:
- requestId
- requestorId
- approverId
- approverName
- comments
- administratorId
- administratorName
- administratorEmail
- accessType
- systemName
- approvedAt

**Handler**: RequestFinallyApprovedEventHandler

**Processing**:
1. Validate event
2. Check idempotency
3. Create notification for administrator
4. Send notification
5. Publish event
6. Log metrics

#### RequestDeclined Event

**Trigger**: Request is declined at any stage

**Event Payload**:
- requestId
- requestorId
- requestorEmail
- declinerId
- declinerName
- declinerRole
- reason
- declinedAt

**Handler**: RequestDeclinedEventHandler

**Processing**:
1. Validate event
2. Check idempotency
3. Create notification for requestor
4. Send notification
5. Archive related notifications
6. Publish event
7. Log metrics

#### RequestReturned Event

**Trigger**: SMD/RDC Head returns request to reviewer

**Event Payload**:
- requestId
- returnerId
- returnerName
- reason
- reviewerId
- reviewerEmail
- returnedAt

**Handler**: RequestReturnedEventHandler

**Processing**:
1. Validate event
2. Check idempotency
3. Create notification for reviewer
4. Send notification
5. Publish event
6. Log metrics

#### RequestImplemented Event

**Trigger**: Administrator marks request as implemented

**Event Payload**:
- requestId
- requestorId
- requestorEmail
- implementerId
- implementerName
- notes
- implementedAt

**Handler**: RequestImplementedEventHandler

**Processing**:
1. Validate event
2. Check idempotency
3. Create notification for requestor
4. Send notification
5. Archive related notifications
6. Publish event
7. Log metrics

### Published Events to Administration Service

#### NotificationSent Event

**Trigger**: When notification is successfully sent

**Event Payload**:
- notificationId
- notificationType
- recipientEmail
- sentAt

**Subscribers**: AdministrationService (audit log)

#### NotificationFailed Event

**Trigger**: When notification delivery fails

**Event Payload**:
- notificationId
- notificationType
- recipientEmail
- reason
- failedAt

**Subscribers**: AdministrationService (audit log, alert)

#### NotificationRetried Event

**Trigger**: When notification retry is attempted

**Event Payload**:
- notificationId
- attemptNumber
- nextRetryAt

**Subscribers**: AdministrationService (audit log)

#### NotificationAbandoned Event

**Trigger**: When notification exceeds max retries

**Event Payload**:
- notificationId
- notificationType
- recipientEmail
- totalAttempts

**Subscribers**: AdministrationService (audit log, alert)

#### TemplateUpdated Event

**Trigger**: When email template is reset to default

**Event Payload**:
- templateId
- templateType
- version
- updatedAt
- updatedBy

**Subscribers**: AdministrationService (audit log)

### Idempotency Strategy

**Idempotency Key**:
- For notifications: `{requestId}_{notificationType}_{recipientId}`
- Check if notification already exists before creating

**Idempotency Implementation**:
1. Generate idempotency key from event
2. Query database for existing notification with same key
3. If exists, skip processing (already processed)
4. If not exists, process event normally
5. Store idempotency key with notification

**Dead Letter Queue**:
- Failed events sent to dead letter queue
- Manual review and reprocessing
- Alerts for dead letter queue messages


---

## Error Handling & Validation

### Validation Rules

#### Notification Validation

- Recipient email must be valid (RFC 5322 format)
- Notification type must be one of defined types
- Request ID must exist in Request Management Service
- Recipient ID must exist in Administration Service
- Template variables must match template requirements
- Retry count must not exceed maximum (3)

#### Email Template Validation

- Template type must be unique
- Subject must not exceed 100 characters
- Body must not exceed 10000 characters
- All required variables must be defined
- Template must have at least one version
- Variables must have name and required flag

#### Email Address Validation

- Must follow RFC 5322 format
- Must not be empty
- Must be normalized to lowercase
- Must not exceed 255 characters

### Error Handling Strategy

#### SMTP Errors

**Transient Errors** (Retry):
- Connection timeout
- Read timeout
- Temporary service unavailable (4xx codes)
- Retry with exponential backoff

**Permanent Errors** (Fail):
- Invalid email address (5xx codes)
- Authentication failure
- Relay denied
- Fail immediately, mark as abandoned

#### Application Errors

**Validation Errors**:
- Invalid input data
- Missing required fields
- Invalid email format
- Return 400 Bad Request

**Authorization Errors**:
- Insufficient permissions
- User not authenticated
- Return 401 Unauthorized or 403 Forbidden

**Not Found Errors**:
- Notification not found
- Template not found
- Return 404 Not Found

**Conflict Errors**:
- Duplicate notification
- Template type already exists
- Return 409 Conflict

**Rate Limit Errors**:
- Email sending rate exceeded
- Return 429 Too Many Requests

**Internal Errors**:
- Database errors
- Kafka errors
- SMTP connection errors
- Return 500 Internal Server Error

### Exception Hierarchy

```
Exception
├── NotificationException
│   ├── NotificationNotFoundException
│   ├── InvalidNotificationStateException
│   ├── NotificationRetryExceededException
│   └── NotificationAlreadyExistsException
├── TemplateException
│   ├── TemplateNotFoundException
│   ├── InvalidTemplateException
│   ├── TemplateVariableException
│   └── TemplateRenderException
├── EmailException
│   ├── InvalidEmailAddressException
│   ├── EmailSendingException
│   ├── SmtpException
│   └── RateLimitExceededException
└── EventException
    ├── EventProcessingException
    ├── EventPublishingException
    └── IdempotencyException
```

---

## Security Design

### Authentication

**JWT Token-Based Authentication**:
- All API endpoints require JWT token
- Token obtained via Administration Service login
- Token included in `Authorization: Bearer <token>` header
- Token validation on each request
- Token refresh mechanism

### Authorization

**Role-Based Access Control (RBAC)**:
- Roles: Employee, Head of Office, Reviewer, Head, System Admin, DB Admin
- Endpoint-level authorization checks
- Method-level security annotations
- Resource-level authorization (users can only see own notifications)

**Authorization Rules**:
- Employees: Can view own notifications
- Administrators: Can view all notifications, manage templates, view metrics
- All authenticated users: Can query notifications and templates

### Data Protection

**Email Address Protection**:
- Email addresses encrypted at rest
- Secure transmission via HTTPS
- Audit logging of email access

**Sensitive Data**:
- Email body content not logged
- Error messages sanitized (no sensitive data in error responses)
- Audit trail of all notification operations

### Audit Logging

**Audit Trail**:
- All notification operations logged
- All template operations logged
- User ID and timestamp recorded
- Changes tracked for compliance

---

## Performance & Scalability

### Caching Strategy

**Email Template Caching**:
- Cache templates in Redis with 1-hour TTL
- Cache key: `template:{templateType}`
- Invalidate on template reset
- Cache hit rate monitoring

**Cache Invalidation**:
- Invalidate on template update
- Invalidate on template reset
- Automatic expiration after TTL
- Manual invalidation on demand

### Database Optimization

**Indexing Strategy**:
- Index on status (for querying by status)
- Index on recipient_id (for querying by recipient)
- Index on created_at (for date range queries)
- Index on next_retry_at (for retry queries)
- Index on request_id (for request-based queries)

**Query Optimization**:
- Use pagination for large result sets
- Use specifications for complex queries
- Lazy load notification logs
- Batch queries where possible

### Rate Limiting

**Email Sending Rate Limit**:
- Maximum 100 emails per second
- Implement token bucket algorithm
- Queue excess emails for later sending
- Monitor rate limit compliance

**API Rate Limiting**:
- Rate limit per user/IP
- Implement sliding window algorithm
- Return 429 Too Many Requests when exceeded

### Batch Processing

**Retry Batch Processing**:
- Process failed notifications in batches
- Configurable batch size (e.g., 100 notifications)
- Process batches at scheduled intervals
- Monitor batch processing performance

**Email Sending Batches**:
- Send emails in batches to SMTP server
- Respect rate limit (100 emails/second)
- Handle partial batch failures

### Horizontal Scaling

**Stateless Services**:
- No local state in service instances
- All state in database or cache
- Can scale horizontally

**Load Balancing**:
- Load balance across multiple instances
- Session affinity not required
- Health checks for instance availability

**Distributed Processing**:
- Kafka for event distribution
- Multiple consumer instances
- Partition events by request ID for ordering

### Performance Monitoring

**Metrics**:
- Notification sent count
- Notification failed count
- Notification retry count
- Email sending rate
- Template cache hit rate
- Event processing latency
- Database query performance

**Monitoring Tools**:
- Prometheus for metrics collection
- Grafana for visualization
- ELK stack for log aggregation
- Alerts for performance degradation

---

## Testing Strategy

### Unit Testing

**Domain Layer Testing**:
- Test aggregate behavior
- Test value object validation
- Test domain service logic
- Test policy validation
- Test factory creation

**Application Layer Testing**:
- Test application service methods
- Test event handling
- Test transaction management
- Test error handling

**Infrastructure Layer Testing**:
- Test repository queries
- Test event publishing/consumption
- Test email sending
- Test caching

### Integration Testing

**Service Integration**:
- Test event consumption from Request Management Service
- Test event publishing to Administration Service
- Test database persistence
- Test Kafka integration
- Test SMTP integration
- Test Redis caching

**End-to-End Testing**:
- Test complete notification flow
- Test retry logic
- Test archival process
- Test metrics collection

### Property-Based Testing

**Property Tests**:
- Notification state transitions are valid
- Retry logic respects max retries
- Template rendering produces valid HTML
- Email addresses are normalized correctly
- Idempotency prevents duplicate notifications
- Metrics are accurate

### Test Data Generation

**Notification Test Data**:
- Generate random notification types
- Generate random email addresses
- Generate random request IDs
- Generate random retry counts

**Template Test Data**:
- Generate random template types
- Generate random template content
- Generate random variables

**Email Test Data**:
- Generate random email addresses
- Generate random email subjects
- Generate random email bodies

### Mock Email Provider

**Mock SMTP Server**:
- In-memory SMTP server for testing
- Capture sent emails
- Simulate SMTP errors
- Simulate rate limiting

---

## Deployment & Operations

### Deployment Architecture

**Docker Container**:
- Single Docker image for Notification Service
- Containerized with Spring Boot
- Health checks configured
- Resource limits configured

**Kubernetes Orchestration**:
- Kubernetes deployment manifest
- Service definition for load balancing
- ConfigMap for configuration
- Secrets for sensitive data
- Persistent volumes for database

**Configuration Management**:
- Environment variables for configuration
- ConfigMap for non-sensitive configuration
- Secrets for sensitive data (SMTP credentials, etc.)
- Spring profiles for environment-specific configuration

### Database Migration

**Schema Management**:
- Flyway for database migrations
- Version control for schema changes
- Automated migration on deployment
- Rollback capability

### Monitoring & Alerting

**Health Checks**:
- Liveness probe: Service is running
- Readiness probe: Service is ready to handle requests
- Startup probe: Service has started

**Metrics Collection**:
- Prometheus metrics endpoint
- Micrometer for metrics collection
- Custom metrics for business logic

**Alerting**:
- Alert on high failure rate
- Alert on rate limit exceeded
- Alert on abandoned notifications
- Alert on SMTP connection failures
- Alert on database connection failures

### Logging

**Structured Logging**:
- SLF4J with Logback
- JSON format for log aggregation
- Correlation IDs for request tracing
- Log levels: DEBUG, INFO, WARN, ERROR

**Log Aggregation**:
- ELK stack (Elasticsearch, Logstash, Kibana)
- Centralized log storage
- Log search and analysis
- Log retention policy

### Graceful Shutdown

**Shutdown Sequence**:
1. Stop accepting new requests
2. Wait for in-flight requests to complete
3. Stop consuming events from Kafka
4. Close database connections
5. Close cache connections
6. Exit

**Shutdown Timeout**:
- Configurable shutdown timeout (e.g., 30 seconds)
- Force shutdown after timeout

---

## Architecture Diagrams

### High-Level System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Access Request Processing System                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  Request Management Service                                              │
│  ├─ Publishes: RequestCreated, RequestSubmitted, etc.                   │
│  └─ Topic: request.events                                               │
│                                                                           │
│  ↓ (Kafka)                                                               │
│                                                                           │
│  Notification Service                                                    │
│  ├─ Consumes: request.events                                            │
│  ├─ Publishes: NotificationSent, NotificationFailed, etc.               │
│  ├─ Topic: notification.events                                          │
│  └─ Components:                                                          │
│     ├─ REST API Layer                                                   │
│     ├─ Application Layer                                                │
│     ├─ Domain Layer                                                     │
│     └─ Infrastructure Layer                                             │
│                                                                           │
│  ↓ (Kafka)                                                               │
│                                                                           │
│  Administration Service                                                  │
│  └─ Consumes: notification.events                                       │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Notification Lifecycle State Machine

```
┌─────────┐
│ Pending │ (Notification created, waiting to send)
└────┬────┘
     │ send()
     ↓
┌─────────┐
│  Sent   │ (Email successfully delivered)
└─────────┘

     OR

┌─────────┐
│ Failed  │ (Email delivery failed)
└────┬────┘
     │ retry()
     ↓
┌──────────┐
│ Retrying │ (Retry attempt in progress)
└────┬─────┘
     │
     ├─ success ──→ ┌─────────┐
     │              │  Sent   │
     │              └─────────┘
     │
     └─ max retries exceeded ──→ ┌──────────┐
                                 │ Abandoned│
                                 └──────────┘

     OR

┌──────────┐
│ Archived │ (Request completed or declined)
└──────────┘
```

### Retry Logic Flow

```
Failed Notification
    ↓
Check if max retries exceeded (3)
    ├─ Yes → Abandon notification
    │         Publish NotificationAbandoned event
    │         Alert administrator
    │
    └─ No → Calculate next retry time
            Retry delay = 5s * (2 ^ retryCount)
            Attempt 1: 5s
            Attempt 2: 10s
            Attempt 3: 20s
            ↓
            Schedule retry
            Update status to Retrying
            ↓
            At scheduled time:
            Retry sending
            ├─ Success → Update status to Sent
            │            Publish NotificationSent event
            │
            └─ Failure → Update status to Failed
                         Publish NotificationFailed event
                         Increment retry count
```

### Email Template Caching

```
Request for Template
    ↓
Check Redis Cache
    ├─ Cache Hit → Return cached template
    │
    └─ Cache Miss → Query database
                    Cache in Redis (TTL: 1 hour)
                    Return template
```

### Batch Retry Processing

```
Scheduled Retry Job
    ↓
Query failed notifications due for retry
    ↓
Process in batches (e.g., 100 notifications)
    ├─ Batch 1: Notifications 1-100
    ├─ Batch 2: Notifications 101-200
    └─ Batch N: Remaining notifications
    ↓
For each notification in batch:
    ├─ Check if max retries exceeded
    ├─ Calculate next retry time
    ├─ Send email
    ├─ Update status
    └─ Publish event
    ↓
Log batch processing metrics
```

---

## Bounded Context Interactions

### Outbound Events

The Notification Service publishes events that are consumed by:
- **Administration Service**: Receives NotificationSent, NotificationFailed, NotificationRetried, NotificationAbandoned, TemplateUpdated events for audit logging and alerts

### Inbound Events

The Notification Service consumes events from:
- **Request Management Service**: All request status change events to trigger notifications

### REST API Integration

**Calls to Administration Service**:
- Get user information (email, name, office)
- Get office hierarchy
- Verify user roles and permissions

**Calls to Request Management Service**:
- Get request details
- Get access type information

---

## Summary

The Notification Service logical design provides a comprehensive, event-driven architecture for managing email notifications throughout the access request lifecycle. The service is designed to be reactive, listening to events from the Request Management Service and sending appropriate notifications to stakeholders. The use of aggregates, value objects, domain services, and specifications ensures clear separation of concerns and maintainability. The factory patterns ensure consistent creation of domain objects, while policies encapsulate business rules and validation logic. The retry mechanism with exponential backoff ensures reliable notification delivery with a maximum of 3 retries. Email template caching in Redis provides performance optimization, while batch processing of failed notifications ensures efficient resource utilization. The service is designed to scale horizontally with stateless instances and can handle up to 100 emails per second with proper rate limiting and monitoring.
