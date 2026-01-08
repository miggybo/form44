# Unit 2: Document Management Service - Logical Design

## Document Information
- **Service**: Document Management Service (Unit 2)
- **Purpose**: Manage document lifecycle with secure storage, access control, and audit trails
- **Technology Stack**: Java, Spring Boot, PostgreSQL, Kafka, Spring Security, Nutanix Object Storage, Redis
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
The Document Management Service manages all file operations for the Access Request Processing System, including uploading, storing, retrieving, and managing supporting documents (SAM, Justification) attached to access requests. This service ensures secure document storage, access control, and maintains a complete audit trail of document access.

### Scope
This logical design covers:
- Document upload, storage, and retrieval operations
- Document access control and authorization
- Document lifecycle management (Uploaded → Active → Archived/Deleted)
- File validation and integrity verification
- Document preview functionality (PDF)
- Event publishing and consumption
- REST API design for document operations
- Database schema and persistence strategy
- Integration with Request Management, Notification, and Administration services

### Key Responsibilities
1. Manage document lifecycle from upload through archival/deletion
2. Enforce document access control based on user roles
3. Store documents securely in Nutanix Object Storage
4. Maintain immutable audit trail of all document access
5. Validate documents during upload (format, size, content)
6. Provide REST APIs for document operations and queries
7. Publish domain events for document status changes
8. Consume request lifecycle events from Request Management Service

### Design Principles
- **Domain-Driven Design**: Rich domain model with aggregates, entities, and value objects
- **Event-Driven Architecture**: Asynchronous communication via Kafka message queue
- **Layered Architecture**: Clear separation of concerns (Presentation, Application, Domain, Infrastructure)
- **SOLID Principles**: Single responsibility, Open/closed, Liskov substitution, Interface segregation, Dependency inversion
- **Security First**: Role-based access control, immutable audit trails, secure storage
- **Scalability**: Stateless services, asynchronous processing, horizontal scaling support
- **Performance**: Caching with Redis, optimized queries, efficient file storage

---

## System Architecture

### High-Level System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Access Request Processing System                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │           Document Management Service (Unit 2)                   │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ REST API Layer (Spring MVC)                                │  │   │
│  │  │ - Document upload/download endpoints                       │  │   │
│  │  │ - Document metadata endpoints                              │  │   │
│  │  │ - Document access control endpoints                        │  │   │
│  │  │ - Document preview endpoints (PDF)                         │  │   │
│  │  │ - Document search endpoints                                │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Application Layer (Spring Services)                        │  │   │
│  │  │ - DocumentApplicationService                               │  │   │
│  │  │ - DocumentAccessApplicationService                         │  │   │
│  │  │ - DocumentValidationApplicationService                     │  │   │
│  │  │ - DocumentStorageApplicationService                        │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Domain Layer (DDD)                                         │  │   │
│  │  │ - Document Aggregate                                       │  │   │
│  │  │ - Domain Services                                          │  │   │
│  │  │ - Value Objects                                            │  │   │
│  │  │ - Domain Events                                            │  │   │
│  │  │ - Policies                                                 │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Infrastructure Layer                                       │  │   │
│  │  │ - DocumentRepository (JPA)                                 │  │   │
│  │  │ - EventPublisher (Kafka)                                   │  │   │
│  │  │ - EventListener (Kafka)                                    │  │   │
│  │  │ - NutanixStorageClient                                     │  │   │
│  │  │ - RedisCache                                               │  │   │
│  │  │ - Database (PostgreSQL)                                    │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ External Services (via REST APIs & Kafka Events)                │   │
│  │ - Request Management Service                                    │   │
│  │ - Notification Service                                          │   │
│  │ - Administration Service                                        │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Infrastructure Components                                        │   │
│  │ - Kafka Message Queue (Event Bus)                               │   │
│  │ - PostgreSQL Database                                           │   │
│  │ - Nutanix Object Storage                                        │   │
│  │ - Redis Cache                                                   │   │
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
Nutanix Object Storage (for file content)
    ↓
Redis Cache (for metadata)
    ↓
Event Publisher (Kafka)
    ↓
Kafka Topic (document.events)
    ↓
External Services (Request Management, Administration)
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
│ - Aggregates (Document)                                     │
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
│ - Nutanix Storage Client                                    │
│ - Redis Cache Client                                        │
│ - External Service Clients                                  │
│ - Security (Spring Security)                                │
│ - Logging & Monitoring                                      │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### Presentation Layer
- Receive HTTP requests from clients
- Validate input data (file format, size)
- Convert DTOs to domain objects
- Call application services
- Return HTTP responses with appropriate status codes
- Handle authentication/authorization

#### Application Layer
- Orchestrate domain logic
- Manage transactions
- Publish domain events
- Coordinate between aggregates and services
- Handle cross-cutting concerns
- Implement use cases (upload, download, delete, access control)

#### Domain Layer
- Encapsulate business rules
- Manage document aggregate state
- Validate invariants (file size, type, access control)
- Publish domain events
- Provide domain services (access control, validation, storage)
- Define specifications for queries

#### Infrastructure Layer
- Persist aggregates to PostgreSQL database
- Store file content in Nutanix Object Storage
- Cache metadata in Redis
- Publish/consume events via Kafka
- Integrate with external services
- Implement security policies
- Provide logging and monitoring


---

## API Layer Design

### REST API Endpoints

#### Document Management Endpoints

**1. Upload Document**
- **Endpoint**: `POST /api/v1/documents/upload`
- **Authentication**: Required (JWT Token)
- **Authorization**: Employee, Head of Office, Reviewer, Head, Administrator
- **Content-Type**: multipart/form-data
- **Request Parameters**:
  - `file`: Binary file content (required)
  - `requestId`: UUID of associated request (required)
  - `uploadedBy`: UUID of uploading user (required)
  - `documentType`: Type of document - SAM, Justification, Supporting (optional)
- **Response** (201 Created):
  ```
  {
    "documentId": "string (UUID)",
    "requestId": "string (UUID)",
    "fileName": "string",
    "fileSize": "number (bytes)",
    "fileType": "string (PDF|DOC|DOCX|JPG|PNG)",
    "mimeType": "string",
    "checksum": "string (SHA-256)",
    "uploadedBy": "string (UUID)",
    "uploadedAt": "ISO-8601 datetime",
    "status": "Uploaded"
  }
  ```
- **Error Responses**:
  - 400: Invalid file format, size exceeds 10MB, invalid request ID
  - 401: Unauthorized
  - 403: Forbidden (insufficient permissions)
  - 409: Conflict (duplicate file)
  - 413: Payload too large

**2. Download Document**
- **Endpoint**: `GET /api/v1/documents/{documentId}/download`
- **Authentication**: Required
- **Authorization**: Requestor, Approver, or Admin (based on access control)
- **Response** (200 OK):
  - Content-Type: application/octet-stream
  - Binary file content
  - Headers: Content-Disposition: attachment; filename="..."
- **Error Responses**:
  - 401: Unauthorized
  - 403: Forbidden (access denied)
  - 404: Document not found

