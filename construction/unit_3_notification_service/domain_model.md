# Unit 3: Notification Service - Domain Model

## Overview

The Notification Service domain model manages all email notifications throughout the access request lifecycle. This is an event-driven service that listens to events from other services and sends appropriate email notifications to relevant stakeholders. The service also manages email templates and tracks notification delivery status.

---

## Aggregates

### 1. Notification Aggregate Root

**Purpose**: Manages a single notification with its delivery status and retry logic.

**Aggregate Boundary**: Encompasses all data and behavior related to a single notification.

**Entities within Aggregate**:
- **Notification** (Aggregate Root)
  - Unique identifier: NotificationId
  - Core attributes: notificationType, recipientId, requestId, status, createdAt, sentAt
  - Relationships: references to recipient, request, template
  - Behavior: manages notification lifecycle, retry logic

- **NotificationLog** (Entity)
  - Tracks notification delivery attempts
  - Attributes: attemptNumber, attemptedAt, status (Success, Failed), errorMessage
  - Behavior: append-only, immutable

**Value Objects within Aggregate**:
- **NotificationId**: Unique identifier for the notification
- **NotificationType**: Type of notification
  - Values: RequestCreated, RequestSubmitted, RequestApproved, RequestDeclined, RequestImplemented, etc.
  - Behavior: maps to email template

- **NotificationStatus**: Enumeration of notification states
  - Values: Pending, Sent, Failed, Retrying, Abandoned
  - Behavior: validates state transitions

- **EmailAddress**: Email address with validation
  - Attributes: address
  - Behavior: validates email format, immutable

- **RetryStrategy**: Encapsulates retry logic
  - Attributes: maxRetries, retryDelayMs, backoffMultiplier
  - Behavior: determines retry behavior

- **NotificationPayload**: Encapsulates notification data
  - Attributes: requestId, recipientId, templateVariables (map)
  - Behavior: immutable

**Aggregate Invariants**:
- A notification must have a valid recipient email
- A notification must have a valid notification type
- A notification must have a valid template
- A notification cannot be sent without template variables
- A notification cannot exceed maximum retry attempts
- A notification must have at least one delivery attempt

**Aggregate Lifecycle**:
1. Created when event is received (status: Pending)
2. Sent when email is successfully delivered (status: Sent)
3. Failed if delivery fails (status: Failed)
4. Retrying if retry is attempted (status: Retrying)
5. Abandoned if max retries exceeded (status: Abandoned)

---

### 2. EmailTemplate Aggregate Root

**Purpose**: Manages email templates with versioning and variable support.

**Aggregate Boundary**: Encompasses all data and behavior related to a single email template.

**Entities within Aggregate**:
- **EmailTemplate** (Aggregate Root)
  - Unique identifier: TemplateId
  - Core attributes: templateType, subject, body, variables, version, createdAt, updatedAt
  - Relationships: references to template type
  - Behavior: manages template versioning, variable rendering

- **TemplateVersion** (Entity)
  - Tracks template versions
  - Attributes: version, subject, body, createdAt, createdBy
  - Behavior: immutable

**Value Objects within Aggregate**:
- **TemplateId**: Unique identifier for the template
- **TemplateType**: Type of template
  - Values: RequestCreated, RequestSubmitted, RequestApproved, RequestDeclined, RequestImplemented, RequestReturned, etc.
  - Behavior: immutable

- **TemplateVariable**: Encapsulates template variable
  - Attributes: name, description, required (boolean)
  - Behavior: immutable

- **TemplateContent**: Encapsulates template content
  - Attributes: subject, body (HTML)
  - Behavior: validates content, immutable

- **TemplateVersion**: Version information
  - Attributes: version, createdAt, createdBy
  - Behavior: immutable

---

## Entities (Outside Aggregates)

### NotificationSearchResult

**Purpose**: Represents a notification in search results (read model entity).

**Attributes**:
- notificationId, notificationType, recipientEmail, status, createdAt, sentAt

**Behavior**: Immutable, used for query results only

---

## Value Objects

### Core Value Objects

**NotificationId**
- Unique identifier for notifications
- Format: UUID or sequential ID
- Behavior: immutable, comparable

**NotificationType**
- Type of notification
- Values: RequestCreated, RequestSubmitted, RequestApprovedByHeadOfOffice, RequestEndorsed, RequestFinallyApproved, RequestDeclined, RequestReturned, RequestImplemented
- Behavior: maps to email template, immutable

**NotificationStatus**
- Enumeration of notification states
- Values: Pending, Sent, Failed, Retrying, Abandoned
- Behavior: validates state transitions

