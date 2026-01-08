# Unit 2: Document Management Service - Domain Model

## Overview

The Document Management Service domain model manages all file operations including uploading, storing, retrieving, and managing supporting documents (SAM, Justification) attached to access requests. This service ensures secure document storage, access control, and maintains a complete audit trail of document access.

---

## Aggregates

### 1. Document Aggregate Root

**Purpose**: Manages a single document with its metadata, access control, and lifecycle.

**Aggregate Boundary**: Encompasses all data and behavior related to a single document.

**Entities within Aggregate**:
- **Document** (Aggregate Root)
  - Unique identifier: DocumentId
  - Core attributes: requestId, fileName, fileSize, fileType, uploadedBy, uploadedAt, status
  - Relationships: references to request, uploader
  - Behavior: manages document lifecycle, access control

- **DocumentAccessLog** (Entity)
  - Tracks all document access events
  - Attributes: accessId, downloadedBy, downloadedAt, accessType (view, download, preview)
  - Behavior: append-only, immutable

**Value Objects within Aggregate**:
- **DocumentId**: Unique identifier for the document
- **FileName**: Name of the document file
  - Attributes: name, extension
  - Behavior: validates file extension against allowed types

- **FileSize**: Size of the document
  - Attributes: bytes, maxAllowed (10MB)
  - Behavior: validates size constraints

- **FileType**: Type of the document
  - Values: PDF, DOC, DOCX, JPG, PNG
  - Behavior: validates against allowed types

- **DocumentStatus**: Enumeration of document states
  - Values: Uploaded, Active, Archived, Deleted
  - Behavior: validates state transitions

- **DocumentAccessControl**: Encapsulates access rules
  - Attributes: allowedRoles (list), isPublic
  - Behavior: determines who can access document

- **FileMetadata**: Encapsulates file information
  - Attributes: mimeType, encoding, checksum
  - Behavior: immutable

- **DocumentValidationRules**: Encapsulates validation logic
  - Attributes: allowedExtensions, maxFileSize, allowedMimeTypes
  - Behavior: validates documents against rules

**Aggregate Invariants**:
- A document must have a valid file name
- A document must have a valid file type
- A document size must not exceed 10MB
- A document must be associated with a request
- A document must have an uploader
- A document cannot be accessed if status is Deleted
- A document access must be logged

**Aggregate Lifecycle**:
1. Created when uploaded (status: Uploaded)
2. Activated when request is submitted (status: Active)
3. Archived when request is declined or implemented (status: Archived)
4. Deleted when explicitly removed (status: Deleted)

---

## Entities (Outside Aggregates)

### DocumentSearchResult

**Purpose**: Represents a document in search results (read model entity).

**Attributes**:
- documentId, fileName, fileType, uploadedBy, uploadedAt, requestId, status

**Behavior**: Immutable, used for query results only

---

## Value Objects

### Core Value Objects

**DocumentId**
- Unique identifier for documents
- Format: UUID or sequential ID
- Behavior: immutable, comparable

**FileName**
- Name of the document file
- Attributes: name, extension
- Behavior: validates extension against allowed types, immutable

**FileSize**
- Size of the document in bytes
- Attributes: bytes, maxAllowed (10485760 bytes = 10MB)
- Behavior: validates size constraints, comparable

**FileType**
- Type of the document
- Values: PDF, DOC, DOCX, JPG, PNG
- Behavior: validates against allowed types, immutable

**DocumentStatus**
- Enumeration of document states
- Values: Uploaded, Active, Archived, Deleted
- Behavior: validates state transitions

**DocumentAccessControl**
- Encapsulates access control rules
- Attributes: allowedRoles (list), isPublic (boolean)
- Behavior: determines who can access document, immutable

**FileMetadata**
- Encapsulates file information
- Attributes: mimeType, encoding, checksum (SHA-256)
- Behavior: immutable, used for integrity verification

**DocumentValidationRules**
- Encapsulates validation logic
- Attributes: allowedExtensions (list), maxFileSize, allowedMimeTypes (list)
- Behavior: validates documents against rules

**DocumentAccessEntry**
- Encapsulates a single access log entry
- Attributes: accessId, downloadedBy, downloadedAt, accessType
- Behavior: immutable

**DocumentUploadInfo**
- Encapsulates upload information
- Attributes: uploadedBy, uploadedAt, uploadPath
- Behavior: immutable

---

## Domain Events

### Published Events

**DocumentUploaded**
- Trigger: When a document is successfully uploaded
- Payload: documentId, requestId, fileName, fileSize, fileType, uploadedBy, uploadedAt
- Subscribers: RequestManagementService (update request), AdministrationService (audit log)

**DocumentDownloaded**
- Trigger: When a document is downloaded
- Payload: documentId, requestId, downloadedBy, downloadedAt
- Subscribers: AdministrationService (audit log)

