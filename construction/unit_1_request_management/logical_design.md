# Unit 1: Request Management Service - Logical Design

## Document Information
- **Service**: Request Management Service (Unit 1)
- **Purpose**: Manage the complete lifecycle of access requests with multi-level approval workflow orchestration
- **Technology Stack**: Java, Spring Boot, PostgreSQL, Kafka, Spring Security
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
The Request Management Service is the core domain of the Access Request Processing System. It manages the complete lifecycle of access requests from creation through implementation, orchestrating a complex multi-level approval workflow involving Head of Office, SMD/RDC Reviewer, SMD/RDC Head, and System/Database Administrators.

### Scope
This logical design covers:
- Request creation, submission, and lifecycle management
- Multi-level approval workflow orchestration
- Access type management and routing configuration
- Event publishing and consumption
- REST API design for request operations
- Database schema and persistence strategy
- Integration with Document Management, Notification, and Administration services

### Key Responsibilities
1. Manage request state transitions through approval workflow
2. Orchestrate routing to appropriate approvers based on access type and office hierarchy
3. Publish domain events for status changes
4. Consume document events from Document Management Service
5. Provide REST APIs for request operations and queries
6. Maintain audit trail of all request actions

### Design Principles
- **Domain-Driven Design**: Rich domain model with aggregates, entities, and value objects
- **Event-Driven Architecture**: Asynchronous communication via Kafka message queue
- **Layered Architecture**: Clear separation of concerns (Presentation, Application, Domain, Infrastructure)
- **SOLID Principles**: Single responsibility, Open/closed, Liskov substitution, Interface segregation, Dependency inversion
- **Scalability**: Stateless services, asynchronous processing, horizontal scaling support

---

## System Architecture

### High-Level System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Access Request Processing System                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │              Request Management Service (Unit 1)                 │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ REST API Layer (Spring MVC)                                │  │   │
│  │  │ - Request endpoints                                        │  │   │
│  │  │ - AccessType endpoints                                     │  │   │
│  │  │ - Search endpoints                                         │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Application Layer (Spring Services)                        │  │   │
│  │  │ - RequestApplicationService                                │  │   │
│  │  │ - AccessTypeApplicationService                             │  │   │
│  │  │ - RequestWorkflowService                                   │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Domain Layer (DDD)                                         │  │   │
│  │  │ - Request Aggregate                                        │  │   │
│  │  │ - AccessType Aggregate                                     │  │   │
│  │  │ - Domain Services                                          │  │   │
│  │  │ - Value Objects                                            │  │   │
│  │  │ - Domain Events                                            │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Infrastructure Layer                                       │  │   │
│  │  │ - RequestRepository (JPA)                                  │  │   │
│  │  │ - AccessTypeRepository (JPA)                               │  │   │
│  │  │ - EventPublisher (Kafka)                                   │  │   │
│  │  │ - EventListener (Kafka)                                    │  │   │
│  │  │ - Database (PostgreSQL)                                    │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ External Services (via REST APIs & Kafka Events)                │   │
│  │ - Document Management Service                                   │   │
│  │ - Notification Service                                          │   │
│  │ - Administration Service                                        │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Infrastructure Components                                        │   │
│  │ - Kafka Message Queue (Event Bus)                               │   │
│  │ - PostgreSQL Database                                           │   │
│  │ - Spring Security (Authentication/Authorization)                │   │
│  │ - Docker Container                                              │   │
│  │ - Kubernetes Orchestration                                      │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Component Interactions

