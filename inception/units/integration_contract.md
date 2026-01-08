# Integration Contract

## Overview
This document defines the integration contract between the four services in the Access Request Processing System. The system uses an event-driven architecture with message queues for asynchronous communication and a shared database with clear ownership boundaries.

---

## Architecture Pattern

### Integration Style
- **Primary**: Event-Driven Architecture using Message Queue (e.g., RabbitMQ, Kafka, AWS SQS)
- **Secondary**: REST APIs for synchronous queries

### Data Strategy
- **Shared Database** with clear table ownership per service
- Each service owns specific tables and is the only writer to those tables
- Services can read from other services' tables but must not write to them
- For data modifications, services must publish events or call APIs

---

## Service Overview

| Service | Responsibilities | Database Tables Owned |
|---------|-----------------|----------------------|
| **Request Management** | Request lifecycle, workflow orchestration | access_requests, request_history, access_types, workflow_states |
| **Document Management** | File storage and retrieval | documents, document_access_log |
| **Notification** | Email notifications | notifications, email_templates, notification_log |
| **Administration** | Users, roles, settings, reports, audit | users, roles, user_roles, offices, system_settings, audit_logs |

---

## Event Contracts

### Event Schema Standard
All events follow this structure:
```json
{
  "eventId": "uuid",
  "eventType": "EventName",
  "timestamp": "ISO-8601 datetime",
  "source": "service-name",
  "version": "1.0",
  "payload": {
    // Event-specific data
  }
}
```

---

## Request Management Service

### Events Published

#### RequestCreated
**Trigger**: New access request is created  
**Consumers**: Notification Service, Administration Service

```json
{
  "eventType": "RequestCreated",
  "payload": {
    "requestId": "string",
    "requestorId": "string",
    "requestorName": "string",
    "requestorEmail": "string",
    "accessType": "string",
    "systemName": "string",
    "justification": "string",
    "officeId": "string",
    "createdAt": "datetime"
  }
}
```

---

#### RequestSubmitted
**Trigger**: Request is submitted for approval  
**Consumers**: Notification Service, Administration Service

```json
{
  "eventType": "RequestSubmitted",
  "payload": {
    "requestId": "string",
    "requestorId": "string",
    "requestorName": "string",
    "requestorEmail": "string",
    "headOfOfficeId": "string",
    "headOfOfficeName": "string",
    "headOfOfficeEmail": "string",
    "accessType": "string",
    "systemName": "string",
    "submittedAt": "datetime"
  }
}
```

---

#### RequestApprovedByHeadOfOffice
**Trigger**: Head of Office approves request  
**Consumers**: Notification Service, Administration Service

```json
{
  "eventType": "RequestApprovedByHeadOfOffice",
  "payload": {
    "requestId": "string",
    "requestorId": "string",
    "requestorEmail": "string",
    "approverId": "string",
    "approverName": "string",
    "comments": "string",
    "reviewerId": "string",
    "reviewerName": "string",
    "reviewerEmail": "string",
    "approvedAt": "datetime"
  }
}
```

---

#### RequestEndorsed
**Trigger**: SMD/RDC Reviewer endorses request  
**Consumers**: Notification Service, Administration Service

```json
{
  "eventType": "RequestEndorsed",
  "payload": {
    "requestId": "string",
    "reviewerId": "string",
    "reviewerName": "string",
    "comments": "string",
    "smdHeadId": "string",
    "smdHeadName": "string",
    "smdHeadEmail": "string",
    "endorsedAt": "datetime"
  }
}
```

---

#### RequestFinallyApproved
**Trigger**: SMD/RDC Head gives final approval  
**Consumers**: Notification Service, Administration Service

```json
{
  "eventType": "RequestFinallyApproved",
  "payload": {
    "requestId": "string",
    "requestorId": "string",
    "approverId": "string",
    "approverName": "string",
    "comments": "string",
    "administratorId": "string",
    "administratorName": "string",
    "administratorEmail": "string",
    "accessType": "string",
    "systemName": "string",
    "approvedAt": "datetime"
  }
}
```

---

#### RequestDeclined
**Trigger**: Request is declined at any stage  
**Consumers**: Notification Service, Administration Service

```json
{
  "eventType": "RequestDeclined",
  "payload": {
    "requestId": "string",
    "requestorId": "string",
    "requestorEmail": "string",
    "declinerId": "string",
    "declinerName": "string",
    "declinerRole": "string",
    "reason": "string",
    "declinedAt": "datetime"
  }
}
```