**DocumentDeleted**
- Trigger: When a document is deleted
- Payload: documentId, requestId, deletedBy, deletedAt
- Subscribers: RequestManagementService (update request), AdministrationService (audit log)

**DocumentAccessDenied**
- Trigger: When unauthorized access is attempted
- Payload: documentId, attemptedBy, attemptedAt, reason
- Subscribers: AdministrationService (audit log, security alert)

**DocumentValidationFailed**
- Trigger: When document validation fails during upload
- Payload: fileName, fileSize, fileType, reason
- Subscribers: AdministrationService (audit log)

### Consumed Events

**RequestCreated** (from Request Management Service)
- Used to: Initialize document tracking for request
- Action: Create document container for request

**RequestDeclined** (from Request Management Service)
- Used to: Archive documents for declined request
- Action: Change document status to Archived

**RequestImplemented** (from Request Management Service)
- Used to: Archive documents for completed request
- Action: Change document status to Archived

---

## Domain Services

### DocumentAccessService

**Purpose**: Enforces document access control and authorization.

**Responsibilities**:
- Verify user has permission to access document
- Log all access attempts
- Enforce role-based access control
- Handle access denial

**Methods**:
- canAccessDocument(documentId, userId, accessType): boolean
- grantAccess(documentId, userId, accessType): void
- denyAccess(documentId, userId, reason): void
- logAccess(documentId, userId, accessType): void

**Dependencies**: DocumentRepository, AdministrationService (for user roles)

---

### DocumentValidationService

**Purpose**: Validates documents during upload.

**Responsibilities**:
- Validate file format
- Validate file size
- Validate file content (if applicable)
- Generate file checksum

**Methods**:
- validateFileFormat(fileName, fileType): ValidationResult
- validateFileSize(fileSize): ValidationResult
- validateFileContent(fileContent): ValidationResult
- generateChecksum(fileContent): String

**Dependencies**: DocumentValidationRules

---

### DocumentStorageService

**Purpose**: Manages physical document storage.

**Responsibilities**:
- Store document files
- Retrieve document files
- Delete document files
- Manage storage paths

**Methods**:
- storeDocument(documentId, fileContent): StoragePath
- retrieveDocument(documentId): FileContent
- deleteDocument(documentId): void
- getStoragePath(documentId): String

**Dependencies**: File system or cloud storage provider

---

## Repositories

### DocumentRepository

**Purpose**: Persists and retrieves Document aggregates.

**Responsibilities**:
- Save new documents
- Update existing documents
- Query documents by various criteria
- Maintain document access logs

**Query Methods**:
- findById(documentId): Document
- findByRequest(requestId): List<Document>
- findByType(fileType): List<Document>
- findByUploadDate(startDate, endDate): List<Document>
- findByUploader(uploaderId): List<Document>
- search(criteria): List<Document>
- findActiveDocumentsForRequest(requestId): List<Document>

**Persistence Methods**:
- save(document): void
- update(document): void
- delete(documentId): void
- archiveDocumentsForRequest(requestId): void

---

## Specifications (Query Objects)

### DocumentsByRequestSpecification

**Purpose**: Query documents for a specific request.

**Criteria**: requestId

**Returns**: List<Document>

---

### DocumentsByTypeSpecification

**Purpose**: Query documents by file type.

**Criteria**: fileType

**Returns**: List<Document>

---

### DocumentsByUploadDateSpecification

**Purpose**: Query documents uploaded within a date range.

**Criteria**: startDate, endDate

**Returns**: List<Document>

---

### IncompleteDocumentsSpecification

**Purpose**: Query requests missing required documents.

**Criteria**: requestId, requiredDocumentTypes

**Returns**: List<MissingDocument>

---

### DocumentsByUploaderSpecification

**Purpose**: Query documents uploaded by a specific user.

**Criteria**: uploaderId

**Returns**: List<Document>

---

### ActiveDocumentsForRequestSpecification

**Purpose**: Query active documents for a request.

**Criteria**: requestId, status (Active)

**Returns**: List<Document>

---

## Policies

### DocumentAccessPolicy

**Purpose**: Defines who can access which documents.

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
- canViewDocument(documentId, userId): boolean
- canDownloadDocument(documentId, userId): boolean
- canPreviewDocument(documentId, userId): boolean
- validateAccessRequest(documentId, userId, accessType): ValidationResult

---

### DocumentValidationPolicy

**Purpose**: Defines document validation rules.

**Rules**:
- Allowed file types: PDF, DOC, DOCX, JPG, PNG
- Maximum file size: 10MB per file
- File name must not be empty
- File name must not contain invalid characters
- File content must match declared file type
- Duplicate files are allowed (same content, different requests)

**Validation Methods**:
- isValidFileType(fileType): boolean
- isValidFileSize(fileSize): boolean
- isValidFileName(fileName): boolean
- validateDocument(document): ValidationResult