```
Client Request
    ↓
REST Controller (Spring MVC)
    ↓
Application Service
    ↓
Domain Service / Repository
    ↓
Domain Model (Aggregates, Entities, Value Objects)
    ↓
Repository (JPA/Hibernate)
    ↓
PostgreSQL Database
    ↓
Event Publisher (Kafka)
    ↓
Kafka Topic (request.events)
    ↓
External Services (Notification, Administration)
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
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Domain Layer (Business Rules)                               │
│ - Aggregates (Request, AccessType)                          │
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
- Return HTTP responses
- Handle authentication/authorization

#### Application Layer
- Orchestrate domain logic
- Manage transactions
- Publish domain events
- Coordinate between aggregates
- Handle cross-cutting concerns
- Implement use cases

#### Domain Layer
- Encapsulate business rules
- Manage aggregate state
- Validate invariants
- Publish domain events
- Provide domain services
- Define specifications

#### Infrastructure Layer
- Persist aggregates to database
- Publish/consume events
- Integrate with external services
- Implement security
- Provide logging and monitoring
- Handle configuration

---

## API Layer Design

### REST API Endpoints

#### Request Management Endpoints

**1. Create Request**
- **Endpoint**: `POST /api/v1/requests`
- **Authentication**: Required (JWT Token)
- **Authorization**: Employee role
- **Request Body**:
  ```
  {
    "requestorId": "string (UUID)",
    "accessType": "string (OS|WebApp|Database)",
    "systemName": "string",
    "justification": "string (min 10, max 10MB)",
    "documents": ["documentId1", "documentId2"]
  }
  ```
- **Response** (201 Created):
  ```
  {
    "requestId": "string (UUID)",
    "status": "Draft",
    "createdAt": "ISO-8601 datetime",
    "requestorId": "string"
  }
  ```
- **Error Responses**:
  - 400: Invalid input data
  - 401: Unauthorized
  - 403: Forbidden (insufficient permissions)
  - 409: Conflict (duplicate request)

**2. Get Request Details**
- **Endpoint**: `GET /api/v1/requests/{requestId}`
- **Authentication**: Required
- **Authorization**: Requestor, Approver, or Admin
- **Response** (200 OK):
  ```
  {
    "requestId": "string",
    "requestorId": "string",
    "requestorName": "string",
    "accessType": "string",
    "systemName": "string",
    "justification": "string",
    "status": "string",
    "createdAt": "datetime",
    "submittedAt": "datetime",
    "approvals": [
      {
        "stage": "HeadOfOffice|Reviewer|Head",
        "approverId": "string",
        "approverName": "string",
        "status": "Approved|Declined|Pending",
        "comments": "string",
        "timestamp": "datetime"
      }
    ],
    "history": [
      {
        "action": "string",
        "actor": "string",
        "timestamp": "datetime",
        "comments": "string"
      }
    ],
    "documents": [
      {
        "documentId": "string",
        "fileName": "string",
        "uploadedAt": "datetime"
      }
    ]
  }
  ```

**3. List Requests**
- **Endpoint**: `GET /api/v1/requests`
- **Authentication**: Required
- **Query Parameters**:
  - `status`: Filter by status (Draft, PendingInitialApproval, etc.)
  - `requestorId`: Filter by requestor
  - `accessType`: Filter by access type
  - `fromDate`: Filter by date range (ISO-8601)
  - `toDate`: Filter by date range (ISO-8601)
  - `page`: Page number (default: 1)
  - `size`: Page size (default: 20, max: 100)
  - `sortBy`: Sort field (createdAt, submittedAt, status)
  - `sortOrder`: Sort order (ASC, DESC)
- **Response** (200 OK):
  ```
  {
    "requests": [
      {
        "requestId": "string",
        "requestorName": "string",
        "accessType": "string",
        "systemName": "string",
        "status": "string",
        "submittedAt": "datetime",
        "currentApprover": "string"
      }
    ],
    "totalCount": "number",
    "page": "number",
    "size": "number",
    "totalPages": "number"
  }
  ```

**4. Submit Request**
- **Endpoint**: `PUT /api/v1/requests/{requestId}/submit`
- **Authentication**: Required
- **Authorization**: Requestor
- **Request Body**:
  ```
  {
    "headOfOfficeId": "string (UUID)"
  }
  ```
- **Response** (200 OK):
  ```
  {
    "requestId": "string",
    "status": "PendingInitialApproval",
    "submittedAt": "datetime"
  }
  ```
- **Error Responses**:
  - 400: Invalid state transition
  - 404: Request not found
  - 409: Request already submitted

**5. Approve Request**
- **Endpoint**: `PUT /api/v1/requests/{requestId}/approve`
- **Authentication**: Required
- **Authorization**: Approver (Head of Office, Reviewer, or Head)
- **Request Body**:
  ```
  {
    "approverId": "string (UUID)",
    "approvalType": "HeadOfOffice|Reviewer|Head",
    "comments": "string (optional)"
  }
  ```
- **Response** (200 OK):
  ```
  {
    "requestId": "string",
    "status": "string (new status)",
    "approvedAt": "datetime"
  }
  ```

**6. Decline Request**
- **Endpoint**: `PUT /api/v1/requests/{requestId}/decline`
- **Authentication**: Required
- **Authorization**: Approver
- **Request Body**:
  ```
  {
    "declinerId": "string (UUID)",
    "reason": "string (required, min 10 chars)"
  }
  ```
- **Response** (200 OK):
  ```
  {
    "requestId": "string",
    "status": "Declined",
    "declinedAt": "datetime"
  }
  ```

**7. Endorse Request**
- **Endpoint**: `PUT /api/v1/requests/{requestId}/endorse`
- **Authentication**: Required
- **Authorization**: SMD/RDC Reviewer
- **Request Body**:
  ```
  {
    "reviewerId": "string (UUID)",
    "comments": "string (optional)"
  }
  ```
- **Response** (200 OK):
  ```
  {
    "requestId": "string",
    "status": "PendingFinalApproval",
    "endorsedAt": "datetime"
  }
  ```

**8. Return Request**
- **Endpoint**: `PUT /api/v1/requests/{requestId}/return`
- **Authentication**: Required
- **Authorization**: SMD/RDC Head
- **Request Body**:
  ```
  {
    "returnerId": "string (UUID)",
    "reason": "string (required)"
  }
  ```
- **Response** (200 OK):
  ```
  {
    "requestId": "string",
    "status": "ReturnedToReviewer",
    "returnedAt": "datetime"
  }
  ```

**9. Mark as Implemented**
- **Endpoint**: `PUT /api/v1/requests/{requestId}/implement`
- **Authentication**: Required
- **Authorization**: Administrator
- **Request Body**:
  ```
  {
    "implementerId": "string (UUID)",
    "notes": "string (optional)"
  }
  ```
- **Response** (200 OK):
  ```
  {
    "requestId": "string",
    "status": "Implemented",
    "implementedAt": "datetime"
  }
  ```

**10. Get Request History**
- **Endpoint**: `GET /api/v1/requests/{requestId}/history`
- **Authentication**: Required
- **Response** (200 OK):
  ```
  {
    "requestId": "string",
    "history": [
      {
        "timestamp": "datetime",
        "action": "string",
        "actor": "string",
        "previousStatus": "string",
        "newStatus": "string",
        "comments": "string"
      }
    ]
  }
  ```

#### Access Type Endpoints

**11. List Access Types**
- **Endpoint**: `GET /api/v1/access-types`
- **Authentication**: Required
- **Response** (200 OK):
  ```
  {
    "accessTypes": [
      {
        "accessTypeId": "string",
        "name": "string",
        "description": "string",
        "routingRules": [
          {
            "administratorRole": "string",
            "isDefault": "boolean"
          }
        ]
      }
    ]
  }
  ```

**12. Create Access Type**
- **Endpoint**: `POST /api/v1/access-types`
- **Authentication**: Required
- **Authorization**: System Admin
- **Request Body**:
  ```
  {
    "name": "string (unique)",
    "description": "string",
    "administratorRoles": ["role1", "role2"]
  }
  ```
- **Response** (201 Created):
  ```
  {
    "accessTypeId": "string",
    "name": "string",
    "createdAt": "datetime"
  }
  ```

**13. Update Access Type Routing**
- **Endpoint**: `PUT /api/v1/access-types/{accessTypeId}/routing`
- **Authentication**: Required
- **Authorization**: System Admin
- **Request Body**:
  ```
  {
    "administratorRoles": ["role1", "role2"],
    "defaultRole": "string"
  }
  ```
- **Response** (200 OK):
  ```
  {
    "accessTypeId": "string",
    "routingRules": [],
    "updatedAt": "datetime"
  }
  ```

#### Search Endpoints

**14. Search Requests**
- **Endpoint**: `GET /api/v1/requests/search`
- **Authentication**: Required
- **Query Parameters**:
  - `q`: Search query (requestId, requestorName, systemName)
  - `accessType`: Filter by access type
  - `status`: Filter by status
  - `officeId`: Filter by office
  - `fromDate`: Date range start
  - `toDate`: Date range end
  - `page`: Page number
  - `size`: Page size
- **Response** (200 OK):
  ```
  {
    "results": [
      {
        "requestId": "string",
        "requestorName": "string",
        "accessType": "string",
        "systemName": "string",
        "status": "string",
        "submittedAt": "datetime"
      }
    ],
    "totalCount": "number"
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
    "path": "/api/v1/requests"
  }
}
```

### Common Error Codes

- `INVALID_INPUT`: Input validation failed
- `UNAUTHORIZED`: Authentication required
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `CONFLICT`: Resource conflict (e.g., duplicate)
- `INVALID_STATE_TRANSITION`: Invalid workflow state transition
- `BUSINESS_RULE_VIOLATION`: Business rule violated
- `INTERNAL_ERROR`: Internal server error

---

## Application Layer Design

### Application Services

The Application Layer contains Spring Services that orchestrate domain logic and manage transactions.

#### RequestApplicationService

**Responsibilities**:
- Create new requests
- Submit requests for approval
- Query requests with various filters
- Coordinate between domain services and repositories
- Manage transactions

**Key Methods**:
- `createRequest(CreateRequestCommand): RequestDTO`
  - Validates input
  - Calls RequestFactory to create aggregate
  - Saves to repository
  - Publishes RequestCreated event
  - Returns RequestDTO

- `submitRequest(SubmitRequestCommand): RequestDTO`
  - Validates request state
  - Calls RequestWorkflowService to submit
  - Saves updated request
  - Publishes RequestSubmitted event
  - Returns updated RequestDTO

- `getRequest(requestId): RequestDTO`
  - Queries repository
  - Converts aggregate to DTO
  - Returns RequestDTO

- `listRequests(ListRequestsQuery): Page<RequestDTO>`
  - Applies filters and pagination
  - Uses RequestRepository specifications
  - Returns paginated results

- `searchRequests(SearchQuery): List<RequestDTO>`
  - Performs full-text search
  - Applies filters
  - Returns search results

#### RequestApprovalApplicationService

**Responsibilities**:
- Handle approval operations
- Manage approval workflow transitions
- Publish approval events

**Key Methods**:
- `approveRequest(ApproveRequestCommand): RequestDTO`
  - Validates approver role and permissions
  - Calls RequestWorkflowService.approveByHeadOfOffice()
  - Saves updated request
  - Publishes RequestApprovedByHeadOfOffice event
  - Returns updated RequestDTO

- `declineRequest(DeclineRequestCommand): RequestDTO`
  - Validates decline reason
  - Calls RequestWorkflowService.declineByHeadOfOffice()
  - Saves updated request
  - Publishes RequestDeclined event
  - Returns updated RequestDTO

- `endorseRequest(EndorseRequestCommand): RequestDTO`
  - Validates reviewer role
  - Calls RequestWorkflowService.endorseByReviewer()
  - Saves updated request
  - Publishes RequestEndorsed event
  - Returns updated RequestDTO

- `returnRequest(ReturnRequestCommand): RequestDTO`
  - Validates return reason
  - Calls RequestWorkflowService.returnToReviewer()
  - Saves updated request
  - Publishes RequestReturned event
  - Returns updated RequestDTO

- `implementRequest(ImplementRequestCommand): RequestDTO`
  - Validates administrator role
  - Calls RequestWorkflowService.markAsImplemented()
  - Saves updated request
  - Publishes RequestImplemented event
  - Returns updated RequestDTO

#### AccessTypeApplicationService

**Responsibilities**:
- Manage access types
- Configure routing rules
- Query access types

**Key Methods**:
- `createAccessType(CreateAccessTypeCommand): AccessTypeDTO`
  - Validates access type name uniqueness
  - Calls AccessTypeFactory to create aggregate
  - Saves to repository
  - Publishes AccessTypeAdded event
  - Returns AccessTypeDTO

- `configureRouting(ConfigureRoutingCommand): AccessTypeDTO`
  - Validates routing configuration
  - Updates access type routing
  - Saves updated access type
  - Publishes AccessTypeRoutingConfigured event
  - Returns updated AccessTypeDTO

- `listAccessTypes(): List<AccessTypeDTO>`
  - Queries all access types
  - Returns list of AccessTypeDTO

- `getAccessType(accessTypeId): AccessTypeDTO`
  - Queries repository
  - Returns AccessTypeDTO

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
- Publishes to Kafka topic `request.events`
- Handles serialization/deserialization
- Implements retry logic with exponential backoff
- Logs all published events

#### Event Publishing Flow

1. Domain aggregate publishes event (added to event list)
2. Application service saves aggregate to repository
3. Application service calls EventPublisher.publishAll()
4. EventPublisher converts events to JSON
5. EventPublisher publishes to Kafka topic
6. External services consume events from Kafka

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

#### Consumed Events

**DocumentUploaded** (from Document Management Service)
- Handler: DocumentUploadedEventHandler
- Action: Update request with document metadata
- Idempotency: Check if document already associated

**DocumentDeleted** (from Document Management Service)
- Handler: DocumentDeletedEventHandler
- Action: Remove document reference from request
- Idempotency: Check if document still associated

### Transaction Management

#### Transaction Boundaries

- Each application service method is a transaction boundary
- Uses Spring @Transactional annotation
- Propagation: REQUIRED (join existing or create new)
- Isolation: READ_COMMITTED (default)
- Rollback on RuntimeException

#### Saga Pattern for Distributed Transactions

For operations spanning multiple services:
1. Request Management publishes event
2. External service consumes event and performs action
3. External service publishes completion event
4. Request Management consumes completion event
5. If any step fails, compensating transactions are triggered

### Command & Query Separation (CQRS)

#### Commands (Write Operations)

- CreateRequestCommand
- SubmitRequestCommand
- ApproveRequestCommand
- DeclineRequestCommand
- EndorseRequestCommand
- ReturnRequestCommand
- ImplementRequestCommand
- CreateAccessTypeCommand
- ConfigureRoutingCommand

#### Queries (Read Operations)

- GetRequestQuery
- ListRequestsQuery
- SearchRequestsQuery
- GetAccessTypeQuery
- ListAccessTypesQuery
- GetRequestHistoryQuery

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

---

## Domain Layer Design

### Aggregate Design

#### Request Aggregate

**Aggregate Root**: Request
- **Identifier**: RequestId (UUID)
- **Lifecycle**: Draft → PendingInitialApproval → PendingReview → PendingFinalApproval → Approved → Implemented
- **Invariants**:
  - Request must have valid requestor
  - Request must have valid access type
  - Request cannot transition to invalid states
  - Request cannot be approved without justification
  - Request cannot be declined without reason
  - All required approvals must be obtained before implementation

**Entities within Aggregate**:
- RequestApproval: Tracks approval at each stage (immutable)
- RequestHistory: Audit trail of all state changes (append-only)

**Value Objects within Aggregate**:
- RequestId: Unique identifier
- RequestStatus: State machine with validation
- RequestorInfo: Requestor information (immutable)
- AccessType: Access type information (immutable)
- RequestJustification: Justification with validation
- ApprovalComment: Comments with metadata (immutable)
- ApprovalChain: Tracks approvers at each stage
- RequestTimestamps: All timestamps (immutable)

**Aggregate Behavior**:
- `submit(headOfOfficeId)`: Transition to PendingInitialApproval
- `approveByHeadOfOffice(approverId, comments)`: Transition to PendingReview
- `declineByHeadOfOffice(declinerId, reason)`: Transition to Declined
- `endorseByReviewer(reviewerId, comments)`: Transition to PendingFinalApproval
- `declineByReviewer(declinerId, reason)`: Transition to Declined
- `approveByHead(approverId, comments)`: Transition to Approved
- `returnToReviewer(returnerId, reason)`: Transition to ReturnedToReviewer
- `markAsImplemented(implementerId, notes)`: Transition to Implemented

#### AccessType Aggregate

**Aggregate Root**: AccessType
- **Identifier**: AccessTypeId (UUID)
- **Invariants**:
  - Access type must have name
  - Access type must have at least one routing rule
  - Access type must have default administrator role
  - Access type names must be unique

**Entities within Aggregate**:
- AccessTypeRouting: Routing configuration (immutable)

**Value Objects within Aggregate**:
- AccessTypeId: Unique identifier
- AccessTypeName: Name (immutable)
- AccessTypeDescription: Description (immutable)
- AccessTypeRoutingConfig: Routing configuration (immutable)

**Aggregate Behavior**:
- `configureRouting(administratorRoles, defaultRole)`: Update routing configuration
- `getRoutingForRole(role)`: Get routing for specific role

### Domain Services

#### RequestWorkflowService

**Purpose**: Orchestrate request approval workflow

**Methods**:
- `submitForInitialApproval(request, headOfOfficeId): void`
  - Validates request state
  - Transitions to PendingInitialApproval
  - Records submission timestamp

- `approveByHeadOfOffice(request, approverId, comments): void`
  - Validates approver role
  - Transitions to PendingReview
  - Records approval

- `declineByHeadOfOffice(request, declinerId, reason): void`
  - Validates decliner role
  - Transitions to Declined
  - Records decline reason

- `endorseByReviewer(request, reviewerId, comments): void`
  - Validates reviewer role
  - Transitions to PendingFinalApproval
  - Records endorsement

- `declineByReviewer(request, declinerId, reason): void`
  - Validates decliner role
  - Transitions to Declined
  - Records decline reason

- `approveByHead(request, approverId, comments): void`
  - Validates approver role
  - Transitions to Approved
  - Records approval

- `returnToReviewer(request, returnerId, reason): void`
  - Validates returner role
  - Transitions to ReturnedToReviewer
  - Records return reason

- `markAsImplemented(request, implementerId, notes): void`
  - Validates implementer role
  - Transitions to Implemented
  - Records implementation

#### RequestRoutingService

**Purpose**: Determine next approver based on request state and access type

**Methods**:
- `getHeadOfOfficeForRequestor(requestorId): UserId`
  - Calls Administration Service API
  - Returns Head of Office for requestor's office

- `getReviewerForAccessType(accessType): UserId`
  - Calls Administration Service API
  - Returns SMD/RDC Reviewer for access type

- `getSmdHeadId(): UserId`
  - Calls Administration Service API
  - Returns SMD/RDC Head

- `getAdministratorForAccessType(accessType): UserId`
  - Calls Administration Service API
  - Returns Administrator for access type

#### AccessTypeRoutingService

**Purpose**: Manage access type routing configuration

**Methods**:
- `addAccessType(name, description, administratorRoles): AccessType`
  - Validates name uniqueness
  - Creates new AccessType aggregate
  - Returns created aggregate

- `configureRouting(accessTypeId, administratorRoles, defaultRole): void`
  - Validates routing configuration
  - Updates access type routing
  - Publishes AccessTypeRoutingConfigured event

- `getRoutingForAccessType(accessTypeId): AccessTypeRouting`
  - Queries repository
  - Returns routing configuration

- `validateRoutingConfiguration(accessTypeId): boolean`
  - Validates routing rules exist
  - Validates default role exists
  - Returns validation result

### Value Objects

#### RequestStatus

**Purpose**: Encapsulate request status with state machine logic

**States**:
- Draft: Initial state
- PendingInitialApproval: Awaiting Head of Office approval
- PendingReview: Awaiting SMD/RDC Reviewer review
- PendingFinalApproval: Awaiting SMD/RDC Head approval
- ReturnedToReviewer: Returned by Head for more review
- Approved: Approved and ready for implementation
- Declined: Declined at any stage
- Implemented: Access implemented

**Valid Transitions**:
- Draft → PendingInitialApproval (submit)
- PendingInitialApproval → PendingReview (approve)
- PendingInitialApproval → Declined (decline)
- PendingReview → PendingFinalApproval (endorse)
- PendingReview → Declined (decline)
- PendingFinalApproval → Approved (approve)
- PendingFinalApproval → ReturnedToReviewer (return)
- ReturnedToReviewer → PendingFinalApproval (re-endorse)
- ReturnedToReviewer → Declined (decline)
- Approved → Implemented (implement)

**Behavior**:
- `canTransitionTo(newStatus): boolean`
  - Validates transition is allowed
  - Returns true/false

- `isTerminal(): boolean`
  - Returns true if Declined or Implemented
  - Returns false otherwise

#### RequestJustification

**Purpose**: Encapsulate justification with validation

**Constraints**:
- Minimum length: 10 characters
- Maximum length: 10MB
- Required field

**Behavior**:
- `validate(): ValidationResult`
  - Checks length constraints
  - Returns validation result

- `getText(): String`
  - Returns justification text

#### ApprovalComment

**Purpose**: Encapsulate approval/decline comments

**Constraints**:
- Optional field
- Maximum length: 5000 characters
- Immutable once created

**Behavior**:
- `getText(): String`
  - Returns comment text

- `getCreatedBy(): UserId`
  - Returns creator ID

- `getCreatedAt(): Instant`
  - Returns creation timestamp

### Repositories

#### RequestRepository

**Purpose**: Persist and retrieve Request aggregates

**Query Methods**:
- `findById(requestId): Optional<Request>`
- `findByRequestor(requestorId): List<Request>`
- `findByStatus(status): List<Request>`
- `findPendingForApprover(approverId, approvalType): List<Request>`
- `findByDateRange(startDate, endDate): List<Request>`
- `findByAccessType(accessType): List<Request>`
- `search(criteria): List<Request>`

**Persistence Methods**:
- `save(request): void`
- `update(request): void`
- `delete(requestId): void`

**Implementation**: Spring Data JPA with custom queries

#### AccessTypeRepository

**Purpose**: Persist and retrieve AccessType aggregates

**Query Methods**:
- `findById(accessTypeId): Optional<AccessType>`
- `findAll(): List<AccessType>`
- `findByName(name): Optional<AccessType>`
- `findByRoutingRole(role): List<AccessType>`

**Persistence Methods**:
- `save(accessType): void`
- `update(accessType): void`
- `delete(accessTypeId): void`

**Implementation**: Spring Data JPA

### Specifications (Query Objects)

#### RequestsByStatusSpecification

**Purpose**: Query requests by status

**Criteria**: status

**Implementation**: Spring Data Specification

#### RequestsByRequestorSpecification

**Purpose**: Query requests by requestor

**Criteria**: requestorId

**Implementation**: Spring Data Specification

#### PendingRequestsForApproverSpecification

**Purpose**: Query pending requests for approver

**Criteria**: approverId, approvalType

**Implementation**: Spring Data Specification

#### RequestsByDateRangeSpecification

**Purpose**: Query requests by date range

**Criteria**: startDate, endDate

**Implementation**: Spring Data Specification

#### RequestsByAccessTypeSpecification

**Purpose**: Query requests by access type

**Criteria**: accessType

**Implementation**: Spring Data Specification

#### OverdueRequestsSpecification

**Purpose**: Query overdue requests

**Criteria**: currentStatus, slaThreshold

**Implementation**: Spring Data Specification

### Policies

#### RequestApprovalPolicy

**Purpose**: Validate approval rules and constraints

**Rules**:
- Request must have valid justification
- Request must have all required documents
- Approver must have appropriate role
- Request must be in valid state for approval
- Approval must include optional comments

**Validation Methods**:
- `canApprove(request, approverId): boolean`
- `validateApprovalData(request, approvalData): ValidationResult`

#### RequestDeclinePolicy

**Purpose**: Validate decline rules and constraints

**Rules**:
- Decline must include mandatory reason
- Decliner must have appropriate role
- Request must be in valid state for decline
- Declined requests cannot be resubmitted

**Validation Methods**:
- `canDecline(request, declinerId): boolean`
- `validateDeclineData(request, declineData): ValidationResult`

#### RequestRoutingPolicy

**Purpose**: Determine routing based on access type and status

**Rules**:
- Draft requests route to requestor only
- Submitted requests route to Head of Office
- Approved by Head requests route to SMD/RDC Reviewer
- Endorsed requests route to SMD/RDC Head
- Finally approved requests route to Administrator

**Routing Methods**:
- `getNextApprover(request): UserId`
- `getNextApprovalStage(request): ApprovalStage`
- `isValidTransition(currentStatus, newStatus): boolean`

#### AccessTypeRoutingPolicy

**Purpose**: Validate access type routing configuration

**Rules**:
- Each access type must have at least one routing rule
- Each access type must have default administrator role
- Routing roles must exist in system
- Access type names must be unique

**Validation Methods**:
- `isValidRoutingConfiguration(accessType): boolean`
- `validateAccessTypeCreation(name, description, routes): ValidationResult`

### Factories

#### RequestFactory

**Purpose**: Create new Request aggregates with validation

**Creation Method**:
- `createRequest(requestorInfo, accessType, systemName, justification): Request`
  - Validates requestor information
  - Validates access type
  - Validates justification length
  - Generates RequestId
  - Returns new Request in Draft status
  - Publishes RequestCreated event

#### AccessTypeFactory

**Purpose**: Create new AccessType aggregates with validation

**Creation Method**:
- `createAccessType(name, description, administratorRoles): AccessType`
  - Validates name uniqueness
  - Validates routing configuration
  - Generates AccessTypeId
  - Returns new AccessType
  - Publishes AccessTypeAdded event

#### RequestHistoryFactory

**Purpose**: Create RequestHistory entries for audit trail

**Creation Method**:
- `createHistoryEntry(request, action, actor, previousStatus, newStatus, comments): RequestHistory`
  - Captures current timestamp
  - Creates immutable entry
  - Returns RequestHistory entity

---

## Infrastructure Layer Design

### Database Design

#### Database Technology
- **DBMS**: PostgreSQL 13+
- **ORM**: Hibernate with Spring Data JPA
- **Migrations**: Liquibase
- **Connection Pooling**: HikariCP

#### Core Tables

**access_requests**
- `request_id` (UUID, Primary Key)
- `requestor_id` (UUID, Foreign Key to users)
- `access_type_id` (UUID, Foreign Key to access_types)
- `system_name` (VARCHAR)
- `justification` (TEXT)
- `status` (VARCHAR, Enum)
- `created_at` (TIMESTAMP)
- `submitted_at` (TIMESTAMP, nullable)
- `approved_at` (TIMESTAMP, nullable)
- `implemented_at` (TIMESTAMP, nullable)
- `created_by` (UUID)
- `updated_by` (UUID)
- `updated_at` (TIMESTAMP)

**Indexes**:
- PRIMARY KEY: request_id
- UNIQUE: request_id
- INDEX: requestor_id (for filtering by requestor)
- INDEX: status (for filtering by status)
- INDEX: access_type_id (for filtering by access type)
- INDEX: created_at (for date range queries)
- INDEX: submitted_at (for pending requests)
- COMPOSITE INDEX: (status, created_at) for common queries

**request_approvals**
- `approval_id` (UUID, Primary Key)
- `request_id` (UUID, Foreign Key)
- `approver_id` (UUID, Foreign Key to users)
- `approval_type` (VARCHAR, Enum: HeadOfOffice, Reviewer, Head)
- `status` (VARCHAR, Enum: Approved, Declined, Pending)
- `comments` (TEXT, nullable)
- `created_at` (TIMESTAMP)

**Indexes**:
- PRIMARY KEY: approval_id
- FOREIGN KEY: request_id
- INDEX: request_id (for finding approvals for request)
- INDEX: approver_id (for finding approvals by approver)
- COMPOSITE INDEX: (request_id, approval_type)

**request_history**
- `history_id` (UUID, Primary Key)
- `request_id` (UUID, Foreign Key)
- `action` (VARCHAR)
- `actor_id` (UUID, Foreign Key to users)
- `previous_status` (VARCHAR, nullable)
- `new_status` (VARCHAR, nullable)
- `comments` (TEXT, nullable)
- `created_at` (TIMESTAMP)

**Indexes**:
- PRIMARY KEY: history_id
- FOREIGN KEY: request_id
- INDEX: request_id (for finding history for request)
- INDEX: created_at (for date range queries)

**access_types**
- `access_type_id` (UUID, Primary Key)
- `name` (VARCHAR, Unique)
- `description` (TEXT)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `created_by` (UUID)
- `updated_by` (UUID)

**Indexes**:
- PRIMARY KEY: access_type_id
- UNIQUE: name

**access_type_routing**
- `routing_id` (UUID, Primary Key)
- `access_type_id` (UUID, Foreign Key)
- `administrator_role` (VARCHAR)
- `is_default` (BOOLEAN)
- `created_at` (TIMESTAMP)

**Indexes**:
- PRIMARY KEY: routing_id
- FOREIGN KEY: access_type_id
- INDEX: access_type_id (for finding routing for access type)
- COMPOSITE INDEX: (access_type_id, is_default)

**request_documents**
- `document_id` (UUID, Primary Key)
- `request_id` (UUID, Foreign Key)
- `document_name` (VARCHAR)
- `document_size` (BIGINT)
- `uploaded_at` (TIMESTAMP)
- `uploaded_by` (UUID)

**Indexes**:
- PRIMARY KEY: document_id
- FOREIGN KEY: request_id
- INDEX: request_id (for finding documents for request)

#### Entity-Relationship Diagram

```
┌─────────────────────────┐
│    access_requests      │
├─────────────────────────┤
│ request_id (PK)         │
│ requestor_id (FK)       │
│ access_type_id (FK)     │
│ system_name             │
│ justification           │
│ status                  │
│ created_at              │
│ submitted_at            │
│ approved_at             │
│ implemented_at          │
│ created_by              │
│ updated_by              │
│ updated_at              │
└─────────────────────────┘
         │
         ├─────────────────────────────────────┐
         │                                     │
         ↓                                     ↓