**3. Get Document Metadata**
- **Endpoint**: `GET /api/v1/documents/{documentId}`
- **Authentication**: Required
- **Authorization**: Requestor, Approver, or Admin
- **Response** (200 OK):
  ```
  {
    "documentId": "string (UUID)",
    "requestId": "string (UUID)",
    "fileName": "string",
    "fileSize": "number (bytes)",
    "fileType": "string",
    "mimeType": "string",
    "checksum": "string (SHA-256)",
    "uploadedBy": "string (UUID)",
    "uploadedByName": "string",
    "uploadedAt": "ISO-8601 datetime",
    "status": "Uploaded|Active|Archived|Deleted",
    "accessControl": {
      "allowedRoles": ["string"],
      "isPublic": "boolean"
    },
    "accessLog": [
      {
        "accessId": "string (UUID)",
        "downloadedBy": "string (UUID)",
        "downloadedByName": "string",
        "downloadedAt": "ISO-8601 datetime",
        "accessType": "view|download|preview"
      }
    ]
  }
  ```

**4. List Documents for Request**
- **Endpoint**: `GET /api/v1/documents/request/{requestId}`
- **Authentication**: Required
- **Authorization**: Requestor, Approver, or Admin
- **Query Parameters**:
  - `status`: Filter by status (Uploaded, Active, Archived, Deleted)
  - `fileType`: Filter by file type (PDF, DOC, DOCX, JPG, PNG)
  - `page`: Page number (default: 1)
  - `size`: Page size (default: 20, max: 100)
  - `sortBy`: Sort field (uploadedAt, fileName, fileSize)
  - `sortOrder`: Sort order (ASC, DESC)
- **Response** (200 OK):
  ```
  {
    "requestId": "string (UUID)",
    "documents": [
      {
        "documentId": "string (UUID)",
        "fileName": "string",
        "fileSize": "number",
        "fileType": "string",
        "uploadedBy": "string",
        "uploadedAt": "ISO-8601 datetime",
        "status": "string"
      }
    ],
    "totalCount": "number",
    "page": "number",
    "size": "number",
    "totalPages": "number"
  }
  ```

**5. Delete Document**
- **Endpoint**: `DELETE /api/v1/documents/{documentId}`
- **Authentication**: Required
- **Authorization**: Requestor (own documents), Administrator
- **Request Body**:
  ```
  {
    "deletedBy": "string (UUID)",
    "reason": "string (optional)"
  }
  ```
- **Response** (200 OK):
  ```
  {
    "documentId": "string (UUID)",
    "status": "Deleted",
    "deletedAt": "ISO-8601 datetime"
  }
  ```
- **Error Responses**:
  - 401: Unauthorized
  - 403: Forbidden (cannot delete)
  - 404: Document not found

**6. Preview Document (PDF)**
- **Endpoint**: `GET /api/v1/documents/{documentId}/preview`
- **Authentication**: Required
- **Authorization**: Requestor, Approver, or Admin
- **Query Parameters**:
  - `page`: Page number (for multi-page PDFs, default: 1)
  - `format`: Preview format (image|pdf, default: image)
- **Response** (200 OK):
  - Content-Type: image/png or application/pdf
  - Binary preview content
- **Error Responses**:
  - 400: Document is not a PDF
  - 401: Unauthorized
  - 403: Forbidden (access denied)
  - 404: Document not found

**7. Get Document Status**
- **Endpoint**: `GET /api/v1/documents/request/{requestId}/status`
- **Authentication**: Required
- **Authorization**: Requestor, Approver, or Admin
- **Response** (200 OK):
  ```
  {
    "requestId": "string (UUID)",
    "hasAllDocuments": "boolean",
    "requiredDocuments": ["SAM", "Justification"],
    "attachedDocuments": [
      {
        "documentId": "string",
        "fileName": "string",
        "fileType": "string",
        "uploadedAt": "datetime"
      }
    ],
    "missingDocuments": ["string"]
  }
  ```

**8. Search Documents**
- **Endpoint**: `GET /api/v1/documents/search`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `q`: Search query (fileName, documentId)
  - `requestId`: Filter by request ID
  - `fileType`: Filter by file type
  - `uploadedBy`: Filter by uploader
  - `fromDate`: Date range start (ISO-8601)
  - `toDate`: Date range end (ISO-8601)
  - `status`: Filter by status
  - `page`: Page number
  - `size`: Page size
- **Response** (200 OK):
  ```
  {
    "results": [
      {
        "documentId": "string",
        "requestId": "string",
        "fileName": "string",
        "fileSize": "number",
        "fileType": "string",
        "uploadedBy": "string",
        "uploadedAt": "datetime",
        "status": "string"
      }
    ],
    "totalCount": "number"
  }
  ```

**9. Get Document Access Log**
- **Endpoint**: `GET /api/v1/documents/{documentId}/access-log`
- **Authentication**: Required
- **Authorization**: Document owner, Administrator
- **Query Parameters**:
  - `page`: Page number (default: 1)
  - `size`: Page size (default: 20)
  - `sortBy`: Sort field (downloadedAt)
  - `sortOrder`: Sort order (ASC, DESC)
- **Response** (200 OK):
  ```
  {
    "documentId": "string (UUID)",
    "accessLog": [
      {
        "accessId": "string (UUID)",
        "downloadedBy": "string (UUID)",
        "downloadedByName": "string",
        "downloadedAt": "ISO-8601 datetime",
        "accessType": "view|download|preview",
        "ipAddress": "string"
      }
    ],
    "totalCount": "number",
    "page": "number"
  }
  ```

**10. Archive Documents for Request**
- **Endpoint**: `PUT /api/v1/documents/request/{requestId}/archive`
- **Authentication**: Required
- **Authorization**: Administrator
- **Request Body**:
  ```
  {
    "archivedBy": "string (UUID)",
    "reason": "string (optional)"
  }
  ```
- **Response** (200 OK):
  ```
  {
    "requestId": "string (UUID)",
    "archivedDocuments": "number",
    "archivedAt": "ISO-8601 datetime"
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
    "path": "/api/v1/documents"
  }
}
```

### Common Error Codes

- `INVALID_FILE_FORMAT`: File format not allowed
- `FILE_SIZE_EXCEEDED`: File size exceeds 10MB limit
- `INVALID_FILE_CONTENT`: File content does not match declared type
- `INVALID_INPUT`: Input validation failed
- `UNAUTHORIZED`: Authentication required
- `FORBIDDEN`: Insufficient permissions or access denied
- `NOT_FOUND`: Document or request not found
- `CONFLICT`: Resource conflict (e.g., duplicate file)
- `STORAGE_ERROR`: Error storing file in Nutanix Object Storage
- `INTERNAL_ERROR`: Internal server error


---

## Application Layer Design

### Application Services

The Application Layer contains Spring Services that orchestrate domain logic and manage transactions.

#### DocumentApplicationService

**Responsibilities**:
- Manage document upload operations
- Query documents with various filters
- Coordinate between domain services and repositories
- Manage transactions
- Cache document metadata in Redis

**Key Methods**:
- `uploadDocument(UploadDocumentCommand): DocumentDTO`
  - Validates file format and size
  - Calls DocumentValidationService to validate content
  - Calls DocumentStorageService to store file in Nutanix
  - Calls DocumentFactory to create aggregate
  - Saves to repository
  - Caches metadata in Redis
  - Publishes DocumentUploaded event
  - Returns DocumentDTO

