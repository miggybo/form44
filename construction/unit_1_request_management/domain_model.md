# Unit 1: Request Management Service - Domain Model

## Overview

The Request Management Service domain model manages the complete lifecycle of access requests, including creation, submission, multi-level approval workflow orchestration, and implementation tracking. This is the core domain of the system, responsible for coordinating the complex approval workflow and maintaining request state.

---

## Aggregates

### 1. Request Aggregate Root

**Purpose**: Manages the complete lifecycle of an access request from creation through implementation.

**Aggregate Boundary**: The Request aggregate encompasses all data and behavior related to a single access request.

**Entities within Aggregate**:
- **Request** (Aggregate Root)
  - Unique identifier: RequestId
  - Core attributes: requestorId, accessType, systemName, justification, status, createdAt, submittedAt
  - Relationships: references to approvers, reviewers, implementer
  - Behavior: state transitions, approval/decline/endorsement operations

- **RequestApproval** (Entity)
  - Tracks approval at each stage
  - Attributes: approverId, approvalType (HeadOfOffice, Reviewer, Head), timestamp, comments
  - Behavior: immutable once created

- **RequestHistory** (Entity)
  - Audit trail of all state changes
  - Attributes: timestamp, action, actor, previousStatus, newStatus, comments
  - Behavior: append-only, immutable

**Value Objects within Aggregate**:
- **RequestId**: Unique identifier for the request
- **RequestStatus**: Enumeration with state transition rules
  - Possible states: Draft, PendingInitialApproval, PendingReview, PendingFinalApproval, ReturnedToReviewer, Approved, Declined, Implemented
  - Behavior: validates valid state transitions
  
- **AccessType**: Encapsulates access type information
  - Attributes: typeId, name, description
  - Behavior: determines routing rules

- **ApprovalComment**: Encapsulates approval/decline comments
  - Attributes: text, createdBy, createdAt
  - Behavior: immutable

- **RequestJustification**: Encapsulates justification with validation
  - Attributes: text, minLength, maxLength
  - Behavior: validates length and content

- **RequestorInfo**: Value object for requestor information
  - Attributes: requestorId, name, position, office
  - Behavior: immutable

**Aggregate Invariants**:
- A request must have a valid requestor
- A request must have a valid access type
- A request cannot transition to invalid states
- A request cannot be approved without proper justification
- A request cannot be declined without a reason
- A declined request cannot be resubmitted (must create new request)
- A request must have all required approvals before implementation

**Aggregate Lifecycle**:
1. Created in Draft status
2. Submitted to Head of Office (status: PendingInitialApproval)
3. Approved by Head of Office (status: PendingReview) or Declined
4. Reviewed by SMD/RDC Reviewer (status: PendingFinalApproval) or Declined
5. Approved by SMD/RDC Head (status: Approved) or Returned to Reviewer
6. Implemented by Administrator (status: Implemented)

---

### 2. AccessType Aggregate Root

**Purpose**: Manages access type definitions and routing configurations.

**Aggregate Boundary**: Encompasses access type definition and its routing rules.

**Entities within Aggregate**:
- **AccessType** (Aggregate Root)
  - Unique identifier: AccessTypeId
  - Attributes: name, description, createdAt, updatedAt
  - Behavior: manages routing configuration

- **AccessTypeRouting** (Entity)
  - Attributes: administratorRole, isDefault
  - Behavior: determines which administrator handles this access type

**Value Objects within Aggregate**:
- **AccessTypeId**: Unique identifier
- **AccessTypeName**: Name of the access type
- **AccessTypeDescription**: Description of the access type
- **AccessTypeRoutingConfig**: Encapsulates routing configuration
  - Attributes: administratorRoles (list), defaultRole
  - Behavior: validates routing configuration

**Aggregate Invariants**:
- An access type must have a name
- An access type must have at least one routing rule
- An access type must have a default administrator role
- Access type names must be unique

---

## Entities (Outside Aggregates)

### RequestSearchResult

**Purpose**: Represents a request in search results (read model entity).

**Attributes**:
- requestId, requestorName, accessType, systemName, status, submittedAt, currentApprover

**Behavior**: Immutable, used for query results only

---

## Value Objects

### Core Value Objects

**RequestId**
- Unique identifier for requests
- Format: UUID or sequential ID
- Behavior: immutable, comparable

**AccessTypeId**
- Unique identifier for access types
- Format: UUID or sequential ID
- Behavior: immutable, comparable

**RequestStatus**
- Enumeration of valid request states
- Values: Draft, PendingInitialApproval, PendingReview, PendingFinalApproval, ReturnedToReviewer, Approved, Declined, Implemented
- Behavior: validates state transitions using state machine logic

**AccessType**
- Encapsulates access type information
- Attributes: typeId, name, description
- Behavior: immutable, comparable