┌─────────────────────────┐    ┌──────────────────────────┐
│  request_approvals      │    │   request_history        │
├─────────────────────────┤    ├──────────────────────────┤
│ approval_id (PK)        │    │ history_id (PK)          │
│ request_id (FK)         │    │ request_id (FK)          │
│ approver_id (FK)        │    │ action                   │
│ approval_type           │    │ actor_id (FK)            │
│ status                  │    │ previous_status          │
│ comments                │    │ new_status               │
│ created_at              │    │ comments                 │
└─────────────────────────┘    │ created_at               │
                               └──────────────────────────┘

┌─────────────────────────┐
│    access_types         │
├─────────────────────────┤
│ access_type_id (PK)     │
│ name (UNIQUE)           │
│ description             │
│ created_at              │
│ updated_at              │
│ created_by              │
│ updated_by              │
└─────────────────────────┘
         │
         ↓
┌─────────────────────────┐
│ access_type_routing     │
├─────────────────────────┤
│ routing_id (PK)         │
│ access_type_id (FK)     │
│ administrator_role      │
│ is_default              │
│ created_at              │
└─────────────────────────┘

┌─────────────────────────┐
│  request_documents      │
├─────────────────────────┤
│ document_id (PK)        │
│ request_id (FK)         │
│ document_name           │
│ document_size           │
│ uploaded_at             │
│ uploaded_by             │
└─────────────────────────┘
```

### Event Bus Design

#### Kafka Configuration

**Topics**:
- `request.events`: All request lifecycle events
  - Partitions: 3 (for parallelism)
  - Replication Factor: 2 (for high availability)
  - Retention: 7 days
  - Cleanup Policy: delete

**Consumer Groups**:
- `request-management-service`: Consumes document events
- `notification-service`: Consumes all request events
- `administration-service`: Consumes all events for audit

#### Event Serialization

**Format**: JSON with schema versioning

**Event Structure**:
```json
{
  "eventId": "UUID",
  "eventType": "RequestCreated",
  "timestamp": "ISO-8601",
  "source": "request-management-service",
  "version": "1.0",
  "payload": {
    "requestId": "UUID",
    "requestorId": "UUID",
    ...
  }
}
```

#### Event Publishing Implementation

**Publisher**: KafkaTemplate (Spring Kafka)
- Asynchronous publishing
- Callback for success/failure
- Retry logic with exponential backoff
- Dead letter queue for failed messages

**Flow**:
1. Domain aggregate publishes event
2. Application service collects events
3. After repository save, publish events
4. KafkaTemplate sends to Kafka
5. Kafka broker persists message
6. Consumer services receive message

#### Event Consumption Implementation

**Listener**: @KafkaListener (Spring Kafka)
- Listens to Kafka topic
- Deserializes JSON to event
- Routes to appropriate handler
- Implements idempotency
- Logs all consumed events

**Idempotency Strategy**:
- Store processed event IDs in database
- Check if event already processed before handling
- Use event ID as unique constraint

### External Service Integration

#### Administration Service Client

**Purpose**: Get user/role information for routing and authorization

**Methods**:
- `getHeadOfOfficeForRequestor(requestorId): User`
- `getReviewerForAccessType(accessType): User`
- `getSmdHeadId(): User`
- `getAdministratorForAccessType(accessType): User`
- `getUserById(userId): User`
- `hasRole(userId, role): boolean`

**Implementation**: RestTemplate or WebClient (Spring)
- Base URL: Configurable
- Timeout: 5 seconds
- Retry: 3 attempts with exponential backoff
- Circuit breaker: Hystrix/Resilience4j

#### Document Management Service Client

**Purpose**: Get document information for requests

**Methods**:
- `getDocumentMetadata(documentId): Document`
- `listDocumentsForRequest(requestId): List<Document>`
- `validateDocumentExists(documentId): boolean`

**Implementation**: RestTemplate or WebClient
- Base URL: Configurable
- Timeout: 5 seconds
- Retry: 3 attempts
- Circuit breaker: Hystrix/Resilience4j

#### Notification Service Client

**Purpose**: Send notifications (optional, events are primary)

**Methods**:
- `sendNotification(notification): void`

**Implementation**: Kafka events (primary), REST API (fallback)

### Security Implementation

#### Authentication

**Mechanism**: JWT (JSON Web Tokens)
- Issued by Administration Service
- Validated by Spring Security
- Token format: Bearer <token>
- Token expiration: Configurable (default: 1 hour)

**Implementation**:
- JwtAuthenticationFilter: Extracts token from header
- JwtTokenProvider: Validates and parses token
- UserDetailsService: Loads user details from token

#### Authorization

**Mechanism**: Role-Based Access Control (RBAC)
- Roles: Employee, HeadOfOffice, Reviewer, Head, SystemAdmin, DBAdmin
- Permissions: Defined per endpoint
- Spring Security @PreAuthorize annotations

**Implementation**:
- SecurityConfig: Configures security rules
- @PreAuthorize("hasRole('EMPLOYEE')"): Restricts endpoint to role
- Custom authorization logic for complex rules

#### Data Protection

**Encryption**:
- Passwords: BCrypt hashing
- Sensitive data: AES-256 encryption at rest
- HTTPS: TLS 1.2+ for transport

**Audit Logging**:
- All user actions logged
- Immutable audit trail
- Retention: Configurable (default: 1 year)

### Logging & Monitoring

#### Logging Strategy

**Framework**: SLF4J with Logback

**Log Levels**:
- ERROR: Errors and exceptions
- WARN: Warnings and potential issues
- INFO: Important business events
- DEBUG: Detailed debugging information
- TRACE: Very detailed tracing

**Log Format**:
```
[timestamp] [level] [logger] [thread] - [message]
```

**Logged Events**:
- Request creation, submission, approval, decline, implementation
- Event publishing and consumption
- External service calls
- Database operations
- Security events (login, authorization failures)
- Errors and exceptions

#### Monitoring Strategy

**Metrics**:
- Request count by status
- Request processing time by stage
- Approval rate
- Decline rate
- Event publishing/consumption rate
- API response times
- Database query performance
- Error rates

**Tools**:
- Micrometer: Metrics collection
- Prometheus: Metrics storage
- Grafana: Metrics visualization
- ELK Stack: Log aggregation and analysis

**Alerts**:
- High error rate (> 5%)
- Slow API response (> 1 second)
- Failed event publishing
- Database connection issues
- Service unavailability

### Caching Strategy

#### Cache Layers

**L1: Application Cache** (In-Memory)
- Cache: Spring Cache with Caffeine
- TTL: 5 minutes
- Cached data:
  - Access types (rarely change)
  - User information (from Administration Service)
  - Role information

**L2: Distributed Cache** (Redis, optional)
- Cache: Spring Cache with Redis
- TTL: 15 minutes
- Cached data:
  - Request summaries
  - Search results
  - User sessions

#### Cache Invalidation

**Strategies**:
- Time-based: TTL expiration
- Event-based: Invalidate on data change
- Manual: Admin invalidation

**Implementation**:
- @Cacheable: Cache method results
- @CacheEvict: Invalidate cache
- @CachePut: Update cache

---

## Data Models & Database Design

### Request Aggregate Data Model

**Request Entity** (JPA Entity)
```
@Entity
@Table(name = "access_requests")
class Request {
  @Id
  private UUID requestId;
  