- `getDocument(documentId): DocumentDTO`
  - Checks Redis cache first
  - If not cached, queries repository
  - Caches result in Redis
  - Converts aggregate to DTO
  - Returns DocumentDTO

- `listDocumentsForRequest(requestId, filters): Page<DocumentDTO>`
  - Applies filters and pagination
  - Uses DocumentRepository specifications
  - Returns paginated results

- `searchDocuments(SearchQuery): List<DocumentDTO>`
  - Performs full-text search
  - Applies filters
  - Returns search results

- `deleteDocument(DeleteDocumentCommand): DocumentDTO`
  - Validates delete permissions
  - Calls DocumentStorageService to delete file
  - Updates document status to Deleted
  - Saves updated document
  - Invalidates Redis cache
  - Publishes DocumentDeleted event
  - Returns updated DocumentDTO

#### DocumentAccessApplicationService

**Responsibilities**:
- Handle document access control
- Manage access logging
- Enforce authorization policies

**Key Methods**:
- `canAccessDocument(documentId, userId, accessType): boolean`
  - Calls DocumentAccessService to check permissions
  - Logs access attempt
  - Returns authorization result

- `downloadDocument(documentId, userId): FileContent`
  - Validates access permissions
  - Calls DocumentStorageService to retrieve file
  - Logs download in access log
  - Publishes DocumentDownloaded event
  - Returns file content

- `previewDocument(documentId, userId, page): PreviewContent`
  - Validates access permissions
  - Validates document is PDF
  - Generates preview image
  - Logs preview access
  - Returns preview content

- `getAccessLog(documentId): List<DocumentAccessEntry>`
  - Queries access log from repository
  - Returns access history

#### DocumentValidationApplicationService

**Responsibilities**:
- Validate documents during upload
- Manage validation rules
- Generate file checksums

**Key Methods**:
- `validateDocument(file): ValidationResult`
  - Calls DocumentValidationService to validate format
  - Calls DocumentValidationService to validate size
  - Calls DocumentValidationService to validate content
  - Returns validation result

- `generateChecksum(fileContent): String`
  - Calls DocumentValidationService to generate SHA-256 checksum
  - Returns checksum

#### DocumentStorageApplicationService

**Responsibilities**:
- Manage file storage operations
- Coordinate with Nutanix Object Storage
- Handle storage errors

**Key Methods**:
- `storeDocument(documentId, fileContent): StoragePath`
  - Calls DocumentStorageService to store in Nutanix
  - Handles storage errors with retry logic
  - Returns storage path

- `retrieveDocument(documentId): FileContent`
  - Calls DocumentStorageService to retrieve from Nutanix
  - Handles retrieval errors
  - Returns file content

- `deleteDocument(documentId): void`
  - Calls DocumentStorageService to delete from Nutanix
  - Handles deletion errors

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
- Publishes to Kafka topic `document.events`
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

- Listens to Kafka topic `document.events` (for consumed events)
- Deserializes JSON to domain events
- Routes events to appropriate handlers
- Implements idempotency (prevents duplicate processing)
- Logs all consumed events

#### Consumed Events

**RequestCreated** (from Request Management Service)
- Handler: RequestCreatedEventHandler
- Action: Initialize document tracking for request
- Idempotency: Check if request already tracked

**RequestDeclined** (from Request Management Service)
- Handler: RequestDeclinedEventHandler
- Action: Archive documents for declined request
- Idempotency: Check if documents already archived

**RequestImplemented** (from Request Management Service)
- Handler: RequestImplementedEventHandler
- Action: Archive documents for completed request
- Idempotency: Check if documents already archived

### Transaction Management

#### Transaction Boundaries

- Each application service method is a transaction boundary
- Uses Spring @Transactional annotation
- Propagation: REQUIRED (join existing or create new)
- Isolation: READ_COMMITTED (default)
- Rollback on RuntimeException

#### Saga Pattern for Distributed Transactions

For operations spanning multiple services:
1. Document Management publishes event
2. Request Management consumes event and performs action
3. Request Management publishes completion event
4. Document Management consumes completion event
5. If any step fails, compensating transactions are triggered

### Command & Query Separation (CQRS)

#### Commands (Write Operations)

- UploadDocumentCommand
- DeleteDocumentCommand
- ArchiveDocumentsCommand
- UpdateAccessControlCommand

#### Queries (Read Operations)

- GetDocumentQuery
- ListDocumentsQuery
- SearchDocumentsQuery
- GetAccessLogQuery
- GetDocumentStatusQuery

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

- **Cache Key Pattern**: `document:{documentId}`, `request-documents:{requestId}`
- **TTL**: 1 hour for document metadata, 30 minutes for access logs
- **Invalidation**: On document update, delete, or archive
- **Serialization**: JSON format

#### Cache Layers

1. **Document Metadata Cache**: Caches document details (fileName, fileSize, status, etc.)
2. **Access Control Cache**: Caches access control rules for documents
3. **Request Documents Cache**: Caches list of documents for a request

#### Cache Invalidation Strategy

- Invalidate on document update
- Invalidate on document delete
- Invalidate on document archive
- Invalidate on access control change
- Automatic expiration after TTL


---

## Domain Layer Design

### Aggregate Design

#### Document Aggregate

**Aggregate Root**: Document
- **Identifier**: DocumentId (UUID)
- **Lifecycle**: Uploaded → Active → Archived/Deleted
- **Invariants**:
  - Document must have valid file name
  - Document must have valid file type
  - Document size must not exceed 10MB
  - Document must be associated with a request
  - Document must have an uploader
  - Document cannot be accessed if status is Deleted
  - All document access must be logged

**Entities within Aggregate**:
- DocumentAccessLog: Tracks all document access events (immutable, append-only)

**Value Objects within Aggregate**:
- DocumentId: Unique identifier (UUID)
- FileName: File name with validation (immutable)
- FileSize: File size in bytes with max constraint (immutable)
- FileType: Enumeration of allowed types (PDF, DOC, DOCX, JPG, PNG) (immutable)
- DocumentStatus: State machine with validation (Uploaded, Active, Archived, Deleted)
- DocumentAccessControl: Access rules (allowedRoles, isPublic) (immutable)
- FileMetadata: File information (mimeType, encoding, checksum) (immutable)
- DocumentUploadInfo: Upload information (uploadedBy, uploadedAt, uploadPath) (immutable)
- DocumentAccessEntry: Single access log entry (immutable)

**Aggregate Behavior**:
- `activate()`: Transition from Uploaded to Active
- `archive()`: Transition to Archived
- `delete()`: Transition to Deleted
- `canAccess(userId, accessType): boolean`: Check access permissions
- `logAccess(userId, accessType): void`: Record access in log
- `updateAccessControl(roles, isPublic): void`: Update access rules

### Domain Services

#### DocumentAccessService

**Purpose**: Enforce document access control and authorization

**Methods**:
- `canAccessDocument(documentId, userId, accessType): boolean`
  - Validates user has permission to access document
  - Checks role-based access control
  - Returns authorization result

- `grantAccess(documentId, userId, accessType): void`
  - Records access grant
  - Logs access event