---

### DocumentRetentionPolicy

**Purpose**: Defines document retention and archival rules.

**Rules**:
- Active documents are retained while request is active
- Documents are archived when request is declined or implemented
- Archived documents are retained for compliance period (configurable)
- Deleted documents are permanently removed

**Archival Methods**:
- archiveDocument(documentId): void
- deleteDocument(documentId): void
- isRetentionExpired(documentId): boolean

---

## Factory Patterns

### DocumentFactory

**Purpose**: Creates new Document aggregates with validation.

**Responsibilities**:
- Validate file format and size
- Generate unique DocumentId
- Create document with proper access control
- Initialize document in Uploaded status

**Creation Method**:
- createDocument(requestId, fileName, fileSize, fileType, uploadedBy): Document
  - Validates file format
  - Validates file size
  - Generates DocumentId
  - Creates access control rules
  - Returns new Document aggregate in Uploaded status

---

### DocumentAccessLogFactory

**Purpose**: Creates DocumentAccessLog entries.

**Responsibilities**:
- Create immutable access log entries
- Capture access information
- Timestamp all entries

**Creation Method**:
- createAccessLogEntry(documentId, userId, accessType): DocumentAccessEntry
  - Captures current timestamp
  - Creates immutable entry
  - Returns DocumentAccessEntry

---

### DocumentValidationRulesFactory

**Purpose**: Creates DocumentValidationRules with default configuration.

**Responsibilities**:
- Initialize validation rules
- Set allowed file types
- Set file size limits
- Set allowed MIME types

**Creation Method**:
- createDefaultValidationRules(): DocumentValidationRules
  - Sets allowed extensions: [PDF, DOC, DOCX, JPG, PNG]
  - Sets max file size: 10MB
  - Sets allowed MIME types
  - Returns DocumentValidationRules

---

## Bounded Context Interactions

### Outbound Events

The Document Management Service publishes events that are consumed by:
- **Request Management Service**: Receives DocumentUploaded, DocumentDeleted events to track document status
- **Administration Service**: Receives all events for audit logging and security monitoring

### Inbound Events

The Document Management Service consumes events from:
- **Request Management Service**: RequestCreated, RequestDeclined, RequestImplemented events to manage document lifecycle

---

## Document Lifecycle Diagram

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

## Document Access Control Diagram

```
┌─────────────────────────────────────────────────────────┐
│              Document Access Control                     │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Employee (Requestor)                                   │
│  ├─ Can view own request documents                      │
│  └─ Can download own request documents                  │
│                                                          │
│  Head of Office                                         │
│  ├─ Can view documents for requests they review         │
│  └─ Can download documents for requests they review     │
│                                                          │
│  SMD/RDC Reviewer                                       │
│  ├─ Can view all documents for assigned requests        │
│  ├─ Can download all documents for assigned requests    │
│  └─ Can preview PDF documents                           │
│                                                          │
│  SMD/RDC Head                                           │
│  ├─ Can view all documents for assigned requests        │
│  ├─ Can download all documents for assigned requests    │
│  └─ Can preview PDF documents                           │
│                                                          │
│  Administrator                                          │
│  ├─ Can view all documents                              │
│  ├─ Can download all documents                          │
│  └─ Can preview all documents                           │
│                                                          │
│  System Administrator                                   │
│  ├─ Can view all documents                              │
│  ├─ Can download all documents                          │
│  ├─ Can delete documents                                │
│  └─ Can archive documents                               │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## Aggregate Relationships Diagram

```
┌─────────────────────────────────────────────────────────┐
│                  Document Aggregate                      │
├─────────────────────────────────────────────────────────┤
│ DocumentId (Value Object)                               │
│ FileName (Value Object)                                 │
│ FileSize (Value Object)                                 │
│ FileType (Value Object)                                 │
│ DocumentStatus (Value Object)                           │
│ DocumentAccessControl (Value Object)                    │
│ FileMetadata (Value Object)                             │
│ DocumentUploadInfo (Value Object)                       │
│                                                          │
│ └─ DocumentAccessLog (Entity) [0..*]                    │
│    └─ DocumentAccessEntry (Value Object)                │
│                                                          │
│ References:                                             │
│ ├─ requestId (from Request Management Service)          │
│ └─ uploadedBy (UserId from Administration Service)      │
└─────────────────────────────────────────────────────────┘
```

---

## Event Flow Diagram

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
```

---

## Summary

The Document Management Service domain model provides a comprehensive architecture for managing documents with strong emphasis on security, access control, and audit trails. The use of aggregates, value objects, domain services, and specifications ensures clear separation of concerns and maintainability. The factory patterns ensure consistent creation of domain objects, while policies encapsulate business rules and validation logic. The service integrates seamlessly with the Request Management Service through event-driven communication.