**ApprovalComment**
- Encapsulates approval/decline comments with metadata
- Attributes: text, createdBy, createdAt
- Behavior: immutable, validates text length

**RequestJustification**
- Encapsulates justification with validation rules
- Attributes: text, minLength (required), maxLength (10MB)
- Behavior: validates length, immutable

**RequestorInfo**
- Encapsulates requestor information
- Attributes: requestorId, name, position, office
- Behavior: immutable, comparable

**AccessTypeRouting**
- Encapsulates routing configuration
- Attributes: administratorRoles (list), defaultRole
- Behavior: validates routing rules, immutable

**ApprovalChain**
- Encapsulates the approval chain for a request
- Attributes: headOfOfficeId, reviewerId, headId, administratorId
- Behavior: tracks who approved at each stage

**RequestTimestamps**
- Encapsulates all timestamps for a request
- Attributes: createdAt, submittedAt, approvedAt, implementedAt
- Behavior: immutable, validates timestamp ordering

---

## Domain Events

### Published Events

**RequestCreated**
- Trigger: When a new request is created
- Payload: requestId, requestorId, accessType, systemName, createdAt
- Subscribers: NotificationService (send confirmation), AdministrationService (audit log)

**RequestSubmitted**
- Trigger: When request is submitted for initial approval
- Payload: requestId, requestorId, headOfOfficeId, submittedAt
- Subscribers: NotificationService (notify Head of Office), AdministrationService (audit log)

**RequestApprovedByHeadOfOffice**
- Trigger: When Head of Office approves request
- Payload: requestId, approverId, comments, reviewerId, approvedAt
- Subscribers: NotificationService (notify requestor and reviewer), AdministrationService (audit log)

**RequestEndorsed**
- Trigger: When SMD/RDC Reviewer endorses request
- Payload: requestId, reviewerId, comments, smdHeadId, endorsedAt
- Subscribers: NotificationService (notify SMD/RDC Head), AdministrationService (audit log)

**RequestFinallyApproved**
- Trigger: When SMD/RDC Head approves request for implementation
- Payload: requestId, approverId, comments, administratorId, accessType, approvedAt
- Subscribers: NotificationService (notify administrator), AdministrationService (audit log)

**RequestDeclined**
- Trigger: When request is declined at any stage
- Payload: requestId, declinerId, reason, requestorId, declinedAt
- Subscribers: NotificationService (notify requestor), AdministrationService (audit log)

**RequestReturned**
- Trigger: When SMD/RDC Head returns request to reviewer
- Payload: requestId, returnerId, reason, reviewerId, returnedAt
- Subscribers: NotificationService (notify reviewer), AdministrationService (audit log)

**RequestImplemented**
- Trigger: When administrator marks request as implemented
- Payload: requestId, implementerId, notes, requestorId, implementedAt
- Subscribers: NotificationService (notify requestor), AdministrationService (audit log)

**AccessTypeAdded**
- Trigger: When new access type is created
- Payload: accessTypeId, name, description, createdAt
- Subscribers: AdministrationService (audit log)

**AccessTypeRoutingConfigured**
- Trigger: When access type routing is configured
- Payload: accessTypeId, administratorRoles, configuredAt
- Subscribers: AdministrationService (audit log)

### Consumed Events

**DocumentUploaded** (from Document Management Service)
- Used to: Update request with document metadata
- Action: Add document reference to request

**DocumentDeleted** (from Document Management Service)
- Used to: Update request document status
- Action: Remove document reference from request

---

## Domain Services

### RequestWorkflowService

**Purpose**: Orchestrates the approval workflow for requests.

**Responsibilities**:
- Determine next approver based on current status
- Validate state transitions
- Execute approval/decline/endorsement operations
- Maintain approval chain

**Methods**:
- submitForInitialApproval(request, headOfOfficeId)
- approveByHeadOfOffice(request, approverId, comments)
- declineByHeadOfOffice(request, declinerId, reason)
- endorseByReviewer(request, reviewerId, comments)
- declineByReviewer(request, reviewerId, reason)
- approveByHead(request, approverId, comments)
- returnToReviewer(request, returnerId, reason)
- markAsImplemented(request, implementerId, notes)

**Dependencies**: RequestRepository, RequestApprovalPolicy

---

### RequestRoutingService

**Purpose**: Determines the next approver/administrator based on request state and access type.

**Responsibilities**:
- Determine Head of Office for requestor
- Determine SMD/RDC Reviewer based on access type
- Determine SMD/RDC Head
- Determine Administrator based on access type

**Methods**:
- getHeadOfOfficeForRequestor(requestorId): UserId
- getReviewerForAccessType(accessType): UserId
- getSmdHeadId(): UserId
- getAdministratorForAccessType(accessType): UserId

**Dependencies**: AdministrationService (for user/role information)