- `denyAccess(documentId, userId, reason): void`
  - Records access denial
  - Publishes DocumentAccessDenied event

- `logAccess(documentId, userId, accessType): void`
  - Creates immutable access log entry
  - Appends to document access log

#### DocumentValidationService

**Purpose**: Validate documents during upload

**Methods**:
- `validateFileFormat(fileName, fileType): ValidationResult`
  - Validates file extension against allowed types
  - Returns validation result

- `validateFileSize(fileSize): ValidationResult`
  - Validates file size does not exceed 10MB
  - Returns validation result

- `validateFileContent(fileContent, declaredType): ValidationResult`
  - Validates file content matches declared type
  - Checks file signature/magic bytes
  - Returns validation result

- `generateChecksum(fileContent): String`
  - Generates SHA-256 checksum
  - Returns checksum string

#### DocumentStorageService

**Purpose**: Manage physical document storage in Nutanix Object Storage

**Methods**:
- `storeDocument(documentId, fileContent): StoragePath`
  - Stores file in Nutanix Object Storage
  - Generates storage path
  - Handles storage errors with retry logic
  - Returns storage path

- `retrieveDocument(documentId): FileContent`
  - Retrieves file from Nutanix Object Storage
  - Handles retrieval errors
  - Returns file content

- `deleteDocument(documentId): void`
  - Deletes file from Nutanix Object Storage
  - Handles deletion errors

- `generatePreview(documentId, page): PreviewContent`
  - Generates PDF preview image
  - Caches preview in Redis
  - Returns preview content

### Repositories

#### DocumentRepository

**Purpose**: Persist and retrieve Document aggregates

**Responsibilities**:
- Save new documents
- Update existing documents
- Query documents by various criteria
- Maintain document access logs

**Query Methods**:
- `findById(documentId): Document`
- `findByRequest(requestId): List<Document>`
- `findByType(fileType): List<Document>`
- `findByUploadDate(startDate, endDate): List<Document>`
- `findByUploader(uploaderId): List<Document>`
- `search(criteria): List<Document>`
- `findActiveDocumentsForRequest(requestId): List<Document>`

**Persistence Methods**:
- `save(document): void`
- `update(document): void`
- `delete(documentId): void`
- `archiveDocumentsForRequest(requestId): void`

### Specifications (Query Objects)

#### DocumentsByRequestSpecification
- **Purpose**: Query documents for a specific request
- **Criteria**: requestId
- **Returns**: List<Document>

#### DocumentsByTypeSpecification
- **Purpose**: Query documents by file type
- **Criteria**: fileType
- **Returns**: List<Document>

#### DocumentsByUploadDateSpecification
- **Purpose**: Query documents uploaded within a date range
- **Criteria**: startDate, endDate
- **Returns**: List<Document>

#### ActiveDocumentsForRequestSpecification
- **Purpose**: Query active documents for a request
- **Criteria**: requestId, status (Active)
- **Returns**: List<Document>

#### DocumentsByUploaderSpecification
- **Purpose**: Query documents uploaded by a specific user
- **Criteria**: uploaderId
- **Returns**: List<Document>

### Policies

#### DocumentAccessPolicy

**Purpose**: Define who can access which documents

**Rules**:
- Only authorized users can download documents
- Employees can only access their own request documents
- Approvers can access documents for requests they review
- Administrators can access all documents
- Access attempts are logged
- Denied access triggers security alert

**Access Levels**:
- View: Can see document metadata
- Download: Can download document
- Preview: Can preview document (PDF only)

**Validation Methods**:
- `canViewDocument(documentId, userId): boolean`
- `canDownloadDocument(documentId, userId): boolean`
- `canPreviewDocument(documentId, userId): boolean`
- `validateAccessRequest(documentId, userId, accessType): ValidationResult`

#### DocumentValidationPolicy

**Purpose**: Define document validation rules

**Rules**:
- Allowed file types: PDF, DOC, DOCX, JPG, PNG
- Maximum file size: 10MB per file
- File name must not be empty
- File name must not contain invalid characters
- File content must match declared file type
- Duplicate files are allowed (same content, different requests)

**Validation Methods**:
- `isValidFileType(fileType): boolean`
- `isValidFileSize(fileSize): boolean`
- `isValidFileName(fileName): boolean`
- `validateDocument(document): ValidationResult`

#### DocumentRetentionPolicy

**Purpose**: Define document retention and archival rules

**Rules**:
- Active documents are retained while request is active
- Documents are archived when request is declined or implemented
- Archived documents are retained for compliance period (configurable)
- Deleted documents are permanently removed

**Archival Methods**:
- `archiveDocument(documentId): void`
- `deleteDocument(documentId): void`
- `isRetentionExpired(documentId): boolean`

### Factory Patterns

#### DocumentFactory

**Purpose**: Create new Document aggregates with validation

**Responsibilities**:
- Validate file format and size
- Generate unique DocumentId
- Create document with proper access control
- Initialize document in Uploaded status

**Creation Method**:
- `createDocument(requestId, fileName, fileSize, fileType, uploadedBy): Document`
  - Validates file format
  - Validates file size
  - Generates DocumentId
  - Creates access control rules
  - Returns new Document aggregate in Uploaded status

#### DocumentAccessLogFactory

**Purpose**: Create DocumentAccessLog entries

**Responsibilities**:
- Create immutable access log entries
- Capture access information
- Timestamp all entries

**Creation Method**:
- `createAccessLogEntry(documentId, userId, accessType): DocumentAccessEntry`
  - Captures current timestamp
  - Creates immutable entry
  - Returns DocumentAccessEntry

#### DocumentValidationRulesFactory

**Purpose**: Create DocumentValidationRules with default configuration

**Responsibilities**:
- Initialize validation rules
- Set allowed file types
- Set file size limits
- Set allowed MIME types

**Creation Method**:
- `createDefaultValidationRules(): DocumentValidationRules`
  - Sets allowed extensions: [PDF, DOC, DOCX, JPG, PNG]
  - Sets max file size: 10MB
  - Sets allowed MIME types
  - Returns DocumentValidationRules

### Domain Events

#### Published Events

**DocumentUploaded**
- **Trigger**: When a document is successfully uploaded
- **Payload**: documentId, requestId, fileName, fileSize, fileType, uploadedBy, uploadedAt
- **Subscribers**: RequestManagementService (update request), AdministrationService (audit log)

**DocumentDownloaded**
- **Trigger**: When a document is downloaded
- **Payload**: documentId, requestId, downloadedBy, downloadedAt
- **Subscribers**: AdministrationService (audit log)

**DocumentDeleted**
- **Trigger**: When a document is deleted
- **Payload**: documentId, requestId, deletedBy, deletedAt
- **Subscribers**: RequestManagementService (update request), AdministrationService (audit log)

**DocumentAccessDenied**
- **Trigger**: When unauthorized access is attempted
- **Payload**: documentId, attemptedBy, attemptedAt, reason
- **Subscribers**: AdministrationService (audit log, security alert)

**DocumentValidationFailed**
- **Trigger**: When document validation fails during upload
- **Payload**: fileName, fileSize, fileType, reason
- **Subscribers**: AdministrationService (audit log)

#### Consumed Events

**RequestCreated** (from Request Management Service)
- **Used to**: Initialize document tracking for request
- **Action**: Create document container for request