  @Column(nullable = false)
  private UUID requestorId;
  
  @Column(nullable = false)
  private UUID accessTypeId;
  
  @Column(nullable = false)
  private String systemName;
  
  @Column(nullable = false, columnDefinition = "TEXT")
  private String justification;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RequestStatus status;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Instant createdAt;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Instant submittedAt;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Instant approvedAt;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Instant implementedAt;
  
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "request_id")
  private List<RequestApproval> approvals;
  
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "request_id")
  private List<RequestHistory> history;
  
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "request_id")
  private List<RequestDocument> documents;
}
```

**RequestApproval Entity** (JPA Entity)
```
@Entity
@Table(name = "request_approvals")
class RequestApproval {
  @Id
  private UUID approvalId;
  
  @Column(nullable = false)
  private UUID requestId;
  
  @Column(nullable = false)
  private UUID approverId;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApprovalType approvalType;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApprovalStatus status;
  
  @Column(columnDefinition = "TEXT")
  private String comments;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Instant createdAt;
}
```

**RequestHistory Entity** (JPA Entity)
```
@Entity
@Table(name = "request_history")
class RequestHistory {
  @Id
  private UUID historyId;
  
  @Column(nullable = false)
  private UUID requestId;
  
  @Column(nullable = false)
  private String action;
  