---

### AccessTypeRoutingService

**Purpose**: Manages access type routing configuration.

**Responsibilities**:
- Add new access type
- Configure routing for access type
- Retrieve routing configuration
- Validate routing configuration

**Methods**:
- addAccessType(name, description, administratorRoles)
- configureRouting(accessTypeId, administratorRoles, defaultRole)
- getRoutingForAccessType(accessTypeId): AccessTypeRouting
- validateRoutingConfiguration(accessTypeId): boolean

**Dependencies**: AccessTypeRepository, AccessTypeRoutingPolicy

---

## Repositories

### RequestRepository

**Purpose**: Persists and retrieves Request aggregates.

**Responsibilities**:
- Save new requests
- Update existing requests
- Query requests by various criteria
- Maintain request history

**Query Methods**:
- findById(requestId): Request
- findByRequestor(requestorId): List<Request>
- findByStatus(status): List<Request>
- findPendingForApprover(approverId, approvalType): List<Request>
- findByDateRange(startDate, endDate): List<Request>
- findByAccessType(accessType): List<Request>
- search(criteria): List<Request>

**Persistence Methods**:
- save(request): void
- update(request): void
- delete(requestId): void

---

### AccessTypeRepository

**Purpose**: Persists and retrieves AccessType aggregates.

**Responsibilities**:
- Save new access types
- Update access types
- Query access types
- Maintain access type history

**Query Methods**:
- findById(accessTypeId): AccessType
- findAll(): List<AccessType>
- findByName(name): AccessType
- findByRoutingRole(role): List<AccessType>

**Persistence Methods**:
- save(accessType): void
- update(accessType): void
- delete(accessTypeId): void

---

## Specifications (Query Objects)

### RequestsByStatusSpecification

**Purpose**: Query requests by status.

**Criteria**: status

**Returns**: List<Request>

---

### RequestsByRequestorSpecification

**Purpose**: Query requests created by a specific requestor.

**Criteria**: requestorId

**Returns**: List<Request>

---

### PendingRequestsForApproverSpecification

**Purpose**: Query pending requests for a specific approver.

**Criteria**: approverId, approvalType (HeadOfOffice, Reviewer, Head)

**Returns**: List<Request>

---

### RequestsByDateRangeSpecification

**Purpose**: Query requests within a date range.

**Criteria**: startDate, endDate

**Returns**: List<Request>

---

### RequestsByAccessTypeSpecification

**Purpose**: Query requests by access type.

**Criteria**: accessType

**Returns**: List<Request>

---

### OverdueRequestsSpecification

**Purpose**: Query requests that have exceeded SLA timelines.

**Criteria**: currentStatus, slaThreshold

**Returns**: List<Request>

---

## Policies

### RequestApprovalPolicy

**Purpose**: Validates approval rules and constraints.

**Rules**:
- Request must have valid justification
- Request must have all required documents (if applicable)
- Approver must have appropriate role
- Request must be in valid state for approval
- Approval must include optional comments

**Validation Methods**:
- canApprove(request, approverId): boolean
- validateApprovalData(request, approvalData): ValidationResult

---

### RequestDeclinePolicy

**Purpose**: Validates decline rules and constraints.

**Rules**:
- Decline must include mandatory reason
- Decliner must have appropriate role
- Request must be in valid state for decline
- Declined requests cannot be resubmitted

**Validation Methods**:
- canDecline(request, declinerId): boolean
- validateDeclineData(request, declineData): ValidationResult

---

### RequestRoutingPolicy

**Purpose**: Determines routing based on access type and current status.

**Rules**:
- Draft requests route to requestor only
- Submitted requests route to Head of Office
- Approved by Head requests route to SMD/RDC Reviewer
- Endorsed requests route to SMD/RDC Head
- Finally approved requests route to Administrator based on access type

**Routing Methods**:
- getNextApprover(request): UserId
- getNextApprovalStage(request): ApprovalStage
- isValidTransition(currentStatus, newStatus): boolean

---

### AccessTypeRoutingPolicy

**Purpose**: Validates access type routing configuration.

**Rules**:
- Each access type must have at least one routing rule
- Each access type must have a default administrator role
- Routing roles must exist in the system
- Access type names must be unique

**Validation Methods**:
- isValidRoutingConfiguration(accessType): boolean
- validateAccessTypeCreation(name, description, routes): ValidationResult

---

## Factory Patterns

### RequestFactory

**Purpose**: Creates new Request aggregates with validation.

**Responsibilities**:
- Validate all required fields
- Generate unique RequestId
- Initialize request in Draft status
- Create initial RequestHistory entry

**Creation Method**:
- createRequest(requestorInfo, accessType, systemName, justification): Request
  - Validates requestor information
  - Validates access type
  - Validates justification length
  - Generates RequestId
  - Returns new Request aggregate in Draft status