**RequestDeclined** (from Request Management Service)
- **Used to**: Archive documents for declined request
- **Action**: Change document status to Archived

**RequestImplemented** (from Request Management Service)
- **Used to**: Archive documents for completed request
- **Action**: Change document status to Archived


---

## Infrastructure Layer Design

### Repository Implementation

#### DocumentRepository (JPA/Hibernate)

**Implementation Strategy**:
- Uses Spring Data JPA for database access
- Implements custom query methods using JPQL or native SQL
- Supports pagination and sorting
- Implements specifications pattern for complex queries

**Key Implementation Details**:
- Entity mapping: Document aggregate → JPA entities
- Lazy loading for access logs (performance optimization)
- Indexes on frequently queried fields (requestId, uploadedBy, status)
- Cascade operations for access logs

**Query Optimization**:
- Use database indexes for requestId, uploadedBy, status, uploadedAt
- Implement query result caching in Redis
- Use pagination for large result sets
- Avoid N+1 query problems with eager loading

### Event Bus Implementation

#### Kafka Event Publisher

**Configuration**:
- Topic: `document.events`
- Partitions: 3 (for scalability)
- Replication Factor: 2 (for reliability)
- Retention: 7 days

**Publishing Flow**:
1. Application service calls EventPublisher.publishAll()
2. EventPublisher converts domain events to JSON
3. EventPublisher publishes to Kafka topic
4. Kafka broker stores message
5. External services consume from topic

**Error Handling**:
- Retry logic with exponential backoff (1s, 5s, 15s)
- Dead letter queue for permanently failed messages
- Logging of all publish attempts

#### Kafka Event Listener

**Configuration**:
- Consumer Group: `document-management-service`
- Topics: `request.events` (for consumed events)
- Auto-offset Reset: earliest

**Consumption Flow**:
1. Kafka listener receives message from topic
2. Deserializes JSON to domain event
3. Routes to appropriate event handler
4. Handler processes event
5. Implements idempotency check
6. Logs consumption

**Idempotency Strategy**:
- Store processed event IDs in database
- Check if event already processed before handling
- Use event ID as unique constraint

### Nutanix Object Storage Integration

#### NutanixStorageClient

**Configuration**:
- Endpoint: Nutanix Object Storage API endpoint
- Authentication: API key/secret
- Bucket: `access-request-documents`
- Region: Configured per environment

**Storage Operations**:
- `uploadObject(documentId, fileContent): StoragePath`
  - Uploads file to Nutanix bucket
  - Generates object key: `documents/{requestId}/{documentId}/{fileName}`
  - Returns storage path

- `downloadObject(documentId): FileContent`
  - Downloads file from Nutanix bucket
  - Handles 404 errors (file not found)
  - Returns file content

- `deleteObject(documentId): void`
  - Deletes file from Nutanix bucket
  - Handles deletion errors

**Error Handling**:
- Retry logic for transient failures
- Exponential backoff
- Circuit breaker pattern for persistent failures
- Fallback to local storage if configured

**Security**:
- Use HTTPS for all connections
- Encrypt files at rest in Nutanix
- Implement access control at storage level
- Audit all storage operations

### Redis Cache Integration

#### RedisCache Configuration

**Configuration**:
- Host: Redis server hostname
- Port: 6379
- Database: 0
- TTL: Configurable per cache type

**Cache Types**:

1. **Document Metadata Cache**
   - Key: `document:{documentId}`
   - Value: Document metadata (fileName, fileSize, status, etc.)
   - TTL: 1 hour

2. **Access Control Cache**
   - Key: `access-control:{documentId}:{userId}`
   - Value: Access permission (true/false)
   - TTL: 30 minutes

3. **Request Documents Cache**
   - Key: `request-documents:{requestId}`
   - Value: List of document IDs for request
   - TTL: 30 minutes

4. **PDF Preview Cache**
   - Key: `preview:{documentId}:{page}`
   - Value: Preview image binary
   - TTL: 24 hours

**Cache Invalidation**:
- On document update: Invalidate metadata cache
- On document delete: Invalidate all related caches
- On access control change: Invalidate access control cache
- Automatic expiration after TTL

**Serialization**:
- Use JSON for metadata caches
- Use binary format for preview cache
- Implement custom serializers for complex objects

### Database Access Layer

#### PostgreSQL Database

**Tables Owned by Document Management Service**:

1. **documents**
   - Columns: documentId (PK), requestId (FK), fileName, fileSize, fileType, mimeType, checksum, uploadedBy (FK), uploadedAt, status, accessControl, storagePath, createdAt, updatedAt
   - Indexes: requestId, uploadedBy, status, uploadedAt
   - Constraints: NOT NULL on required fields, UNIQUE on documentId

2. **document_access_log**
   - Columns: accessId (PK), documentId (FK), downloadedBy (FK), downloadedAt, accessType, ipAddress, createdAt
   - Indexes: documentId, downloadedBy, downloadedAt
   - Constraints: NOT NULL on required fields, APPEND-ONLY (no updates/deletes)

**Relationships**:
- documents.requestId → access_requests.requestId (Request Management Service)
- documents.uploadedBy → users.userId (Administration Service)
- document_access_log.downloadedBy → users.userId (Administration Service)

**Data Integrity**:
- Foreign key constraints for referential integrity
- Check constraints for valid status values
- Unique constraints for documentId
- NOT NULL constraints for required fields

### External Service Clients

#### RequestManagementServiceClient

**Purpose**: Communicate with Request Management Service

**Methods**:
- `getRequest(requestId): RequestDTO`
  - Retrieves request details
  - Used for validation and access control

- `updateRequestDocuments(requestId, documents): void`
  - Updates request with document information
  - Called after document upload/delete

#### AdministrationServiceClient

**Purpose**: Communicate with Administration Service

**Methods**:
- `getUserRoles(userId): List<String>`
  - Retrieves user roles
  - Used for access control

- `getOfficeHierarchy(officeId): OfficeHierarchy`
  - Retrieves office hierarchy
  - Used for access control

- `logAuditEvent(event): void`
  - Logs audit event
  - Called for all document operations

### Security Implementation

#### Spring Security Configuration

**Authentication**:
- JWT token validation
- Token extraction from Authorization header
- Token expiration validation

**Authorization**:
- Role-based access control (RBAC)
- Method-level security with @PreAuthorize
- Custom authorization logic for document access

**CORS Configuration**:
- Allow requests from trusted origins
- Allow credentials in requests
- Allow specific HTTP methods (GET, POST, PUT, DELETE)

**Security Headers**:
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Strict-Transport-Security: max-age=31536000

### Logging and Monitoring

#### Logging Strategy

**Log Levels**:
- ERROR: Errors and exceptions
- WARN: Warnings and potential issues
- INFO: Important business events
- DEBUG: Detailed debugging information

**Log Events**:
- Document upload: fileName, fileSize, uploadedBy, timestamp
- Document download: documentId, downloadedBy, timestamp
- Document deletion: documentId, deletedBy, timestamp
- Access denied: documentId, attemptedBy, reason, timestamp
- Validation failures: fileName, reason, timestamp