---

#### RequestReturned
**Trigger**: SMD/RDC Head returns request to reviewer  
**Consumers**: Notification Service, Administration Service

```json
{
  "eventType": "RequestReturned",
  "payload": {
    "requestId": "string",
    "returnerId": "string",
    "returnerName": "string",
    "reason": "string",
    "reviewerId": "string",
    "reviewerEmail": "string",
    "returnedAt": "datetime"
  }
}
```

---

#### RequestImplemented
**Trigger**: Administrator marks request as implemented  
**Consumers**: Notification Service, Administration Service

```json
{
  "eventType": "RequestImplemented",
  "payload": {
    "requestId": "string",
    "requestorId": "string",
    "requestorEmail": "string",
    "implementerId": "string",
    "implementerName": "string",
    "notes": "string",
    "implementedAt": "datetime"
  }
}
```

---

#### AccessTypeAdded
**Trigger**: New access type is created  
**Consumers**: Administration Service

```json
{
  "eventType": "AccessTypeAdded",
  "payload": {
    "accessTypeId": "string",
    "name": "string",
    "description": "string",
    "createdBy": "string",
    "createdAt": "datetime"
  }
}
```

---

#### AccessTypeRoutingConfigured
**Trigger**: Access type routing rules are updated  
**Consumers**: Administration Service

```json
{
  "eventType": "AccessTypeRoutingConfigured",
  "payload": {
    "accessTypeId": "string",
    "administratorRoles": ["string"],
    "configuredBy": "string",
    "configuredAt": "datetime"
  }
}
```

---

### Events Consumed

#### DocumentUploaded
**Source**: Document Management Service  
**Purpose**: Update request with document metadata

```json
{
  "eventType": "DocumentUploaded",
  "payload": {
    "documentId": "string",
    "requestId": "string",
    "fileName": "string",
    "fileSize": "number",
    "fileType": "string",
    "uploadedBy": "string",
    "uploadedAt": "datetime"
  }
}
```

---

#### DocumentDeleted
**Source**: Document Management Service  
**Purpose**: Update request document status

```json
{
  "eventType": "DocumentDeleted",
  "payload": {
    "documentId": "string",
    "requestId": "string",
    "deletedBy": "string",
    "deletedAt": "datetime"
  }
}
```

---

### REST API Endpoints

#### Requests
- `POST /api/requests` - Create new request
  - **Request Body**: `{ requestorId, accessType, systemName, justification }`
  - **Response**: `{ requestId, status, createdAt }`

- `GET /api/requests/{id}` - Get request details
  - **Response**: `{ requestId, requestorId, accessType, systemName, status, history, ... }`

- `GET /api/requests` - List requests with filters
  - **Query Params**: `status, requestorId, accessType, fromDate, toDate, page, size`
  - **Response**: `{ requests: [], totalCount, page, size }`

- `PUT /api/requests/{id}/submit` - Submit request
  - **Request Body**: `{ headOfOfficeId }`
  - **Response**: `{ requestId, status, submittedAt }`

- `PUT /api/requests/{id}/approve` - Approve request
  - **Request Body**: `{ approverId, comments }`
  - **Response**: `{ requestId, status, approvedAt }`

- `PUT /api/requests/{id}/decline` - Decline request
  - **Request Body**: `{ declinerId, reason }`
  - **Response**: `{ requestId, status, declinedAt }`

- `PUT /api/requests/{id}/endorse` - Endorse request
  - **Request Body**: `{ reviewerId, comments }`
  - **Response**: `{ requestId, status, endorsedAt }`

- `PUT /api/requests/{id}/return` - Return request
  - **Request Body**: `{ returnerId, reason }`
  - **Response**: `{ requestId, status, returnedAt }`

- `PUT /api/requests/{id}/implement` - Mark as implemented
  - **Request Body**: `{ implementerId, notes }`
  - **Response**: `{ requestId, status, implementedAt }`

- `GET /api/requests/{id}/history` - Get request history
  - **Response**: `{ requestId, history: [{ action, actor, timestamp, comments }] }`

#### Access Types
- `GET /api/access-types` - List all access types
  - **Response**: `{ accessTypes: [{ id, name, description, routingRules }] }`