  @Column(nullable = false)
  private UUID actorId;
  
  @Enumerated(EnumType.STRING)
  private RequestStatus previousStatus;
  
  @Enumerated(EnumType.STRING)
  private RequestStatus newStatus;
  
  @Column(columnDefinition = "TEXT")
  private String comments;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Instant createdAt;
}
```

### AccessType Aggregate Data Model

**AccessType Entity** (JPA Entity)
```
@Entity
@Table(name = "access_types")
class AccessType {
  @Id
  private UUID accessTypeId;
  
  @Column(nullable = false, unique = true)
  private String name;
  
  @Column(columnDefinition = "TEXT")
  private String description;
  
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "access_type_id")
  private List<AccessTypeRouting> routingRules;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Instant createdAt;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Instant updatedAt;
}
```

**AccessTypeRouting Entity** (JPA Entity)
```
@Entity
@Table(name = "access_type_routing")
class AccessTypeRouting {
  @Id
  private UUID routingId;
  
  @Column(nullable = false)
  private UUID accessTypeId;
  
  @Column(nullable = false)
  private String administratorRole;
  
  @Column(nullable = false)
  private Boolean isDefault;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Instant createdAt;
}
```

### Data Transfer Objects (DTOs)

**RequestDTO**
```
class RequestDTO {
  String requestId;
  String requestorId;
  String requestorName;
  String accessType;
  String systemName;
  String justification;
  String status;
  Instant createdAt;
  Instant submittedAt;
  List<ApprovalDTO> approvals;
  List<HistoryDTO> history;
  List<DocumentDTO> documents;
}
```

**ApprovalDTO**
```
class ApprovalDTO {
  String approvalId;
  String approverId;
  String approverName;
  String approvalType;
  String status;
  String comments;
  Instant createdAt;
}
```

**AccessTypeDTO**
```
class AccessTypeDTO {
  String accessTypeId;
  String name;
  String description;
  List<RoutingRuleDTO> routingRules;
  Instant createdAt;
  Instant updatedAt;
}
```

---

## Event Handling Design

### Published Events

#### RequestCreated Event

**Trigger**: New request created
**Consumers**: Notification Service, Administration Service
**Payload**:
```json
{
  "eventId": "UUID",
  "eventType": "RequestCreated",
  "timestamp": "ISO-8601",
  "source": "request-management-service",
  "version": "1.0",
  "payload": {
    "requestId": "UUID",
    "requestorId": "UUID",
    "requestorName": "string",
    "requestorEmail": "string",
    "accessType": "string",
    "systemName": "string",
    "justification": "string",
    "officeId": "UUID",
    "createdAt": "ISO-8601"
  }
}
```

#### RequestSubmitted Event

**Trigger**: Request submitted for approval
**Consumers**: Notification Service, Administration Service
**Payload**: Similar structure with submission details

#### RequestApprovedByHeadOfOffice Event

**Trigger**: Head of Office approves request
**Consumers**: Notification Service, Administration Service
**Payload**: Includes approver details and next reviewer

#### RequestEndorsed Event

**Trigger**: SMD/RDC Reviewer endorses request
**Consumers**: Notification Service, Administration Service
**Payload**: Includes reviewer details and next approver

#### RequestFinallyApproved Event

**Trigger**: SMD/RDC Head approves request
**Consumers**: Notification Service, Administration Service
**Payload**: Includes approver details and assigned administrator

#### RequestDeclined Event

**Trigger**: Request declined at any stage
**Consumers**: Notification Service, Administration Service
**Payload**: Includes decliner details and decline reason

#### RequestReturned Event

**Trigger**: Request returned to reviewer
**Consumers**: Notification Service, Administration Service
**Payload**: Includes return reason and reviewer details

#### RequestImplemented Event

**Trigger**: Administrator marks request as implemented
**Consumers**: Notification Service, Administration Service
**Payload**: Includes implementation details and notes

#### AccessTypeAdded Event

**Trigger**: New access type created
**Consumers**: Administration Service
**Payload**: Includes access type details

#### AccessTypeRoutingConfigured Event

**Trigger**: Access type routing configured
**Consumers**: Administration Service
**Payload**: Includes routing configuration details

### Consumed Events

#### DocumentUploaded Event

**Source**: Document Management Service
**Handler**: DocumentUploadedEventHandler
**Action**: Update request with document metadata
**Idempotency**: Check if document already associated

#### DocumentDeleted Event

**Source**: Document Management Service
**Handler**: DocumentDeletedEventHandler
**Action**: Remove document reference from request
**Idempotency**: Check if document still associated

### Event Publishing Flow

```
1. Domain Aggregate publishes event
   ↓