**Log Aggregation**:
- Centralized logging with ELK stack or similar
- Structured logging with JSON format
- Correlation IDs for tracing requests

#### Monitoring Strategy

**Metrics**:
- Document upload count (per hour/day)
- Document download count (per hour/day)
- Average file size
- Storage usage
- Cache hit rate
- API response times
- Error rates

**Alerts**:
- High error rate (>5%)
- Storage usage >80%
- Cache hit rate <50%
- API response time >1000ms
- Kafka consumer lag >1000 messages

**Health Checks**:
- Database connectivity
- Nutanix Object Storage connectivity
- Redis connectivity
- Kafka connectivity


---

## Data Models & Database Design

### Database Schema

#### documents Table

```
Column Name          | Data Type      | Constraints           | Description
---------------------|----------------|----------------------|----------------------------------
documentId           | UUID           | PRIMARY KEY           | Unique document identifier
requestId            | UUID           | FOREIGN KEY, NOT NULL | Reference to access request
fileName             | VARCHAR(255)   | NOT NULL              | Original file name
fileSize             | BIGINT         | NOT NULL, CHECK >0    | File size in bytes
fileType             | VARCHAR(10)    | NOT NULL              | File type (PDF, DOC, etc.)
mimeType             | VARCHAR(100)   | NOT NULL              | MIME type
checksum             | VARCHAR(64)    | NOT NULL              | SHA-256 checksum
uploadedBy           | UUID           | FOREIGN KEY, NOT NULL | User who uploaded
uploadedAt           | TIMESTAMP      | NOT NULL              | Upload timestamp
status               | VARCHAR(20)    | NOT NULL              | Status (Uploaded, Active, etc.)
accessControl        | JSONB          | NOT NULL              | Access control rules
storagePath          | VARCHAR(500)   | NOT NULL              | Path in Nutanix storage
createdAt            | TIMESTAMP      | NOT NULL              | Record creation time
updatedAt            | TIMESTAMP      | NOT NULL              | Last update time

Indexes:
- PRIMARY KEY (documentId)
- FOREIGN KEY (requestId) → access_requests(requestId)
- FOREIGN KEY (uploadedBy) → users(userId)
- INDEX (requestId)
- INDEX (uploadedBy)
- INDEX (status)
- INDEX (uploadedAt)
```

#### document_access_log Table

```
Column Name          | Data Type      | Constraints           | Description
---------------------|----------------|----------------------|----------------------------------
accessId             | UUID           | PRIMARY KEY           | Unique access log entry ID
documentId           | UUID           | FOREIGN KEY, NOT NULL | Reference to document
downloadedBy         | UUID           | FOREIGN KEY, NOT NULL | User who accessed
downloadedAt         | TIMESTAMP      | NOT NULL              | Access timestamp
accessType           | VARCHAR(20)    | NOT NULL              | Access type (view, download, preview)
ipAddress            | VARCHAR(45)    | NOT NULL              | IP address of accessor
createdAt            | TIMESTAMP      | NOT NULL              | Record creation time

Indexes:
- PRIMARY KEY (accessId)
- FOREIGN KEY (documentId) → documents(documentId)
- FOREIGN KEY (downloadedBy) → users(userId)
- INDEX (documentId)
- INDEX (downloadedBy)
- INDEX (downloadedAt)

Constraints:
- APPEND-ONLY: No UPDATE or DELETE operations allowed
```

### Entity-Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    documents                                 │
├─────────────────────────────────────────────────────────────┤
│ documentId (PK)                                              │
│ requestId (FK) → access_requests.requestId                  │
│ fileName                                                     │
│ fileSize                                                     │
│ fileType                                                     │
│ mimeType                                                     │
│ checksum                                                     │
│ uploadedBy (FK) → users.userId                              │
│ uploadedAt                                                   │
│ status                                                       │
│ accessControl (JSONB)                                        │
│ storagePath                                                  │
│ createdAt                                                    │
│ updatedAt                                                    │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 1:N
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              document_access_log                             │
├─────────────────────────────────────────────────────────────┤
│ accessId (PK)                                                │
│ documentId (FK) → documents.documentId                       │
│ downloadedBy (FK) → users.userId                             │
│ downloadedAt                                                 │
│ accessType                                                   │
│ ipAddress                                                    │
│ createdAt                                                    │
└─────────────────────────────────────────────────────────────┘
```

### Performance Considerations

**Indexing Strategy**:
- Index on requestId for fast document lookup by request
- Index on uploadedBy for user's documents query
- Index on status for filtering by document status
- Index on uploadedAt for date range queries
- Composite index on (requestId, status) for common queries

**Query Optimization**:
- Use pagination for large result sets
- Implement query result caching in Redis
- Use database connection pooling
- Monitor slow queries with query logs

**Storage Optimization**:
- Archive old documents to reduce active table size
- Partition documents table by uploadedAt for better performance
- Use JSONB for flexible access control storage

---

## Event Handling Design

### Event Flow Diagram

```
Document Upload
    ↓
DocumentUploaded Event
    ├→ RequestManagementService (update request)
    └→ AdministrationService (audit log)
    ↓
Document Access
    ↓
DocumentDownloaded Event
    └→ AdministrationService (audit log)
    ↓
Document Deletion
    ↓
DocumentDeleted Event
    ├→ RequestManagementService (update request)
    └→ AdministrationService (audit log)
    ↓
Unauthorized Access Attempt
    ↓
DocumentAccessDenied Event
    └→ AdministrationService (audit log, security alert)
    ↓
Validation Failure
    ↓
DocumentValidationFailed Event
    └→ AdministrationService (audit log)