---

### AccessTypeFactory

**Purpose**: Creates new AccessType aggregates with validation.

**Responsibilities**:
- Validate access type name uniqueness
- Validate routing configuration
- Generate unique AccessTypeId
- Initialize access type

**Creation Method**:
- createAccessType(name, description, administratorRoles): AccessType
  - Validates name is unique
  - Validates routing configuration
  - Generates AccessTypeId
  - Returns new AccessType aggregate

---

### RequestHistoryFactory

**Purpose**: Creates RequestHistory entries for audit trail.

**Responsibilities**:
- Create immutable history entries
- Capture all relevant state change information
- Timestamp all entries

**Creation Method**:
- createHistoryEntry(request, action, actor, previousStatus, newStatus, comments): RequestHistory
  - Captures current timestamp
  - Creates immutable entry
  - Returns RequestHistory entity

---

## Bounded Context Interactions

### Outbound Events

The Request Management Service publishes events that are consumed by:
- **Notification Service**: Receives all request status change events to send notifications
- **Administration Service**: Receives all events for audit logging and metrics

### Inbound Events

The Request Management Service consumes events from:
- **Document Management Service**: DocumentUploaded, DocumentDeleted events to track document status

---

## Aggregate Relationships Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Request Aggregate                         │
├─────────────────────────────────────────────────────────────┤
│ RequestId (Value Object)                                    │
│ RequestStatus (Value Object with state machine)             │
│ RequestorInfo (Value Object)                                │
│ AccessType (Value Object)                                   │
│ RequestJustification (Value Object)                         │
│ ApprovalChain (Value Object)                                │
│ RequestTimestamps (Value Object)                            │
│                                                              │
│ ├─ RequestApproval (Entity) [0..*]                          │
│ │  └─ ApprovalComment (Value Object)                        │
│ │                                                            │
│ └─ RequestHistory (Entity) [0..*]                           │
│    └─ Immutable audit trail                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  AccessType Aggregate                        │
├─────────────────────────────────────────────────────────────┤
│ AccessTypeId (Value Object)                                 │
│ AccessTypeName (Value Object)                               │
│ AccessTypeDescription (Value Object)                        │
│                                                              │
│ └─ AccessTypeRouting (Entity) [1..*]                        │
│    └─ AccessTypeRoutingConfig (Value Object)                │
└─────────────────────────────────────────────────────────────┘
```

---

## Event Flow Diagram

```
Request Created
    ↓
RequestCreated Event
    ├→ NotificationService (send confirmation)
    └→ AdministrationService (audit log)
    ↓
Request Submitted
    ↓
RequestSubmitted Event
    ├→ NotificationService (notify Head of Office)
    └→ AdministrationService (audit log)
    ↓
[Approved by Head of Office]
    ↓
RequestApprovedByHeadOfOffice Event
    ├→ NotificationService (notify requestor & reviewer)
    └→ AdministrationService (audit log)
    ↓
[Endorsed by Reviewer]
    ↓
RequestEndorsed Event
    ├→ NotificationService (notify SMD/RDC Head)
    └→ AdministrationService (audit log)
    ↓
[Approved by Head]
    ↓
RequestFinallyApproved Event
    ├→ NotificationService (notify administrator)
    └→ AdministrationService (audit log)
    ↓
[Implemented by Administrator]
    ↓
RequestImplemented Event
    ├→ NotificationService (notify requestor)
    └→ AdministrationService (audit log)
```

---

## State Machine Diagram

```
┌─────────┐
│  Draft  │
└────┬────┘
     │ submit()
     ↓
┌──────────────────────┐
│ PendingInitialApproval│
└────┬────────────┬────┘
     │            │
     │ approve()  │ decline()
     ↓            ↓
┌──────────────┐  ┌─────────┐
│ PendingReview│  │ Declined│
└────┬────┬────┘  └─────────┘
     │    │
     │    │ decline()
     │    ↓
     │  ┌─────────┐
     │  │ Declined│
     │  └─────────┘
     │
     │ endorse()
     ↓
┌──────────────────────┐
│ PendingFinalApproval │
└────┬────────────┬────┘
     │            │
     │ approve()  │ return()
     ↓            ↓
┌──────────┐  ┌──────────────────┐
│ Approved │  │ ReturnedToReviewer│
└────┬─────┘  └──────────────────┘
     │
     │ implement()
     ↓
┌──────────────┐
│ Implemented  │
└──────────────┘
```

---

## Summary

The Request Management Service domain model provides a comprehensive, event-driven architecture for managing access requests through a complex multi-level approval workflow. The use of aggregates, value objects, domain services, and specifications ensures clear separation of concerns, maintainability, and adherence to DDD principles. The factory patterns ensure consistent creation of domain objects, while policies encapsulate business rules and validation logic.