2. Event added to aggregate's event list
   ↓
3. Application Service saves aggregate to repository
   ↓
4. Repository save triggers event publishing
   ↓
5. EventPublisher.publishAll(events) called
   ↓
6. Events converted to JSON
   ↓
7. Events published to Kafka topic
   ↓
8. Kafka broker persists events
   ↓
9. Consumer services receive events
   ↓
10. Event handlers process events
```

### Event Consumption Flow

```
1. Kafka listener receives message
   ↓
2. Message deserialized to event
   ↓
3. Event type determined
   ↓
4. Appropriate handler selected
   ↓
5. Idempotency check performed
   ↓
6. Handler processes event
   ↓
7. Event marked as processed
   ↓
8. Acknowledgment sent to Kafka
```

---

## Error Handling & Validation

### Input Validation

**Validation Framework**: Spring Validation with Hibernate Validator

**Validation Annotations**:
- @NotNull: Field must not be null
- @NotBlank: String must not be blank
- @Size: Collection or string size constraints
- @Min/@Max: Numeric range constraints
- @Pattern: Regex pattern matching
- @Email: Email format validation
- @UUID: UUID format validation

**Validation Layers**:
1. **Controller Level**: @Valid annotation on request body
2. **Service Level**: Manual validation for business rules
3. **Domain Level**: Aggregate invariant validation

**Example**:
```
@PostMapping("/requests")
public ResponseEntity<RequestDTO> createRequest(
  @Valid @RequestBody CreateRequestRequest request
) {
  // Validation performed automatically
  // If invalid, 400 Bad Request returned
}
```

### Business Rule Validation

**Policies**: Encapsulate business rules
- RequestApprovalPolicy: Approval rules
- RequestDeclinePolicy: Decline rules
- RequestRoutingPolicy: Routing rules
- AccessTypeRoutingPolicy: Access type routing rules

**Validation Flow**:
1. Input validation (format, constraints)
2. Business rule validation (policies)
3. State validation (aggregate invariants)
4. Authorization validation (roles, permissions)

### Error Handling

**Exception Hierarchy**:
```
Exception
├── RuntimeException
│   ├── ApplicationException
│   │   ├── ValidationException
│   │   ├── BusinessRuleException
│   │   ├── ResourceNotFoundException
│   │   ├── ConflictException
│   │   ├── UnauthorizedException
│   │   └── ForbiddenException
│   └── ExternalServiceException
│       ├── AdministrationServiceException
│       ├── DocumentManagementServiceException
│       └── NotificationServiceException
```

**Exception Handling**:
- Global exception handler (@ControllerAdvice)
- Converts exceptions to HTTP responses
- Logs all exceptions
- Returns standardized error response

**Error Response Format**:
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable message",
    "details": {
      "field": "error details"
    },
    "timestamp": "ISO-8601",
    "path": "/api/v1/requests"
  }
}
```

### Retry Strategy

**Retry Scenarios**:
- External service calls (Administration, Document Management)
- Kafka publishing failures
- Database connection issues

**Retry Configuration**:
- Max attempts: 3
- Initial delay: 1 second
- Backoff multiplier: 2 (exponential backoff)
- Max delay: 30 seconds

**Implementation**: Spring Retry or Resilience4j

### Circuit Breaker Pattern

**Purpose**: Prevent cascading failures

**States**:
- CLOSED: Normal operation
- OPEN: Service unavailable, fail fast
- HALF_OPEN: Testing if service recovered

**Configuration**:
- Failure threshold: 50% (5 failures out of 10 calls)
- Success threshold: 2 successful calls to close
- Timeout: 30 seconds before attempting half-open

**Implementation**: Resilience4j

---

## Security Design

### Authentication

**Mechanism**: JWT (JSON Web Tokens)

**Token Structure**:
```
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "userId",
  "name": "userName",
  "roles": ["EMPLOYEE", "HEAD_OF_OFFICE"],
  "iat": 1234567890,
  "exp": 1234571490
}

Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

**Token Validation**:
- Signature verification
- Expiration check
- Issuer verification
- Audience verification

**Implementation**:
- JwtAuthenticationFilter: Extracts token from Authorization header
- JwtTokenProvider: Validates and parses token
- UserDetailsService: Loads user details from token

### Authorization

**Role-Based Access Control (RBAC)**:
- Employee: Create requests, view own requests
- Head of Office: Approve/decline requests from team
- SMD/RDC Reviewer: Review and endorse requests
- SMD/RDC Head: Final approval and return requests
- System Admin: Implement OS/Web App access, manage access types
- DB Admin: Implement database access

**Authorization Implementation**:
- @PreAuthorize("hasRole('EMPLOYEE')"): Restrict by role
- @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')"): Multiple roles
- Custom authorization logic for complex rules

**Example**:
```
@PutMapping("/requests/{id}/approve")
@PreAuthorize("hasAnyRole('HEAD_OF_OFFICE', 'REVIEWER', 'HEAD')")
public ResponseEntity<RequestDTO> approveRequest(
  @PathVariable String id,
  @Valid @RequestBody ApproveRequestRequest request
) {
  // Only users with specified roles can access
}
```

### Data Protection

**Encryption at Rest**:
- Sensitive fields encrypted using AES-256
- Encryption keys stored in secure vault
- Database-level encryption (PostgreSQL pgcrypto)

**Encryption in Transit**:
- HTTPS/TLS 1.2+ for all API calls
- Kafka SSL/TLS for event communication
- Database SSL connections

**Password Security**:
- BCrypt hashing with salt
- Minimum 12 characters
- Complexity requirements (uppercase, lowercase, numbers, special chars)

**Audit Logging**:
- All user actions logged
- Immutable audit trail
- Includes: user, action, timestamp, resource, result
- Retention: 1 year (configurable)

### API Security

**Rate Limiting**:
- Per-user rate limit: 100 requests/minute
- Per-IP rate limit: 1000 requests/minute
- Implementation: Spring Cloud Gateway or custom filter

**CORS (Cross-Origin Resource Sharing)**:
- Allowed origins: Configurable
- Allowed methods: GET, POST, PUT, DELETE
- Allowed headers: Content-Type, Authorization
- Credentials: Allowed

**CSRF (Cross-Site Request Forgery) Protection**:
- CSRF tokens for state-changing operations
- Token validation on server side
- Implementation: Spring Security CSRF filter

---

## Performance & Scalability

### Database Optimization

**Query Optimization**:
- Indexes on frequently queried columns
- Composite indexes for common filter combinations
- Query analysis and optimization
- N+1 query prevention (eager loading where appropriate)

**Connection Pooling**:
- HikariCP with 10-20 connections
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes
- Max lifetime: 30 minutes

**Caching Strategy**:
- L1: Application cache (Caffeine) for access types
- L2: Distributed cache (Redis) for request summaries
- Cache invalidation on data changes

### API Performance

**Response Time Targets**:
- GET requests: < 200ms
- POST requests: < 500ms
- Complex queries: < 1 second

**Optimization Techniques**:
- Pagination for large result sets
- Lazy loading for related entities
- Projection queries for read-only data
- Asynchronous processing for long-running operations

### Scalability

**Horizontal Scaling**:
- Stateless services (no session affinity required)
- Load balancing across multiple instances
- Shared database for data consistency
- Kafka for asynchronous communication

**Vertical Scaling**:
- Increase CPU and memory for single instance
- Database optimization for larger datasets
- Caching to reduce database load

**Auto-Scaling**:
- Kubernetes HPA (Horizontal Pod Autoscaler)
- Scale based on CPU usage (70% threshold)
- Scale based on memory usage (80% threshold)
- Scale based on custom metrics (request rate)

### Asynchronous Processing

**Use Cases**:
- Event publishing (non-blocking)
- External service calls (with timeout)
- Long-running operations (background jobs)

**Implementation**:
- Spring @Async for background tasks
- Kafka for event-driven processing
- Message queues for job scheduling

---

## Testing Strategy

### Unit Testing

**Framework**: JUnit 5 with Mockito

**Coverage Target**: 80%+

**Test Categories**:
1. **Domain Model Tests**
   - Aggregate creation and state transitions
   - Value object validation
   - Invariant enforcement
   - Factory creation

2. **Service Tests**
   - Application service logic
   - Domain service behavior
   - Repository interactions (mocked)
   - Event publishing

3. **Repository Tests**
   - Query methods
   - Persistence operations
   - Specification implementations

4. **Validation Tests**
   - Input validation
   - Business rule validation
   - Policy enforcement

**Example Test**:
```
@Test
void testRequestApprovalTransition() {
  // Arrange
  Request request = RequestFactory.createRequest(...);
  request.submit(headOfOfficeId);
  
  // Act
  request.approveByHeadOfOffice(approverId, "Approved");
  
  // Assert
  assertEquals(RequestStatus.PENDING_REVIEW, request.getStatus());
  assertTrue(request.getApprovals().stream()
    .anyMatch(a -> a.getApproverId().equals(approverId)));
}
```

### Integration Testing

**Framework**: Spring Boot Test with TestContainers

**Test Categories**:
1. **API Integration Tests**
   - REST endpoint testing
   - Request/response validation
   - Error handling

2. **Database Integration Tests**
   - Repository operations
   - Transaction management
   - Data consistency

3. **Event Integration Tests**
   - Event publishing
   - Event consumption
   - Idempotency

4. **External Service Integration Tests**
   - Mock external services
   - Error scenarios
   - Timeout handling

**Example Test**:
```
@SpringBootTest
@Testcontainers
class RequestControllerIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgres = 
    new PostgreSQLContainer<>("postgres:13");
  
  @Test
  void testCreateRequest() {
    // Arrange
    CreateRequestRequest request = new CreateRequestRequest(...);
    
    // Act
    ResponseEntity<RequestDTO> response = 
      restTemplate.postForEntity("/api/v1/requests", request, RequestDTO.class);
    
    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody().getRequestId());
  }
}
```

### End-to-End Testing

**Framework**: Spring Boot Test with Testcontainers

**Test Scenarios**:
1. Complete request workflow (create → submit → approve → implement)
2. Decline scenarios at each stage
3. Return to reviewer scenario
4. Event publishing and consumption
5. External service integration

### Performance Testing

**Framework**: JMeter or Gatling

**Test Scenarios**:
- Load testing: 100 concurrent users
- Stress testing: Gradual increase to failure point
- Spike testing: Sudden traffic increase
- Endurance testing: Sustained load over time

**Metrics**:
- Response time (average, p95, p99)
- Throughput (requests/second)
- Error rate
- Resource utilization (CPU, memory)

### Security Testing

**Test Categories**:
1. **Authentication Tests**
   - Valid credentials
   - Invalid credentials
   - Expired tokens
   - Missing tokens

2. **Authorization Tests**
   - Role-based access
   - Resource ownership
   - Permission enforcement

3. **Input Validation Tests**
   - SQL injection attempts
   - XSS attempts
   - Invalid input formats

4. **Data Protection Tests**
   - Encryption verification
   - Secure password storage
   - Audit logging

---

## Deployment & Operations

### Deployment Architecture

#### Container Strategy

**Docker Image**:
- Base Image: openjdk:17-slim
- Multi-stage build for optimization
- Minimal image size
- Security scanning for vulnerabilities

**Dockerfile Structure**:
```
Stage 1: Build
- Maven build
- Run tests
- Create JAR