```

### Event Serialization

**Event JSON Format**:
```json
{
  "eventId": "uuid",
  "eventType": "DocumentUploaded",
  "timestamp": "ISO-8601 datetime",
  "source": "document-management-service",
  "version": "1.0",
  "payload": {
    "documentId": "uuid",
    "requestId": "uuid",
    "fileName": "string",
    "fileSize": "number",
    "fileType": "string",
    "uploadedBy": "uuid",
    "uploadedAt": "ISO-8601 datetime"
  }
}
```

### Idempotency Strategy

**Idempotent Event Processing**:
1. Store processed event IDs in database table `processed_events`
2. Before processing event, check if already processed
3. If already processed, skip processing (idempotent)
4. If not processed, process and store event ID

**Processed Events Table**:
```
Column Name          | Data Type      | Constraints
---------------------|----------------|----------------------
eventId              | UUID           | PRIMARY KEY
eventType            | VARCHAR(100)   | NOT NULL
processedAt          | TIMESTAMP      | NOT NULL
source               | VARCHAR(100)   | NOT NULL
```

### Dead Letter Queue

**Configuration**:
- Topic: `document.events.dlq`
- Used for messages that fail processing after retries
- Monitored for manual intervention

**Handling**:
1. Message fails processing 3 times
2. Message moved to DLQ
3. Alert sent to operations team
4. Manual review and reprocessing

---

## Error Handling & Validation

### Input Validation

**File Upload Validation**:
- File format validation (extension check)
- File size validation (max 10MB)
- File content validation (magic bytes check)
- File name validation (no invalid characters)

**Request Validation**:
- RequestId must be valid UUID
- RequestId must exist in Request Management Service
- UploadedBy must be valid UUID
- UploadedBy must exist in Administration Service

### File Validation

**Format Validation**:
- Check file extension against allowed list
- Validate MIME type matches extension
- Check file signature (magic bytes)

**Size Validation**:
- Check file size ≤ 10MB
- Reject empty files

**Content Validation**:
- For PDFs: Check PDF header
- For Office docs: Check OLE/OOXML header
- For images: Check image header

### Error Handling Patterns

**Validation Errors**:
- Return 400 Bad Request with error details
- Include field name and validation rule violated
- Example: "File size exceeds maximum of 10MB"

**Authorization Errors**:
- Return 403 Forbidden
- Include reason for denial
- Log security event

**Not Found Errors**:
- Return 404 Not Found
- Include resource identifier
- Example: "Document with ID {documentId} not found"

**Storage Errors**:
- Return 500 Internal Server Error
- Implement retry logic
- Log error details for investigation

**Conflict Errors**:
- Return 409 Conflict
- Include conflict details
- Example: "Document already exists for this request"

### Exception Hierarchy

```
Exception
├── DocumentManagementException (base)
│   ├── DocumentNotFoundException
│   ├── DocumentAccessDeniedException
│   ├── DocumentValidationException
│   ├── DocumentStorageException
│   ├── InvalidFileFormatException
│   ├── FileSizeExceededException
│   └── DuplicateDocumentException
```

### Error Recovery

**Transient Errors**:
- Implement exponential backoff retry
- Max 3 retries with delays (1s, 5s, 15s)
- Log retry attempts

**Permanent Errors**:
- Log error with full context
- Publish error event
- Return appropriate HTTP error code
- Alert operations team if critical

---

## Security Design

### Authentication

**JWT Token Validation**:
- Extract token from Authorization header
- Validate token signature
- Check token expiration
- Validate token claims (userId, roles)

**Token Refresh**:
- Implement token refresh endpoint
- Refresh tokens before expiration
- Invalidate old tokens

### Authorization

**Role-Based Access Control**:
- Employee: Can access own request documents
- Head of Office: Can access documents for requests they review
- SMD/RDC Reviewer: Can access assigned request documents
- SMD/RDC Head: Can access assigned request documents
- Administrator: Can access all documents
- System Administrator: Can access all documents and delete

**Method-Level Security**:
- Use @PreAuthorize annotations
- Implement custom authorization logic
- Check user roles and permissions

**Document-Level Security**:
- Implement DocumentAccessPolicy
- Check access control rules
- Log access attempts

### Secure File Storage

**Encryption**:
- Encrypt files at rest in Nutanix Object Storage
- Use AES-256 encryption
- Manage encryption keys securely

**Access Control**:
- Implement bucket-level access control
- Restrict access to authorized users only
- Audit all storage access

**Integrity Verification**:
- Generate SHA-256 checksum on upload
- Verify checksum on download
- Detect file tampering

### Audit Logging

**Security Events**:
- Document upload: documentId, uploadedBy, timestamp
- Document download: documentId, downloadedBy, timestamp
- Access denied: documentId, attemptedBy, reason, timestamp
- Document deletion: documentId, deletedBy, timestamp

**Audit Trail**:
- Immutable append-only log
- Stored in document_access_log table
- Retained for compliance period

### CORS and Security Headers

**CORS Configuration**:
- Allow requests from trusted origins
- Allow credentials in requests
- Allow specific HTTP methods

**Security Headers**:
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Strict-Transport-Security: max-age=31536000
- Content-Security-Policy: restrict resource loading

---

## Performance & Scalability

### Caching Strategy

**Redis Caching**:
- Cache document metadata (1 hour TTL)
- Cache access control rules (30 minutes TTL)
- Cache request documents list (30 minutes TTL)
- Cache PDF previews (24 hours TTL)

**Cache Invalidation**:
- Invalidate on document update
- Invalidate on document delete
- Invalidate on access control change
- Automatic expiration after TTL

**Cache Hit Rate Target**: >80%

### Database Optimization

**Indexing**:
- Index on requestId for fast lookup
- Index on uploadedBy for user queries
- Index on status for filtering
- Index on uploadedAt for date range queries
- Composite indexes for common query patterns

**Query Optimization**:
- Use pagination for large result sets
- Implement query result caching
- Use database connection pooling
- Monitor slow queries

**Partitioning**:
- Partition documents table by uploadedAt
- Reduces table size and improves query performance
- Enables archival of old partitions

### Horizontal Scaling

**Stateless Services**:
- No session state in service instances
- All state in database or cache
- Enables horizontal scaling

**Load Balancing**:
- Distribute requests across service instances
- Use round-robin or least connections algorithm
- Health checks for instance availability

**Asynchronous Processing**:
- Use Kafka for event-driven communication
- Decouple services for independent scaling
- Enable parallel processing

### File Storage Optimization

**Nutanix Object Storage**:
- Distributed storage for scalability
- Automatic replication for reliability
- Efficient storage utilization

**Storage Paths**:
- Organize by requestId and documentId
- Enables efficient retrieval and deletion
- Supports archival and cleanup

### Performance Targets

- Document upload: <5 seconds
- Document download: <2 seconds
- Document metadata query: <500ms
- Document list query: <1 second
- API response time (p95): <1 second
- Cache hit rate: >80%
- Database query time (p95): <100ms

---

## Testing Strategy

### Unit Testing

**Domain Layer Testing**:
- Test aggregate invariants
- Test value object validation
- Test domain service logic
- Test factory creation
- Test policy rules

**Application Layer Testing**:
- Test application service methods
- Test command handlers
- Test event publishing
- Test transaction management
- Test caching logic

**Infrastructure Layer Testing**:
- Test repository queries
- Test event serialization/deserialization
- Test external service clients
- Test security configuration

**Test Coverage Target**: >80%

### Integration Testing

**API Integration Tests**:
- Test REST endpoints
- Test request/response serialization
- Test error handling
- Test authentication/authorization

**Database Integration Tests**:
- Test repository operations
- Test data persistence
- Test query results
- Test transaction rollback

**Event Integration Tests**:
- Test event publishing
- Test event consumption
- Test idempotency
- Test error handling

**External Service Integration Tests**:
- Test Nutanix Object Storage integration
- Test Redis cache integration
- Test Kafka integration
- Test Administration Service client

### Property-Based Testing

**Document Validation**:
- Test file format validation with random inputs
- Test file size validation with various sizes
- Test checksum generation consistency

**Access Control**:
- Test access control rules with various user roles
- Test access log consistency

### Performance Testing

**Load Testing**:
- Test document upload with concurrent requests
- Test document download with concurrent requests
- Test database query performance
- Test cache performance

**Stress Testing**:
- Test system behavior under high load
- Test graceful degradation
- Test recovery from failures

### Test Data Management

**Test Fixtures**:
- Create sample documents
- Create sample users and roles
- Create sample requests

**Test Database**:
- Use separate test database
- Reset database between tests
- Use transactions for test isolation

---

## Deployment & Operations

### Deployment Architecture

**Docker Container**:
- Base image: openjdk:17-slim
- Multi-stage build for optimization
- Health check endpoint
- Graceful shutdown support

**Kubernetes Deployment**:
- Deployment with 3 replicas (for high availability)
- Service for load balancing
- ConfigMap for configuration
- Secrets for sensitive data
- PersistentVolume for data storage

**Environment Configuration**:
- Development: Single replica, local storage
- Staging: 2 replicas, staging storage
- Production: 3+ replicas, production storage

### Configuration Management

**Environment Variables**:
- Database connection string
- Kafka broker addresses
- Nutanix Object Storage endpoint
- Redis connection string
- JWT secret key
- Log level

**ConfigMap**:
- Application properties
- Feature flags
- Timeout configurations
- Cache TTL settings

**Secrets**:
- Database password
- API keys
- JWT secret
- Storage credentials

### Health Checks

**Liveness Probe**:
- Endpoint: `/actuator/health/live`
- Interval: 10 seconds
- Timeout: 5 seconds
- Failure threshold: 3

**Readiness Probe**:
- Endpoint: `/actuator/health/ready`
- Interval: 5 seconds
- Timeout: 3 seconds
- Failure threshold: 3

**Health Check Components**:
- Database connectivity
- Kafka connectivity
- Nutanix Object Storage connectivity
- Redis connectivity

### Graceful Shutdown

**Shutdown Process**:
1. Stop accepting new requests
2. Wait for in-flight requests to complete (max 30 seconds)
3. Close database connections
4. Close Kafka connections
5. Shutdown application

**Drain Period**: 30 seconds

### Scaling Policies

**Horizontal Scaling**:
- Scale up when CPU >70% or memory >80%
- Scale down when CPU <30% and memory <50%
- Min replicas: 2
- Max replicas: 10

**Vertical Scaling**:
- CPU request: 500m, limit: 1000m
- Memory request: 512Mi, limit: 1Gi

### Monitoring and Alerting

**Metrics**:
- Document upload count
- Document download count
- Average file size
- Storage usage
- Cache hit rate
- API response times
- Error rates

**Alerts**:
- High error rate (>5%)
- Storage usage >80%
- Cache hit rate <50%
- API response time >1000ms
- Kafka consumer lag >1000 messages
- Database connection pool exhausted

**Dashboards**:
- Service health dashboard
- Performance metrics dashboard
- Error tracking dashboard
- Storage usage dashboard

---

## Architecture Diagrams

### Component Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Document Management Service                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ REST API Layer                                               │   │
│  │ ┌────────────────────────────────────────────────────────┐   │   │
│  │ │ DocumentController                                     │   │   │
│  │ │ - uploadDocument()                                     │   │   │
│  │ │ - downloadDocument()                                   │   │   │
│  │ │ - getDocument()                                        │   │   │
│  │ │ - listDocuments()                                      │   │   │
│  │ │ - deleteDocument()                                     │   │   │
│  │ │ - previewDocument()                                    │   │   │
│  │ └────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                            ↓                                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ Application Layer                                            │   │
│  │ ┌────────────────────────────────────────────────────────┐   │   │
│  │ │ DocumentApplicationService                             │   │   │
│  │ │ DocumentAccessApplicationService                       │   │   │
│  │ │ DocumentValidationApplicationService                   │   │   │
│  │ │ DocumentStorageApplicationService                      │   │   │
│  │ └────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                            ↓                                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ Domain Layer                                                 │   │
│  │ ┌────────────────────────────────────────────────────────┐   │   │
│  │ │ Document Aggregate                                     │   │   │
│  │ │ DocumentAccessService                                  │   │   │
│  │ │ DocumentValidationService                              │   │   │
│  │ │ DocumentStorageService                                 │   │   │
│  │ │ DocumentFactory                                        │   │   │
│  │ │ Policies & Specifications                              │   │   │
│  │ └────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                            ↓                                         │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ Infrastructure Layer                                         │   │
│  │ ┌────────────────────────────────────────────────────────┐   │   │
│  │ │ DocumentRepository (JPA)                               │   │   │
│  │ │ EventPublisher (Kafka)                                 │   │   │
│  │ │ EventListener (Kafka)                                  │   │   │
│  │ │ NutanixStorageClient                                   │   │   │
│  │ │ RedisCache                                             │   │   │
│  │ │ SecurityConfiguration                                  │   │   │
│  │ └────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
                            ↓
        ┌───────────────────┼───────────────────┐
        ↓                   ↓                   ↓
    PostgreSQL         Nutanix Object      Redis Cache
    Database           Storage
```