- `POST /api/access-types` - Create new access type
  - **Request Body**: `{ name, description, routingRules }`
  - **Response**: `{ accessTypeId, name, createdAt }`

- `PUT /api/access-types/{id}` - Update access type
  - **Request Body**: `{ name, description }`
  - **Response**: `{ accessTypeId, updatedAt }`

- `PUT /api/access-types/{id}/routing` - Configure routing
  - **Request Body**: `{ administratorRoles: [] }`
  - **Response**: `{ accessTypeId, routingRules, updatedAt }`

#### Search
- `GET /api/requests/search` - Search requests
  - **Query Params**: `q, accessType, status, officeId, fromDate, toDate`
  - **Response**: `{ results: [], totalCount }`

---

## Document Management Service

### Events Published

#### DocumentUploaded
**Trigger**: Document successfully uploaded  
**Consumers**: Request Management Service, Administration Service

```json
{
  "eventType": "DocumentUploaded",
  "payload": {
    "documentId": "string",
    "requestId": "string",
    "fileName": "string",
    "fileSize": "number",
    "fileType": "string",
    "uploadedBy": "string",
    "uploadedAt": "datetime"
  }
}
```

---

#### DocumentDownloaded
**Trigger**: Document is downloaded  
**Consumers**: Administration Service

```json
{
  "eventType": "DocumentDownloaded",
  "payload": {
    "documentId": "string",
    "requestId": "string",
    "downloadedBy": "string",
    "downloadedAt": "datetime"
  }
}
```

---

#### DocumentDeleted
**Trigger**: Document is removed  
**Consumers**: Request Management Service, Administration Service

```json
{
  "eventType": "DocumentDeleted",
  "payload": {
    "documentId": "string",
    "requestId": "string",
    "deletedBy": "string",
    "deletedAt": "datetime"
  }
}
```

---

### Events Consumed

#### RequestCreated
**Source**: Request Management Service  
**Purpose**: Initialize document tracking for request

#### RequestDeclined
**Source**: Request Management Service  
**Purpose**: Archive documents for declined request

#### RequestImplemented
**Source**: Request Management Service  
**Purpose**: Archive documents for completed request

---

### REST API Endpoints

#### Documents
- `POST /api/documents/upload` - Upload document
  - **Request Body**: `multipart/form-data { file, requestId, uploadedBy }`
  - **Response**: `{ documentId, fileName, fileSize, uploadedAt }`

- `GET /api/documents/{id}` - Get document metadata
  - **Response**: `{ documentId, requestId, fileName, fileSize, fileType, uploadedBy, uploadedAt }`

- `GET /api/documents/{id}/download` - Download document
  - **Response**: Binary file stream

- `DELETE /api/documents/{id}` - Delete document
  - **Request Body**: `{ deletedBy }`
  - **Response**: `{ documentId, deletedAt }`

- `GET /api/documents/request/{requestId}` - List documents for request
  - **Response**: `{ requestId, documents: [{ documentId, fileName, fileSize, uploadedAt }] }`

- `GET /api/documents/{id}/preview` - Preview document (PDF only)
  - **Response**: PDF preview data

#### Document Status
- `GET /api/documents/request/{requestId}/status` - Get document completeness
  - **Response**: `{ requestId, hasAllDocuments, missingDocuments: [], attachedDocuments: [] }`

---

## Notification Service

### Events Published
None - Notification Service is a consumer-only service

---

### Events Consumed

All events from Request Management Service:
- `RequestCreated` → Send confirmation to requestor
- `RequestSubmitted` → Send notification to Head of Office
- `RequestApprovedByHeadOfOffice` → Send notification to requestor and reviewer
- `RequestEndorsed` → Send notification to SMD/RDC Head
- `RequestFinallyApproved` → Send notification to administrator
- `RequestDeclined` → Send notification to requestor
- `RequestReturned` → Send notification to reviewer
- `RequestImplemented` → Send notification to requestor

---

### REST API Endpoints

#### Notifications
- `GET /api/notifications` - List all notifications
  - **Query Params**: `userId, status, fromDate, toDate, page, size`
  - **Response**: `{ notifications: [], totalCount }`

- `GET /api/notifications/{id}` - Get notification details
  - **Response**: `{ notificationId, userId, type, subject, body, status, sentAt }`

- `POST /api/notifications/send` - Send manual notification
  - **Request Body**: `{ recipientEmail, subject, body, type }`
  - **Response**: `{ notificationId, status, sentAt }`