Stage 2: Runtime
- Copy JAR from build stage
- Set environment variables
- Expose port 8080
- Health check
```

#### Kubernetes Deployment

**Deployment Configuration**:
- Replicas: 3 (for high availability)
- Resource requests: CPU 500m, Memory 512Mi
- Resource limits: CPU 1000m, Memory 1024Mi
- Liveness probe: /actuator/health/liveness
- Readiness probe: /actuator/health/readiness

**Service Configuration**:
- Type: ClusterIP (internal) or LoadBalancer (external)
- Port: 8080
- Protocol: TCP

**ConfigMap**:
- Database connection string
- Kafka broker addresses
- External service URLs
- Logging configuration

**Secrets**:
- Database credentials
- JWT secret key
- API keys for external services
- SSL certificates

**Ingress**:
- Host: api.example.com
- Path: /api/v1/requests
- TLS: Enabled
- Rate limiting: 100 requests/minute per user

#### Database Deployment

**PostgreSQL**:
- Version: 13+
- Deployment: Managed service (AWS RDS, Azure Database, etc.)
- Backup: Daily automated backups
- Replication: Multi-AZ for high availability
- Monitoring: CloudWatch or equivalent

**Database Initialization**:
- Liquibase migrations on startup
- Schema creation
- Initial data loading
- Index creation

#### Kafka Deployment

**Kafka Cluster**:
- Brokers: 3 (for high availability)
- Replication factor: 2
- Partitions: 3 per topic
- Retention: 7 days

**Topics**:
- request.events: All request lifecycle events
- Partitions: 3
- Replication factor: 2

**Consumer Groups**:
- request-management-service: Consumes document events
- notification-service: Consumes all request events
- administration-service: Consumes all events

### Configuration Management

#### Environment-Specific Configuration

**Development**:
- Database: Local PostgreSQL
- Kafka: Local Kafka
- External services: Mocked
- Logging: DEBUG level
- Security: Disabled for testing

**Staging**:
- Database: Managed PostgreSQL
- Kafka: Managed Kafka
- External services: Real services
- Logging: INFO level
- Security: Enabled

**Production**:
- Database: Managed PostgreSQL with backups
- Kafka: Managed Kafka with monitoring
- External services: Real services
- Logging: WARN level
- Security: Fully enabled

#### Configuration Files

**application.properties**:
- Server port
- Database connection
- Kafka configuration
- Logging configuration
- Security settings

**application-dev.properties**:
- Development-specific overrides

**application-prod.properties**:
- Production-specific overrides

### Monitoring & Alerting

#### Metrics Collection

**Micrometer Integration**:
- Application metrics
- JVM metrics
- Database metrics
- HTTP metrics

**Metrics Exported**:
- Prometheus format
- CloudWatch
- Datadog

**Key Metrics**:
- Request count by status
- Request processing time by stage
- Approval rate
- Decline rate
- Event publishing/consumption rate
- API response times
- Database query performance
- Error rates
- JVM heap usage
- Thread count

#### Monitoring Dashboard

**Grafana Dashboards**:
1. **System Health Dashboard**
   - Service availability
   - Error rates
   - Response times
   - Resource utilization

2. **Business Metrics Dashboard**
   - Request count by status
   - Approval rate
   - Processing time by stage
   - Bottlenecks

3. **Infrastructure Dashboard**
   - Database performance
   - Kafka lag
   - Container resource usage
   - Network I/O

#### Alerting Rules

**Critical Alerts**:
- Service unavailable (HTTP 5xx > 5%)
- Database connection failures
- Kafka broker down
- High error rate (> 10%)

**Warning Alerts**:
- High response time (> 1 second)
- High memory usage (> 80%)
- High CPU usage (> 80%)
- Slow database queries (> 5 seconds)

**Info Alerts**:
- Deployment completed
- Configuration changed
- Backup completed

### Logging & Troubleshooting

#### Centralized Logging

**ELK Stack**:
- Elasticsearch: Log storage and indexing
- Logstash: Log processing and forwarding
- Kibana: Log visualization and analysis

**Log Aggregation**:
- All service logs sent to Elasticsearch
- Structured logging (JSON format)
- Log retention: 30 days

**Log Queries**:
- Search by request ID
- Search by user ID
- Search by error type
- Search by timestamp range

#### Troubleshooting Guide

**Common Issues**:
1. **Request stuck in pending state**
   - Check if approver has received notification
   - Verify approver has correct role
   - Check Kafka event publishing

2. **High response times**
   - Check database query performance
   - Check external service latency
   - Check cache hit rate

3. **Event processing failures**
   - Check Kafka consumer lag
   - Check event deserialization
   - Check idempotency handling

4. **Database connection issues**
   - Check connection pool status
   - Check database availability
   - Check network connectivity

### Backup & Recovery

#### Backup Strategy

**Database Backups**:
- Frequency: Daily automated backups
- Retention: 30 days
- Type: Full backup + incremental backups
- Location: Separate storage (S3, Azure Blob)

**Configuration Backups**:
- Kubernetes ConfigMaps and Secrets
- Frequency: On every change
- Version control: Git

**Event Backups**:
- Kafka topic retention: 7 days
- Backup to S3: Daily
- Retention: 90 days

#### Recovery Procedures

**Database Recovery**:
1. Identify backup to restore
2. Create new database instance
3. Restore from backup
4. Verify data integrity
5. Update connection string
6. Restart services

**Service Recovery**:
1. Identify failed service
2. Check logs for error
3. Fix issue
4. Redeploy service
5. Verify health checks
6. Monitor for issues

**Event Recovery**:
1. Identify failed event
2. Replay event from Kafka
3. Verify idempotency handling
4. Monitor for side effects

---

## Architecture Diagrams

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Client Applications                              │
│                    (Web Browser, Mobile App, etc.)                       │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ↓
                    ┌────────────────────────┐
                    │   API Gateway / LB     │
                    │  (Kubernetes Ingress)  │
                    └────────────┬───────────┘
                                 │
                    ┌────────────┴───────────┐
                    │                        │
                    ↓                        ↓
        ┌──────────────────────┐  ┌──────────────────────┐
        │  Request Management  │  │  Request Management  │
        │    Service Pod 1     │  │    Service Pod 2     │
        │  (Spring Boot App)   │  │  (Spring Boot App)   │
        └──────────┬───────────┘  └──────────┬───────────┘
                   │                         │
                   └────────────┬────────────┘
                                │
                    ┌───────────┴────────────┐
                    │                        │
                    ↓                        ↓
        ┌──────────────────────┐  ┌──────────────────────┐
        │   PostgreSQL DB      │  │   Kafka Cluster      │
        │  (Managed Service)   │  │  (3 Brokers)         │
        └──────────────────────┘  └──────────┬───────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    ↓                        ↓                        ↓
        ┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
        │  Notification        │  │  Document            │  │  Administration      │
        │  Service             │  │  Management Service  │  │  Service             │
        │  (Kafka Consumer)    │  │  (Kafka Consumer)    │  │  (Kafka Consumer)    │
        └──────────────────────┘  └──────────────────────┘  └──────────────────────┘
```