### Document Upload Sequence Diagram

```
Client                Controller            Service              Domain              Repository
  │                      │                    │                   │                    │
  ├─ POST /upload ──────→│                    │                   │                    │
  │                      ├─ validate ────────→│                   │                    │
  │                      │                    ├─ validate ───────→│                    │
  │                      │                    │                   ├─ check rules ─────→│
  │                      │                    │                   │                    │
  │                      │                    ├─ store ──────────→│ Nutanix Storage    │
  │                      │                    │                   │                    │
  │                      │                    ├─ create ─────────→│                    │
  │                      │                    │                   ├─ save ────────────→│
  │                      │                    │                   │                    │
  │                      │                    ├─ publish event ──→│                    │
  │                      │                    │                   │                    │
  │                      │                    ├─ cache ──────────→│ Redis Cache        │
  │                      │                    │                   │                    │
  │                      │← DocumentDTO ─────│                   │                    │
  │← 201 Created ────────│                    │                   │                    │
  │                      │                    │                   │                    │
```

### Document Access Control Diagram

```
┌─────────────────────────────────────────────────────────────┐
│              Document Access Control                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Employee (Requestor)                                       │
│  ├─ Can view own request documents                          │
│  └─ Can download own request documents                      │
│                                                              │
│  Head of Office                                             │
│  ├─ Can view documents for requests they review             │
│  └─ Can download documents for requests they review         │
│                                                              │
│  SMD/RDC Reviewer                                           │
│  ├─ Can view all documents for assigned requests            │
│  ├─ Can download all documents for assigned requests        │
│  └─ Can preview PDF documents                               │
│                                                              │
│  SMD/RDC Head                                               │
│  ├─ Can view all documents for assigned requests            │
│  ├─ Can download all documents for assigned requests        │
│  └─ Can preview PDF documents                               │
│                                                              │
│  Administrator                                              │
│  ├─ Can view all documents                                  │
│  ├─ Can download all documents                              │
│  └─ Can preview all documents                               │
│                                                              │
│  System Administrator                                       │
│  ├─ Can view all documents                                  │
│  ├─ Can download all documents                              │
│  ├─ Can delete documents                                    │
│  └─ Can archive documents                                   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Document Lifecycle State Machine

```
┌──────────────┐
│   Uploaded   │ (Document created, not yet active)
└──────┬───────┘
       │ request submitted
       ↓
┌──────────────┐
│    Active    │ (Document is active, can be accessed)
└──────┬───────┘
       │
       ├─ request declined ──→ ┌──────────────┐
       │                       │   Archived   │
       │                       └──────────────┘
       │
       └─ request implemented ─→ ┌──────────────┐
                                 │   Archived   │
                                 └──────────────┘

┌──────────────┐
│   Deleted    │ (Document permanently removed)
└──────────────┘
```

---

**Document Status**: Complete
**Last Updated**: January 8, 2025
**Version**: 1.0