**EmailAddress**
- Email address with validation
- Attributes: address
- Behavior: validates email format (RFC 5322), normalizes to lowercase, immutable

**RetryStrategy**
- Encapsulates retry logic
- Attributes: maxRetries (default: 3), retryDelayMs (default: 5000), backoffMultiplier (default: 2)
- Behavior: calculates next retry time, immutable

**NotificationPayload**
- Encapsulates notification data
- Attributes: requestId, recipientId, templateVariables (map of key-value pairs)
- Behavior: immutable, validates required variables

**TemplateId**
- Unique identifier for templates
- Format: UUID or sequential ID
- Behavior: immutable, comparable

**TemplateType**
- Type of template
- Values: RequestCreated, RequestSubmitted, RequestApprovedByHeadOfOffice, RequestEndorsed, RequestFinallyApproved, RequestDeclined, RequestReturned, RequestImplemented
- Behavior: immutable

**TemplateVariable**
- Encapsulates template variable definition
- Attributes: name, description, required (boolean)
- Behavior: immutable

**TemplateContent**
- Encapsulates template content
- Attributes: subject, body (HTML)
- Behavior: validates content, immutable

**TemplateVersion**
- Version information
- Attributes: version (semantic versioning), createdAt, createdBy
- Behavior: immutable

**NotificationDeliveryResult**
- Encapsulates delivery result
- Attributes: success (boolean), sentAt, errorMessage (if failed)
- Behavior: immutable

---

## Domain Events

### Published Events

**NotificationSent**
- Trigger: When notification is successfully sent
- Payload: notificationId, notificationType, recipientEmail, sentAt
- Subscribers: AdministrationService (audit log)

**NotificationFailed**
- Trigger: When notification delivery fails
- Payload: notificationId, notificationType, recipientEmail, reason, failedAt
- Subscribers: AdministrationService (audit log, alert)

**NotificationRetried**
- Trigger: When notification retry is attempted
- Payload: notificationId, attemptNumber, nextRetryAt
- Subscribers: AdministrationService (audit log)

**NotificationAbandoned**
- Trigger: When notification exceeds max retries
- Payload: notificationId, notificationType, recipientEmail, totalAttempts
- Subscribers: AdministrationService (audit log, alert)

**TemplateUpdated**
- Trigger: When email template is updated
- Payload: templateId, templateType, version, updatedAt, updatedBy
- Subscribers: AdministrationService (audit log)

### Consumed Events

**RequestCreated** (from Request Management Service)
- Used to: Send confirmation notification to requestor
- Action: Create Notification aggregate, send email

**RequestSubmitted** (from Request Management Service)
- Used to: Send notification to Head of Office
- Action: Create Notification aggregate, send email

**RequestApprovedByHeadOfOffice** (from Request Management Service)
- Used to: Send notifications to requestor and reviewer
- Action: Create Notification aggregates, send emails

**RequestEndorsed** (from Request Management Service)
- Used to: Send notification to SMD/RDC Head
- Action: Create Notification aggregate, send email

**RequestFinallyApproved** (from Request Management Service)
- Used to: Send notification to administrator
- Action: Create Notification aggregate, send email

**RequestDeclined** (from Request Management Service)
- Used to: Send notification to requestor
- Action: Create Notification aggregate, send email

**RequestReturned** (from Request Management Service)
- Used to: Send notification to reviewer
- Action: Create Notification aggregate, send email

**RequestImplemented** (from Request Management Service)
- Used to: Send notification to requestor
- Action: Create Notification aggregate, send email

---

## Domain Services

### NotificationService

**Purpose**: Sends notifications based on events.

**Responsibilities**:
- Listen to domain events
- Create notifications from events
- Determine recipients
- Send emails
- Handle delivery failures

**Methods**:
- handleRequestCreated(event): void
- handleRequestSubmitted(event): void
- handleRequestApprovedByHeadOfOffice(event): void
- handleRequestEndorsed(event): void
- handleRequestFinallyApproved(event): void
- handleRequestDeclined(event): void
- handleRequestReturned(event): void
- handleRequestImplemented(event): void
- sendNotification(notification): NotificationDeliveryResult

**Dependencies**: NotificationRepository, EmailTemplateRepository, EmailService

---

### EmailTemplateService

**Purpose**: Manages and renders email templates.

**Responsibilities**:
- Retrieve templates by type
- Render templates with variables
- Manage template versions
- Support template preview

**Methods**:
- getTemplate(templateType): EmailTemplate
- renderTemplate(templateType, variables): RenderedEmail
- updateTemplate(templateId, content): void
- previewTemplate(templateType, variables): RenderedEmail
- getTemplateVersion(templateId, version): EmailTemplate
- revertToDefaultTemplate(templateType): void