- `GET /api/notifications/user/{userId}` - Get user's notifications
  - **Response**: `{ userId, notifications: [] }`

#### Email Templates
- `GET /api/email-templates` - List all templates
  - **Response**: `{ templates: [{ type, name, subject, body }] }`

- `GET /api/email-templates/{type}` - Get template by type
  - **Response**: `{ type, name, subject, body, variables }`

- `PUT /api/email-templates/{type}` - Update template
  - **Request Body**: `{ subject, body }`
  - **Response**: `{ type, updatedAt }`

- `POST /api/email-templates/{type}/preview` - Preview template
  - **Request Body**: `{ variables: {} }`
  - **Response**: `{ subject, body }`

- `POST /api/email-templates/{type}/reset` - Reset to default
  - **Response**: `{ type, resetAt }`

---

## Administration Service

### Events Published

#### UserRoleAssigned
**Trigger**: Role is assigned to user  
**Consumers**: None (logged only)

```json
{
  "eventType": "UserRoleAssigned",
  "payload": {
    "userId": "string",
    "roleId": "string",
    "roleName": "string",
    "assignedBy": "string",
    "assignedAt": "datetime"
  }
}
```

---

#### UserRoleRevoked
**Trigger**: Role is removed from user  
**Consumers**: None (logged only)

```json
{
  "eventType": "UserRoleRevoked",
  "payload": {
    "userId": "string",
    "roleId": "string",
    "roleName": "string",
    "revokedBy": "string",
    "revokedAt": "datetime"
  }
}
```

---

#### SystemSettingsUpdated
**Trigger**: System settings are changed  
**Consumers**: All services (may need to refresh configuration)

```json
{
  "eventType": "SystemSettingsUpdated",
  "payload": {
    "settingKey": "string",
    "oldValue": "string",
    "newValue": "string",
    "updatedBy": "string",
    "updatedAt": "datetime"
  }
}
```

---

### Events Consumed

All events from all services for audit logging and metrics:
- All Request Management events
- All Document Management events
- Own events (UserRoleAssigned, UserRoleRevoked, SystemSettingsUpdated)

---

### REST API Endpoints

#### Authentication
- `POST /api/auth/login` - User login
  - **Request Body**: `{ username, password }`
  - **Response**: `{ token, userId, roles, expiresAt }`

- `POST /api/auth/logout` - User logout
  - **Request Body**: `{ token }`
  - **Response**: `{ success: true }`

- `POST /api/auth/refresh` - Refresh session token
  - **Request Body**: `{ token }`
  - **Response**: `{ token, expiresAt }`

- `POST /api/auth/reset-password` - Request password reset
  - **Request Body**: `{ email }`
  - **Response**: `{ success: true, message }`

#### Users & Roles
- `GET /api/users` - List all users
  - **Query Params**: `officeId, roleId, page, size`
  - **Response**: `{ users: [], totalCount }`

- `GET /api/users/{id}` - Get user details
  - **Response**: `{ userId, username, email, officeId, roles: [] }`

- `POST /api/users` - Create new user
  - **Request Body**: `{ username, email, password, officeId }`
  - **Response**: `{ userId, createdAt }`

- `PUT /api/users/{id}` - Update user
  - **Request Body**: `{ email, officeId }`
  - **Response**: `{ userId, updatedAt }`

- `GET /api/users/{id}/roles` - Get user roles
  - **Response**: `{ userId, roles: [{ roleId, roleName }] }`

- `POST /api/users/{id}/roles` - Assign role to user
  - **Request Body**: `{ roleId, assignedBy }`
  - **Response**: `{ userId, roleId, assignedAt }`

- `DELETE /api/users/{id}/roles/{roleId}` - Revoke role from user
  - **Request Body**: `{ revokedBy }`
  - **Response**: `{ userId, roleId, revokedAt }`

- `GET /api/roles` - List all roles
  - **Response**: `{ roles: [{ roleId, roleName, description }] }`

#### System Settings
- `GET /api/settings` - Get all settings
  - **Response**: `{ settings: [{ key, value, description }] }`

- `GET /api/settings/{key}` - Get specific setting
  - **Response**: `{ key, value, description }`

- `PUT /api/settings/{key}` - Update setting
  - **Request Body**: `{ value, updatedBy }`
  - **Response**: `{ key, value, updatedAt }`