### Request Lifecycle Sequence Diagram

```
Employee          System           Head of Office    Reviewer         Head          Admin
   │                │                    │              │              │             │
   │─ Create Request─→│                   │              │              │             │
   │                │─ Save to DB        │              │              │             │
   │                │─ Publish Event     │              │              │             │
   │                │                    │              │              │             │
   │                │─ Send Notification─→              │              │             │
   │                │                    │              │              │             │
   │─ Submit Request─→│                   │              │              │             │
   │                │─ Update Status     │              │              │             │
   │                │─ Publish Event     │              │              │             │
   │                │                    │              │              │             │
   │                │─ Send Notification─→              │              │             │
   │                │                    │              │              │             │
   │                │                    │─ Approve    │              │             │
   │                │←─ Publish Event    │              │              │             │
   │                │                    │              │              │             │
   │                │─ Send Notification─────────────→ │              │             │
   │                │                    │              │              │             │
   │                │                    │              │─ Endorse    │             │
   │                │←─ Publish Event    │              │              │             │
   │                │                    │              │              │             │
   │                │─ Send Notification─────────────────────────────→ │             │
   │                │                    │              │              │             │
   │                │                    │              │              │─ Approve   │
   │                │←─ Publish Event    │              │              │             │
   │                │                    │              │              │             │
   │                │─ Send Notification─────────────────────────────────────────→ │
   │                │                    │              │              │             │
   │                │                    │              │              │             │─ Implement
   │                │←─ Publish Event    │              │              │             │
   │                │                    │              │              │             │
   │←─ Send Notification─────────────────────────────────────────────────────────────│
   │                │                    │              │              │             │
```

### Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Request Management Service                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ REST API Layer                                                   │   │
│  │ POST /api/v1/requests                                            │   │
│  │ GET /api/v1/requests/{id}                                        │   │
│  │ PUT /api/v1/requests/{id}/approve                                │   │
│  │ PUT /api/v1/requests/{id}/decline                                │   │
│  │ etc.                                                             │   │
│  └──────────────────┬───────────────────────────────────────────────┘   │
│                     │                                                    │
│                     ↓                                                    │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Application Layer                                                │   │
│  │ RequestApplicationService                                        │   │
│  │ RequestApprovalApplicationService                                │   │
│  │ AccessTypeApplicationService                                     │   │
│  └──────────────────┬───────────────────────────────────────────────┘   │
│                     │                                                    │
│                     ↓                                                    │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Domain Layer                                                     │   │
│  │ Request Aggregate                                                │   │
│  │ RequestWorkflowService                                           │   │
│  │ RequestRoutingService                                            │   │
│  │ Policies & Specifications                                        │   │
│  └──────────────────┬───────────────────────────────────────────────┘   │
│                     │                                                    │
│        ┌────────────┼────────────┐                                       │
│        │            │            │                                       │
│        ↓            ↓            ↓                                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐                             │
│  │Repository│ │EventPub  │ │ExternalSvc  │                             │
│  │(JPA)     │ │(Kafka)   │ │Client       │                             │
│  └────┬─────┘ └────┬─────┘ └──────┬───────┘                             │
│       │            │              │                                      │
│       ↓            ↓              ↓                                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐                             │
│  │PostgreSQL│ │Kafka     │ │Admin Service │                             │
│  │Database  │ │Topic     │ │Document Svc  │                             │
│  │          │ │          │ │Notification  │                             │
│  └──────────┘ └──────────┘ └──────────────┘                             │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Event Flow Diagram

```
Request Management Service
    │
    ├─ RequestCreated
    │  ├→ Notification Service (send confirmation)
    │  └→ Administration Service (audit log)
    │
    ├─ RequestSubmitted
    │  ├→ Notification Service (notify Head of Office)
    │  └→ Administration Service (audit log)
    │
    ├─ RequestApprovedByHeadOfOffice
    │  ├→ Notification Service (notify requestor & reviewer)
    │  └→ Administration Service (audit log)
    │
    ├─ RequestEndorsed
    │  ├→ Notification Service (notify SMD/RDC Head)
    │  └→ Administration Service (audit log)
    │
    ├─ RequestFinallyApproved
    │  ├→ Notification Service (notify administrator)
    │  └→ Administration Service (audit log)
    │
    ├─ RequestDeclined
    │  ├→ Notification Service (notify requestor)
    │  └→ Administration Service (audit log)
    │
    ├─ RequestReturned
    │  ├→ Notification Service (notify reviewer)
    │  └→ Administration Service (audit log)
    │
    ├─ RequestImplemented
    │  ├→ Notification Service (notify requestor)
    │  └→ Administration Service (audit log)
    │
    ├─ AccessTypeAdded
    │  └→ Administration Service (audit log)
    │
    └─ AccessTypeRoutingConfigured
       └→ Administration Service (audit log)

Consumed Events:
    │
    ├─ DocumentUploaded (from Document Management Service)
    │  └→ Update request with document metadata
    │
    └─ DocumentDeleted (from Document Management Service)
       └→ Remove document reference from request
```

### State Machine Diagram

```
                    ┌─────────────┐
                    │    Draft    │
                    └──────┬──────┘
                           │ submit()
                           ↓
                ┌──────────────────────────┐
                │ PendingInitialApproval   │
                └──────┬──────────────┬────┘
                       │              │
                       │ approve()    │ decline()
                       ↓              ↓
            ┌──────────────────┐  ┌──────────┐
            │  PendingReview   │  │ Declined │
            └──────┬───────┬───┘  └──────────┘
                   │       │
                   │       │ decline()
                   │       ↓
                   │    ┌──────────┐
                   │    │ Declined │
                   │    └──────────┘
                   │
                   │ endorse()
                   ↓
        ┌──────────────────────────┐
        │ PendingFinalApproval     │
        └──────┬──────────────┬────┘
               │              │
               │ approve()    │ return()
               ↓              ↓
        ┌──────────────┐  ┌──────────────────┐
        │  Approved    │  │ ReturnedToReviewer│
        └──────┬───────┘  └──────────────────┘
               │
               │ implement()
               ↓
        ┌──────────────┐
        │ Implemented  │
        └──────────────┘
```

---

## Summary

This logical design provides a comprehensive blueprint for implementing the Request Management Service using Java, Spring Boot, PostgreSQL, and Kafka. The design follows Domain-Driven Design principles with clear separation of concerns across presentation, application, domain, and infrastructure layers.

### Key Design Highlights

1. **Event-Driven Architecture**: Asynchronous communication via Kafka enables loose coupling and scalability
2. **Rich Domain Model**: Aggregates, entities, and value objects encapsulate business logic
3. **Layered Architecture**: Clear separation of concerns with well-defined responsibilities
4. **Comprehensive API Design**: RESTful endpoints with proper error handling and validation
5. **Security**: JWT authentication, RBAC authorization, and audit logging
6. **Scalability**: Stateless services, horizontal scaling, and caching strategies
7. **Reliability**: Retry logic, circuit breakers, and comprehensive error handling
8. **Observability**: Metrics, logging, and monitoring for operational visibility
9. **Testing**: Unit, integration, and end-to-end testing strategies
10. **Deployment**: Containerization with Docker and orchestration with Kubernetes

### Next Steps

1. **Implementation**: Use this design as blueprint for code implementation
2. **Database Schema**: Create PostgreSQL schema based on table designs
3. **API Development**: Implement REST endpoints according to API design
4. **Event Handling**: Set up Kafka topics and event handlers
5. **Testing**: Develop unit and integration tests
6. **Deployment**: Create Docker images and Kubernetes manifests
7. **Monitoring**: Set up Prometheus, Grafana, and ELK stack
8. **Documentation**: Generate API documentation from code

---

**Document Version**: 1.0
**Created**: January 8, 2025
**Status**: Complete - Ready for Implementation