**Dependencies**: EmailTemplateRepository

---

### NotificationRetryService

**Purpose**: Handles failed notification retries.

**Responsibilities**:
- Identify failed notifications
- Determine retry eligibility
- Schedule retries
- Update retry status

**Methods**:
- retryFailedNotifications(): void
- shouldRetry(notification): boolean
- calculateNextRetryTime(notification): DateTime
- retryNotification(notificationId): NotificationDeliveryResult
- abandonNotification(notificationId): void

**Dependencies**: NotificationRepository, NotificationService

---

### EmailService

**Purpose**: Sends emails via SMTP or email provider.

**Responsibilities**:
- Send emails
- Handle SMTP errors
- Track delivery status
- Manage email provider configuration

**Methods**:
- sendEmail(to, subject, body): EmailDeliveryResult
- sendBulkEmails(recipients, subject, body): List<EmailDeliveryResult>
- testConnection(): boolean

**Dependencies**: Email provider (SMTP, SendGrid, etc.)

---

## Repositories

### NotificationRepository

**Purpose**: Persists and retrieves Notification aggregates.

**Responsibilities**:
- Save new notifications
- Update existing notifications
- Query notifications by various criteria
- Maintain notification logs

**Query Methods**:
- findById(notificationId): Notification
- findByRecipient(recipientId): List<Notification>
- findByStatus(status): List<Notification>
- findFailedNotifications(): List<Notification>
- findPendingNotifications(): List<Notification>
- findByDateRange(startDate, endDate): List<Notification>
- findByNotificationType(notificationType): List<Notification>
- search(criteria): List<Notification>

**Persistence Methods**:
- save(notification): void
- update(notification): void
- delete(notificationId): void

---

### EmailTemplateRepository

**Purpose**: Persists and retrieves EmailTemplate aggregates.

**Responsibilities**:
- Save new templates
- Update existing templates
- Query templates by type
- Maintain template versions

**Query Methods**:
- findById(templateId): EmailTemplate
- findByType(templateType): EmailTemplate
- findAll(): List<EmailTemplate>
- findByVersion(templateId, version): EmailTemplate
- getDefaultTemplate(templateType): EmailTemplate

**Persistence Methods**:
- save(template): void
- update(template): void
- delete(templateId): void
- saveVersion(templateId, version): void

---

## Specifications (Query Objects)

### FailedNotificationsSpecification

**Purpose**: Query failed notifications for retry.

**Criteria**: status (Failed), attemptCount < maxRetries

**Returns**: List<Notification>

---

### NotificationsByTypeSpecification

**Purpose**: Query notifications by type.

**Criteria**: notificationType

**Returns**: List<Notification>

---

### PendingNotificationsSpecification

**Purpose**: Query pending notifications.

**Criteria**: status (Pending)

**Returns**: List<Notification>

---

### NotificationsByRecipientSpecification

**Purpose**: Query notifications for a specific recipient.

**Criteria**: recipientId

**Returns**: List<Notification>

---

### NotificationsByDateRangeSpecification

**Purpose**: Query notifications within a date range.

**Criteria**: startDate, endDate

**Returns**: List<Notification>

---

### OverdueNotificationsSpecification

**Purpose**: Query notifications that are overdue for retry.

**Criteria**: status (Retrying), nextRetryTime < now

**Returns**: List<Notification>

---

## Policies

### NotificationRetryPolicy

**Purpose**: Defines retry logic for failed notifications.

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
- shouldRetry(notification): boolean
- calculateNextRetryTime(notification): DateTime
- isMaxRetriesExceeded(notification): boolean

---

### NotificationDeliveryPolicy

**Purpose**: Determines recipients for each event.

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
- getRecipientsForEvent(eventType): List<UserId>
- isValidRecipient(userId, eventType): boolean

---

### TemplateValidationPolicy

**Purpose**: Validates email templates.

**Rules**:
- Template must have subject
- Template must have body
- Subject must not exceed 100 characters
- Body must not exceed 10000 characters
- All template variables must be defined
- Template must have at least one version
- Template type must be unique

**Validation Methods**:
- isValidTemplate(template): boolean
- validateTemplateContent(content): ValidationResult
- validateTemplateVariables(template): ValidationResult

---

## Factory Patterns

### NotificationFactory

**Purpose**: Creates Notification aggregates from events.

**Responsibilities**:
- Create notifications from domain events
- Determine recipients
- Set up retry strategy
- Initialize notification in Pending status