- `POST /api/settings/test` - Test configuration
  - **Request Body**: `{ key, value }`
  - **Response**: `{ success: true/false, message }`

#### Audit Logs
- `GET /api/audit-logs` - List audit logs
  - **Query Params**: `userId, action, fromDate, toDate, page, size`
  - **Response**: `{ logs: [], totalCount }`

- `GET /api/audit-logs/{id}` - Get audit log details
  - **Response**: `{ logId, userId, action, details, timestamp }`

- `GET /api/audit-logs/export` - Export audit logs
  - **Query Params**: `fromDate, toDate, format`
  - **Response**: CSV file

#### Reports & Analytics
- `GET /api/reports/requests` - Generate request report
  - **Query Params**: `fromDate, toDate, officeId, accessType`
  - **Response**: `{ totalRequests, approved, declined, pending, avgProcessingTime }`

- `GET /api/reports/performance` - Generate performance report
  - **Query Params**: `fromDate, toDate`
  - **Response**: `{ avgTimeByStage: {}, bottlenecks: [] }`

- `GET /api/reports/export` - Export report
  - **Query Params**: `reportType, fromDate, toDate, format`
  - **Response**: PDF or Excel file

- `GET /api/dashboard/metrics` - Get dashboard metrics
  - **Response**: `{ totalRequests, pendingByStage: {}, completedToday, avgProcessingTime }`

- `GET /api/dashboard/trends` - Get trend data
  - **Query Params**: `period`
  - **Response**: `{ requestsByDate: [], approvalRates: [], processingTimes: [] }`

#### Offices
- `GET /api/offices` - List all offices
  - **Response**: `{ offices: [{ officeId, name, level, parentId }] }`

- `GET /api/offices/{id}` - Get office details
  - **Response**: `{ officeId, name, level, parentId, headOfOfficeId }`

- `GET /api/offices/{id}/hierarchy` - Get office hierarchy
  - **Response**: `{ officeId, name, parent: {}, children: [] }`

---

## Database Schema Ownership

### Request Management Service Tables

#### access_requests
- Primary owner and writer
- Other services can read for display purposes

#### request_history
- Primary owner and writer
- Other services can read for audit trail

#### access_types
- Primary owner and writer
- Other services can read for dropdown lists

#### workflow_states
- Primary owner and writer
- Other services can read for status display

---

### Document Management Service Tables

#### documents
- Primary owner and writer
- Other services can read metadata only

#### document_access_log
- Primary owner and writer
- Administration Service can read for audit

---

### Notification Service Tables

#### notifications
- Primary owner and writer
- Administration Service can read for audit

#### email_templates
- Primary owner and writer
- Administration Service can update via API

#### notification_log
- Primary owner and writer
- Administration Service can read for audit

---

### Administration Service Tables

#### users
- Primary owner and writer
- All services can read for user information

#### roles
- Primary owner and writer
- All services can read for authorization

#### user_roles
- Primary owner and writer
- All services can read for authorization

#### offices
- Primary owner and writer
- All services can read for office hierarchy

#### system_settings
- Primary owner and writer
- All services can read for configuration

#### audit_logs
- Primary owner and writer
- Read-only for authorized administrators

---

## Error Handling Standards

### HTTP Status Codes
- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., duplicate)
- `500 Internal Server Error` - Server error

### Error Response Format
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {},
    "timestamp": "ISO-8601 datetime"
  }
}
```

---

## Authentication & Authorization

### Authentication
- All API endpoints require authentication token (JWT)
- Token obtained via `/api/auth/login`
- Token included in `Authorization: Bearer <token>` header

### Authorization
- Role-based access control (RBAC)
- Roles: Employee, Head of Office, SMD/RDC Reviewer, SMD/RDC Head, System Admin, DB Admin
- Each endpoint specifies required roles
- Users can have multiple roles

---

## Message Queue Configuration

### Queue Names
- `request.events` - All request lifecycle events
- `document.events` - All document events
- `notification.events` - Notification requests
- `admin.events` - Administrative events

### Message Format
All messages use JSON format with standard event schema

### Retry Policy
- Failed message processing retries 3 times
- Exponential backoff: 1s, 5s, 15s
- Dead letter queue for permanently failed messages

---

**Document Version**: 1.0  
**Last Updated**: 2025  
**Services**: 4  
**Total Events**: 15  
**Total API Endpoints**: 60+