**Creation Methods**:
- createFromRequestCreated(event): Notification
- createFromRequestSubmitted(event): Notification
- createFromRequestApprovedByHeadOfOffice(event): List<Notification>
- createFromRequestEndorsed(event): Notification
- createFromRequestFinallyApproved(event): Notification
- createFromRequestDeclined(event): Notification
- createFromRequestReturned(event): Notification
- createFromRequestImplemented(event): Notification

---

### EmailTemplateFactory

**Purpose**: Creates EmailTemplate aggregates with validation.

**Responsibilities**:
- Create templates with validation
- Generate unique TemplateId
- Initialize template with default version
- Set up template variables

**Creation Method**:
- createTemplate(templateType, subject, body, variables): EmailTemplate
  - Validates template content
  - Validates variables
  - Generates TemplateId
  - Creates initial version
  - Returns new EmailTemplate aggregate

---

### NotificationLogFactory

**Purpose**: Creates NotificationLog entries.

**Responsibilities**:
- Create immutable log entries
- Capture delivery attempt information
- Timestamp all entries

**Creation Method**:
- createLogEntry(notificationId, status, errorMessage): NotificationLog
  - Captures current timestamp
  - Creates immutable entry
  - Returns NotificationLog entity

---

## Bounded Context Interactions

### Outbound Events

The Notification Service publishes events that are consumed by:
- **Administration Service**: Receives NotificationSent, NotificationFailed, NotificationRetried, NotificationAbandoned, TemplateUpdated events for audit logging and alerts

### Inbound Events

The Notification Service consumes events from:
- **Request Management Service**: All request status change events to trigger notifications

---

## Event-Driven Architecture Diagram

```
Request Management Service
    │
    ├─ RequestCreated
    │   └→ NotificationService
    │       └→ Create Notification (Requestor)
    │           └→ Send Email
    │
    ├─ RequestSubmitted
    │   └→ NotificationService
    │       └→ Create Notification (Head of Office)
    │           └→ Send Email
    │
    ├─ RequestApprovedByHeadOfOffice
    │   └→ NotificationService
    │       ├→ Create Notification (Requestor)
    │       │   └→ Send Email
    │       └→ Create Notification (Reviewer)
    │           └→ Send Email
    │
    ├─ RequestEndorsed
    │   └→ NotificationService
    │       └→ Create Notification (SMD/RDC Head)
    │           └→ Send Email
    │
    ├─ RequestFinallyApproved
    │   └→ NotificationService
    │       └→ Create Notification (Administrator)
    │           └→ Send Email
    │
    ├─ RequestDeclined
    │   └→ NotificationService
    │       └→ Create Notification (Requestor)
    │           └→ Send Email
    │
    ├─ RequestReturned
    │   └→ NotificationService
    │       └→ Create Notification (Reviewer)
    │           └→ Send Email
    │
    └─ RequestImplemented
        └→ NotificationService
            └→ Create Notification (Requestor)
                └→ Send Email
```

---

## Notification Lifecycle Diagram

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
```

---

## Aggregate Relationships Diagram

```
┌─────────────────────────────────────────────────────────┐
│               Notification Aggregate                     │
├─────────────────────────────────────────────────────────┤
│ NotificationId (Value Object)                           │
│ NotificationType (Value Object)                         │
│ NotificationStatus (Value Object)                       │
│ EmailAddress (Value Object)                             │
│ RetryStrategy (Value Object)                            │
│ NotificationPayload (Value Object)                      │
│                                                          │
│ └─ NotificationLog (Entity) [1..*]                      │
│    └─ NotificationDeliveryResult (Value Object)         │
│                                                          │
│ References:                                             │
│ ├─ requestId (from Request Management Service)          │
│ ├─ recipientId (UserId from Administration Service)     │
│ └─ templateId (from EmailTemplate Aggregate)            │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│              EmailTemplate Aggregate                     │
├─────────────────────────────────────────────────────────┤
│ TemplateId (Value Object)                               │
│ TemplateType (Value Object)                             │
│ TemplateContent (Value Object)                          │
│                                                          │
│ └─ TemplateVersion (Entity) [1..*]                      │
│    └─ TemplateVersion (Value Object)                    │
│                                                          │
│ ├─ TemplateVariable (Value Object) [0..*]               │
│ └─ TemplateContent (Value Object)                       │
└─────────────────────────────────────────────────────────┘
```

---

## Summary

The Notification Service domain model provides a comprehensive, event-driven architecture for managing email notifications throughout the access request lifecycle. The service is designed to be reactive, listening to events from the Request Management Service and sending appropriate notifications to stakeholders. The use of aggregates, value objects, domain services, and specifications ensures clear separation of concerns and maintainability. The factory patterns ensure consistent creation of domain objects, while policies encapsulate business rules and validation logic. The retry mechanism ensures reliable notification delivery with exponential backoff strategy.

