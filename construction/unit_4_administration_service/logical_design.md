# Unit 4: Administration Service - Logical Design

## Document Information
- **Service**: Administration Service (Unit 4)
- **Purpose**: Manage user authentication, authorization, role management, system configuration, reporting, and audit logging for the entire system
- **Technology Stack**: Java, Spring Boot, PostgreSQL, Kafka, Spring Security, Redis, JWT
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
11. [Authentication & Authorization Design](#authentication--authorization-design)
12. [Reporting & Analytics Design](#reporting--analytics-design)
13. [Office Hierarchy Management Design](#office-hierarchy-management-design)
14. [Security Design](#security-design)
15. [Performance & Scalability](#performance--scalability)
16. [Testing Strategy](#testing-strategy)
17. [Deployment & Operations](#deployment--operations)
18. [Architecture Diagrams](#architecture-diagrams)

---

## Overview

### Purpose

The Administration Service is a cross-cutting, event-driven service that provides comprehensive administrative capabilities for the entire Access Request Processing System. It manages user authentication and authorization, role-based access control, office hierarchy management, system configuration, reporting and analytics, and maintains an immutable audit trail of all system activities. This service acts as the security and compliance backbone of the system.

### Scope

This logical design covers:
- User identity management and authentication (login, logout, password reset, session management)
- Role-based access control (RBAC) with multi-role support
- User role assignment and revocation
- Office hierarchy management and validation
- System configuration and settings management with versioning
- Comprehensive audit logging of all system activities
- Reporting and analytics (request reports, performance metrics, user activity, audit reports)
- Dashboard with real-time metrics and trends
- User and role queries with filtering and search
- Integration with Request Management, Document Management, and Notification services
- Event consumption from all services for audit logging and metrics

### Key Responsibilities

1. Authenticate users via email and password with JWT token generation
2. Manage user sessions with timeout and refresh token support
3. Enforce role-based access control across all system operations
4. Manage user roles and permissions with multi-role support
5. Assign and revoke roles from users
6. Manage office hierarchy and validate office relationships
7. Assign users to offices and validate office-based access
8. Manage system configuration settings with versioning
9. Log all system activities in an immutable audit trail
10. Generate reports on request processing, performance, and user activity
11. Provide dashboard with real-time metrics and trends
12. Consume events from all services for audit logging and metrics
13. Publish authentication, authorization, and configuration change events
14. Cache user roles, permissions, and office hierarchy for performance
15. Enforce rate limiting on authentication attempts
16. Support password reset with secure token generation

### Design Principles

- **Domain-Driven Design**: Rich domain model with aggregates, entities, and value objects
- **Event-Driven Architecture**: Asynchronous communication via Kafka message queue
- **Layered Architecture**: Clear separation of concerns (Presentation, Application, Domain, Infrastructure)
- **SOLID Principles**: Single responsibility, Open/closed, Liskov substitution, Interface segregation, Dependency inversion
- **Security First**: Authentication, authorization, encryption, audit logging at every layer
- **Reliability**: Transaction management, error handling, idempotent processing
- **Performance**: Caching strategies, query optimization, batch processing
- **Scalability**: Stateless services, asynchronous processing, horizontal scaling support
- **Auditability**: Immutable audit trail, comprehensive logging, compliance support

---

## System Architecture

### High-Level System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Access Request Processing System                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │            Administration Service (Unit 4)                       │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ REST API Layer (Spring MVC)                                │  │   │
│  │  │ - Authentication endpoints (login, logout, refresh)        │  │   │
│  │  │ - User management endpoints (CRUD, role assignment)        │  │   │
│  │  │ - Role management endpoints                                │  │   │
│  │  │ - Office management endpoints                              │  │   │
│  │  │ - System settings endpoints                                │  │   │
│  │  │ - Audit log query endpoints                                │  │   │
│  │  │ - Reporting and dashboard endpoints                        │  │   │
│  │  │ - Metrics endpoints                                        │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Application Layer (Spring Services)                        │  │   │
│  │  │ - AuthenticationApplicationService                         │  │   │
│  │  │ - AuthorizationApplicationService                          │  │   │
│  │  │ - UserManagementApplicationService                         │  │   │
│  │  │ - RoleManagementApplicationService                         │  │   │
│  │  │ - OfficeManagementApplicationService                       │  │   │
│  │  │ - SystemSettingsApplicationService                         │  │   │
│  │  │ - AuditApplicationService                                  │  │   │
│  │  │ - ReportingApplicationService                              │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Domain Layer (DDD)                                         │  │   │
│  │  │ - User Aggregate                                           │  │   │
│  │  │ - Role Aggregate                                           │  │   │
│  │  │ - Office Aggregate                                         │  │   │
│  │  │ - SystemSettings Aggregate                                 │  │   │
│  │  │ - AuditLog Aggregate                                       │  │   │
│  │  │ - Domain Services                                          │  │   │
│  │  │ - Value Objects                                            │  │   │
│  │  │ - Domain Events                                            │  │   │
│  │  │ - Policies                                                 │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │ Infrastructure Layer                                       │  │   │
│  │  │ - UserRepository (JPA)                                     │  │   │
│  │  │ - RoleRepository (JPA)                                     │  │   │
│  │  │ - OfficeRepository (JPA)                                   │  │   │
│  │  │ - SystemSettingsRepository (JPA)                           │  │   │
│  │  │ - AuditLogRepository (JPA)                                 │  │   │
│  │  │ - EventPublisher (Kafka)                                   │  │   │
│  │  │ - EventListener (Kafka)                                    │  │   │
│  │  │ - RedisCache (User roles, permissions, office hierarchy)   │  │   │
│  │  │ - Database (PostgreSQL)                                    │  │   │
│  │  │ - Spring Security (Authentication/Authorization)           │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ External Services (via REST APIs & Kafka Events)                │   │
│  │ - Request Management Service (Event Consumer)                   │   │
│  │ - Document Management Service (Event Consumer)                  │   │
│  │ - Notification Service (Event Consumer)                         │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Infrastructure Components                                        │   │
│  │ - Kafka Message Queue (Event Bus)                               │   │
│  │ - PostgreSQL Database                                           │   │
│  │ - Redis Cache (User roles, permissions, office hierarchy)       │   │
│  │ - Spring Security (Authentication/Authorization)                │   │
│  │ - Docker Container                                              │   │
│  │ - Kubernetes Orchestration                                      │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Component Interactions

```
Client (Web/Mobile)
    ↓
REST API Controller
    ↓
Application Service
    ↓
Domain Service / Aggregate
    ↓
Repository
    ↓
Database / Cache
    ↓
Response back to Client

Event Flow:
Request Management Service (Event Publisher)
    ↓
Kafka Topic (request.events)
    ↓
Event Listener (Kafka Consumer)
    ↓
Event Handler (Route to appropriate handler)
    ↓
AuditApplicationService
    ↓
AuditService (Domain Service)
    ↓
AuditLog Repository (Persist)
    ↓
Event Publisher (Kafka)
    ↓
Kafka Topic (admin.events)
    ↓
Other Services (Event Consumers)
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
│ - Authentication Filters                                    │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Application Layer (Business Logic Orchestration)            │
│ - Application Services                                      │
│ - Command/Query Handlers                                    │
│ - Event Publishing                                          │
│ - Transaction Management                                    │
│ - Metrics Collection                                        │
│ - Caching Coordination                                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Domain Layer (Business Rules)                               │
│ - Aggregates (User, Role, Office, SystemSettings, AuditLog)│
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
│ - Redis Cache Client                                        │
│ - Spring Security                                           │
│ - External Service Clients                                  │
│ - Logging & Monitoring                                      │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### Presentation Layer
- Receive HTTP requests from clients
- Validate input data and convert to domain objects
- Call application services to process requests
- Return HTTP responses with appropriate status codes
- Handle authentication via JWT tokens
- Enforce authorization via role-based access control
- Provide error responses with appropriate error codes
- Implement rate limiting for sensitive endpoints

#### Application Layer
- Orchestrate domain logic and coordinate between aggregates
- Manage transaction boundaries
- Publish domain events to Kafka
- Consume domain events from Kafka
- Coordinate caching strategies
- Collect and aggregate metrics
- Implement use cases (authenticate user, assign role, generate report, etc.)
- Handle cross-cutting concerns (logging, monitoring)

#### Domain Layer
- Encapsulate business rules and validation logic
- Manage aggregate state and invariants
- Publish domain events
- Provide domain services (authentication, authorization, audit logging)
- Define specifications for queries
- Implement policies for business rules
- Validate all state changes

#### Infrastructure Layer
- Persist aggregates to PostgreSQL database
- Publish/consume events via Kafka
- Cache user roles, permissions, and office hierarchy in Redis
- Integrate with Spring Security for authentication/authorization
- Implement logging and monitoring
- Provide external service clients
- Handle database transactions and connection pooling


---

## API Layer Design

### REST API Endpoints

The Administration Service exposes 40+ REST API endpoints organized into logical groups. All endpoints require JWT authentication except for the login endpoint.

#### Authentication Endpoints

**1. User Login**
- **Endpoint**: `POST /api/v1/auth/login`
- **Authentication**: Not required
- **Request Body**:
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "token": "string (JWT)",
    "userId": "string (UUID)",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "roles": ["string"],
    "officeId": "string (UUID)",
    "expiresAt": "ISO-8601 datetime"
  }
  ```
- **Error Responses**:
  - 400 Bad Request: Invalid email or password format
  - 401 Unauthorized: Invalid credentials
  - 429 Too Many Requests: Too many login attempts (rate limiting)

**2. User Logout**
- **Endpoint**: `POST /api/v1/auth/logout`
- **Authentication**: Required (JWT Token)
- **Request Body**:
  ```json
  {
    "token": "string (JWT)"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "success": true,
    "message": "Logged out successfully"
  }
  ```

**3. Refresh Token**
- **Endpoint**: `POST /api/v1/auth/refresh`
- **Authentication**: Required (JWT Token)
- **Request Body**:
  ```json
  {
    "token": "string (JWT)"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "token": "string (new JWT)",
    "expiresAt": "ISO-8601 datetime"
  }
  ```

**4. Request Password Reset**
- **Endpoint**: `POST /api/v1/auth/reset-password`
- **Authentication**: Not required
- **Request Body**:
  ```json
  {
    "email": "string"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "success": true,
    "message": "Password reset email sent"
  }
  ```

**5. Confirm Password Reset**
- **Endpoint**: `POST /api/v1/auth/reset-password/confirm`
- **Authentication**: Not required
- **Request Body**:
  ```json
  {
    "token": "string (reset token)",
    "newPassword": "string"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "success": true,
    "message": "Password reset successfully"
  }
  ```

#### User Management Endpoints

**6. List Users**
- **Endpoint**: `GET /api/v1/users`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `officeId`: Filter by office (optional)
  - `roleId`: Filter by role (optional)
  - `status`: Filter by status (Active, Inactive, Suspended, Deleted)
  - `page`: Page number (default: 1)
  - `size`: Page size (default: 20, max: 100)
  - `sortBy`: Sort field (email, firstName, lastName, createdAt)
  - `sortOrder`: Sort order (ASC, DESC)
- **Response** (200 OK):
  ```json
  {
    "users": [
      {
        "userId": "string (UUID)",
        "email": "string",
        "firstName": "string",
        "lastName": "string",
        "position": "string",
        "officeId": "string (UUID)",
        "officeName": "string",
        "roles": ["string"],
        "status": "string",
        "createdAt": "ISO-8601 datetime",
        "updatedAt": "ISO-8601 datetime"
      }
    ],
    "totalCount": "number",
    "page": "number",
    "size": "number",
    "totalPages": "number"
  }
  ```

**7. Get User Details**
- **Endpoint**: `GET /api/v1/users/{userId}`
- **Authentication**: Required
- **Authorization**: User (own profile), Administrator
- **Response** (200 OK):
  ```json
  {
    "userId": "string (UUID)",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "position": "string",
    "officeId": "string (UUID)",
    "officeName": "string",
    "roles": [
      {
        "roleId": "string (UUID)",
        "roleName": "string",
        "assignedAt": "ISO-8601 datetime"
      }
    ],
    "status": "string",
    "createdAt": "ISO-8601 datetime",
    "updatedAt": "ISO-8601 datetime"
  }
  ```

**8. Create User**
- **Endpoint**: `POST /api/v1/users`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```json
  {
    "email": "string",
    "password": "string",
    "firstName": "string",
    "lastName": "string",
    "position": "string",
    "officeId": "string (UUID)"
  }
  ```
- **Response** (201 Created):
  ```json
  {
    "userId": "string (UUID)",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "createdAt": "ISO-8601 datetime"
  }
  ```

**9. Update User**
- **Endpoint**: `PUT /api/v1/users/{userId}`
- **Authentication**: Required
- **Authorization**: User (own profile), Administrator
- **Request Body**:
  ```json
  {
    "firstName": "string",
    "lastName": "string",
    "position": "string",
    "officeId": "string (UUID)"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "userId": "string (UUID)",
    "updatedAt": "ISO-8601 datetime"
  }
  ```

**10. Suspend User**
- **Endpoint**: `PUT /api/v1/users/{userId}/suspend`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```json
  {
    "reason": "string"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "userId": "string (UUID)",
    "status": "Suspended",
    "suspendedAt": "ISO-8601 datetime"
  }
  ```

**11. Activate User**
- **Endpoint**: `PUT /api/v1/users/{userId}/activate`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```json
  {
    "userId": "string (UUID)",
    "status": "Active",
    "activatedAt": "ISO-8601 datetime"
  }
  ```

**12. Delete User**
- **Endpoint**: `DELETE /api/v1/users/{userId}`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```json
  {
    "userId": "string (UUID)",
    "deletedAt": "ISO-8601 datetime"
  }
  ```

**13. Search Users**
- **Endpoint**: `GET /api/v1/users/search`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `q`: Search query (email, firstName, lastName)
  - `officeId`: Filter by office
  - `roleId`: Filter by role
  - `page`: Page number
  - `size`: Page size
- **Response** (200 OK):
  ```json
  {
    "results": [
      {
        "userId": "string",
        "email": "string",
        "firstName": "string",
        "lastName": "string",
        "officeName": "string",
        "roles": ["string"]
      }
    ],
    "totalCount": "number"
  }
  ```

#### User Role Management Endpoints

**14. Get User Roles**
- **Endpoint**: `GET /api/v1/users/{userId}/roles`
- **Authentication**: Required
- **Authorization**: User (own roles), Administrator
- **Response** (200 OK):
  ```json
  {
    "userId": "string (UUID)",
    "roles": [
      {
        "roleId": "string (UUID)",
        "roleName": "string",
        "description": "string",
        "assignedAt": "ISO-8601 datetime",
        "assignedBy": "string"
      }
    ]
  }
  ```

**15. Assign Role to User**
- **Endpoint**: `POST /api/v1/users/{userId}/roles`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```json
  {
    "roleId": "string (UUID)"
  }
  ```
- **Response** (201 Created):
  ```json
  {
    "userId": "string (UUID)",
    "roleId": "string (UUID)",
    "roleName": "string",
    "assignedAt": "ISO-8601 datetime"
  }
  ```

**16. Revoke Role from User**
- **Endpoint**: `DELETE /api/v1/users/{userId}/roles/{roleId}`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```json
  {
    "userId": "string (UUID)",
    "roleId": "string (UUID)",
    "revokedAt": "ISO-8601 datetime"
  }
  ```

#### Role Management Endpoints

**17. List Roles**
- **Endpoint**: `GET /api/v1/roles`
- **Authentication**: Required
- **Authorization**: Authenticated users
- **Response** (200 OK):
  ```json
  {
    "roles": [
      {
        "roleId": "string (UUID)",
        "roleName": "string",
        "description": "string",
        "permissions": ["string"],
        "createdAt": "ISO-8601 datetime"
      }
    ]
  }
  ```

**18. Get Role Details**
- **Endpoint**: `GET /api/v1/roles/{roleId}`
- **Authentication**: Required
- **Authorization**: Authenticated users
- **Response** (200 OK):
  ```json
  {
    "roleId": "string (UUID)",
    "roleName": "string",
    "description": "string",
    "permissions": [
      {
        "permissionId": "string (UUID)",
        "name": "string",
        "description": "string",
        "resource": "string",
        "action": "string"
      }
    ],
    "createdAt": "ISO-8601 datetime",
    "updatedAt": "ISO-8601 datetime"
  }
  ```

**19. Create Role**
- **Endpoint**: `POST /api/v1/roles`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```json
  {
    "roleName": "string",
    "description": "string",
    "permissions": ["string (permission IDs)"]
  }
  ```
- **Response** (201 Created):
  ```json
  {
    "roleId": "string (UUID)",
    "roleName": "string",
    "createdAt": "ISO-8601 datetime"
  }
  ```

**20. Update Role**
- **Endpoint**: `PUT /api/v1/roles/{roleId}`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```json
  {
    "description": "string",
    "permissions": ["string (permission IDs)"]
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "roleId": "string (UUID)",
    "updatedAt": "ISO-8601 datetime"
  }
  ```

#### Office Management Endpoints

**21. List Offices**
- **Endpoint**: `GET /api/v1/offices`
- **Authentication**: Required
- **Authorization**: Authenticated users
- **Query Parameters**:
  - `parentOfficeId`: Filter by parent office (optional)
  - `page`: Page number
  - `size`: Page size
- **Response** (200 OK):
  ```json
  {
    "offices": [
      {
        "officeId": "string (UUID)",
        "name": "string",
        "level": "number",
        "parentOfficeId": "string (UUID)",
        "headOfOfficeId": "string (UUID)",
        "headOfOfficeName": "string",
        "createdAt": "ISO-8601 datetime"
      }
    ],
    "totalCount": "number"
  }
  ```

**22. Get Office Details**
- **Endpoint**: `GET /api/v1/offices/{officeId}`
- **Authentication**: Required
- **Authorization**: Authenticated users
- **Response** (200 OK):
  ```json
  {
    "officeId": "string (UUID)",
    "name": "string",
    "level": "number",
    "parentOfficeId": "string (UUID)",
    "parentOfficeName": "string",
    "headOfOfficeId": "string (UUID)",
    "headOfOfficeName": "string",
    "headOfOfficeEmail": "string",
    "createdAt": "ISO-8601 datetime",
    "updatedAt": "ISO-8601 datetime"
  }
  ```

**23. Get Office Hierarchy**
- **Endpoint**: `GET /api/v1/offices/{officeId}/hierarchy`
- **Authentication**: Required
- **Authorization**: Authenticated users
- **Response** (200 OK):
  ```json
  {
    "officeId": "string (UUID)",
    "name": "string",
    "parent": {
      "officeId": "string (UUID)",
      "name": "string"
    },
    "children": [
      {
        "officeId": "string (UUID)",
        "name": "string"
      }
    ]
  }
  ```

**24. Create Office**
- **Endpoint**: `POST /api/v1/offices`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```json
  {
    "name": "string",
    "parentOfficeId": "string (UUID)",
    "headOfOfficeId": "string (UUID)"
  }
  ```
- **Response** (201 Created):
  ```json
  {
    "officeId": "string (UUID)",
    "name": "string",
    "createdAt": "ISO-8601 datetime"
  }
  ```

**25. Update Office**
- **Endpoint**: `PUT /api/v1/offices/{officeId}`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```json
  {
    "name": "string",
    "headOfOfficeId": "string (UUID)"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "officeId": "string (UUID)",
    "updatedAt": "ISO-8601 datetime"
  }
  ```

#### System Settings Endpoints

**26. Get All Settings**
- **Endpoint**: `GET /api/v1/settings`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```json
  {
    "settings": [
      {
        "key": "string",
        "value": "string",
        "type": "String|Integer|Boolean",
        "description": "string",
        "updatedAt": "ISO-8601 datetime"
      }
    ]
  }
  ```

**27. Get Specific Setting**
- **Endpoint**: `GET /api/v1/settings/{key}`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```json
  {
    "key": "string",
    "value": "string",
    "type": "String|Integer|Boolean",
    "description": "string",
    "updatedAt": "ISO-8601 datetime"
  }
  ```

**28. Update Setting**
- **Endpoint**: `PUT /api/v1/settings/{key}`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```json
  {
    "value": "string"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "key": "string",
    "value": "string",
    "updatedAt": "ISO-8601 datetime"
  }
  ```

**29. Test Setting**
- **Endpoint**: `POST /api/v1/settings/test`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Request Body**:
  ```json
  {
    "key": "string",
    "value": "string"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "success": true,
    "message": "Setting is valid"
  }
  ```

#### Audit Log Endpoints

**30. List Audit Logs**
- **Endpoint**: `GET /api/v1/audit-logs`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `userId`: Filter by user (optional)
  - `action`: Filter by action (optional)
  - `resourceType`: Filter by resource type (optional)
  - `fromDate`: Date range start (ISO-8601)
  - `toDate`: Date range end (ISO-8601)
  - `page`: Page number
  - `size`: Page size
- **Response** (200 OK):
  ```json
  {
    "logs": [
      {
        "auditLogId": "string (UUID)",
        "userId": "string (UUID)",
        "userName": "string",
        "action": "string",
        "resourceType": "string",
        "resourceId": "string",
        "timestamp": "ISO-8601 datetime",
        "details": "string"
      }
    ],
    "totalCount": "number",
    "page": "number",
    "size": "number"
  }
  ```

**31. Get Audit Log Details**
- **Endpoint**: `GET /api/v1/audit-logs/{auditLogId}`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```json
  {
    "auditLogId": "string (UUID)",
    "userId": "string (UUID)",
    "userName": "string",
    "action": "string",
    "resourceType": "string",
    "resourceId": "string",
    "timestamp": "ISO-8601 datetime",
    "details": {
      "oldValue": "string",
      "newValue": "string",
      "reason": "string",
      "ipAddress": "string"
    }
  }
  ```

**32. Export Audit Logs**
- **Endpoint**: `GET /api/v1/audit-logs/export`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `fromDate`: Date range start
  - `toDate`: Date range end
  - `format`: Export format (CSV, PDF, Excel)
- **Response** (200 OK): File download

**33. Search Audit Logs**
- **Endpoint**: `GET /api/v1/audit-logs/search`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `q`: Search query (userId, action, resourceId)
  - `fromDate`: Date range start
  - `toDate`: Date range end
  - `page`: Page number
  - `size`: Page size
- **Response** (200 OK):
  ```json
  {
    "results": [
      {
        "auditLogId": "string",
        "userId": "string",
        "action": "string",
        "timestamp": "datetime"
      }
    ],
    "totalCount": "number"
  }
  ```

#### Reporting & Analytics Endpoints

**34. Generate Request Report**
- **Endpoint**: `GET /api/v1/reports/requests`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `fromDate`: Date range start (ISO-8601)
  - `toDate`: Date range end (ISO-8601)
  - `officeId`: Filter by office (optional)
  - `accessType`: Filter by access type (optional)
  - `status`: Filter by status (optional)
- **Response** (200 OK):
  ```json
  {
    "period": {
      "fromDate": "ISO-8601 datetime",
      "toDate": "ISO-8601 datetime"
    },
    "summary": {
      "totalRequests": "number",
      "approvedRequests": "number",
      "declinedRequests": "number",
      "pendingRequests": "number",
      "implementedRequests": "number",
      "approvalRate": "number (percentage)",
      "avgProcessingTime": "number (hours)"
    },
    "byStatus": [
      {
        "status": "string",
        "count": "number"
      }
    ],
    "byAccessType": [
      {
        "accessType": "string",
        "count": "number",
        "approvalRate": "number"
      }
    ],
    "byOffice": [
      {
        "officeId": "string",
        "officeName": "string",
        "count": "number",
        "approvalRate": "number"
      }
    ]
  }
  ```

**35. Generate Performance Report**
- **Endpoint**: `GET /api/v1/reports/performance`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `fromDate`: Date range start
  - `toDate`: Date range end
- **Response** (200 OK):
  ```json
  {
    "period": {
      "fromDate": "ISO-8601 datetime",
      "toDate": "ISO-8601 datetime"
    },
    "avgTimeByStage": {
      "createdToSubmitted": "number (hours)",
      "submittedToApprovedByHeadOfOffice": "number (hours)",
      "approvedToEndorsed": "number (hours)",
      "endorsedToFinallyApproved": "number (hours)",
      "approvedToImplemented": "number (hours)"
    },
    "bottlenecks": [
      {
        "stage": "string",
        "avgTime": "number (hours)",
        "percentageOfTotal": "number"
      }
    ],
    "throughput": {
      "requestsPerDay": "number",
      "requestsPerWeek": "number",
      "requestsPerMonth": "number"
    }
  }
  ```

**36. Generate User Activity Report**
- **Endpoint**: `GET /api/v1/reports/user-activity`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `userId`: User ID (optional)
  - `fromDate`: Date range start
  - `toDate`: Date range end
- **Response** (200 OK):
  ```json
  {
    "userId": "string (UUID)",
    "userName": "string",
    "period": {
      "fromDate": "ISO-8601 datetime",
      "toDate": "ISO-8601 datetime"
    },
    "activitySummary": {
      "requestsCreated": "number",
      "requestsApproved": "number",
      "requestsDeclined": "number",
      "requestsImplemented": "number",
      "loginCount": "number",
      "lastLoginAt": "ISO-8601 datetime"
    },
    "activityByDay": [
      {
        "date": "ISO-8601 date",
        "actions": "number"
      }
    ]
  }
  ```

**37. Export Report**
- **Endpoint**: `GET /api/v1/reports/export`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `reportType`: Report type (requests, performance, userActivity, audit)
  - `fromDate`: Date range start
  - `toDate`: Date range end
  - `format`: Export format (CSV, PDF, Excel)
- **Response** (200 OK): File download

#### Dashboard & Metrics Endpoints

**38. Get Dashboard Metrics**
- **Endpoint**: `GET /api/v1/dashboard/metrics`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Response** (200 OK):
  ```json
  {
    "totalRequests": "number",
    "pendingRequests": "number",
    "approvedRequests": "number",
    "declinedRequests": "number",
    "implementedRequests": "number",
    "pendingByStage": {
      "createdNotSubmitted": "number",
      "submittedNotApprovedByHeadOfOffice": "number",
      "approvedNotEndorsed": "number",
      "endorsedNotFinallyApproved": "number",
      "approvedNotImplemented": "number"
    },
    "completedToday": "number",
    "avgProcessingTime": "number (hours)",
    "approvalRate": "number (percentage)"
  }
  ```

**39. Get Trend Data**
- **Endpoint**: `GET /api/v1/dashboard/trends`
- **Authentication**: Required
- **Authorization**: Administrator only
- **Query Parameters**:
  - `period`: Time period (day, week, month)
- **Response** (200 OK):
  ```json
  {
    "period": "string",
    "requestsByDate": [
      {
        "date": "ISO-8601 date",
        "count": "number"
      }
    ],
    "approvalRateByDate": [
      {
        "date": "ISO-8601 date",
        "rate": "number (percentage)"
      }
    ],
    "processingTimeByDate": [
      {
        "date": "ISO-8601 date",
        "avgTime": "number (hours)"
      }
    ]
  }
  ```

**40. Get System Health**
- **Endpoint**: `GET /api/v1/health`
- **Authentication**: Not required
- **Response** (200 OK):
  ```json
  {
    "status": "UP|DOWN",
    "timestamp": "ISO-8601 datetime",
    "components": {
      "database": "UP|DOWN",
      "kafka": "UP|DOWN",
      "redis": "UP|DOWN"
    }
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
    "path": "/api/v1/endpoint"
  }
}
```

### Common Error Codes

- `INVALID_INPUT`: Input validation failed
- `INVALID_EMAIL_FORMAT`: Email format is invalid
- `INVALID_PASSWORD_FORMAT`: Password does not meet requirements
- `INVALID_CREDENTIALS`: Email or password is incorrect
- `UNAUTHORIZED`: Authentication required
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `DUPLICATE_EMAIL`: Email already exists
- `DUPLICATE_ROLE_NAME`: Role name already exists
- `DUPLICATE_OFFICE_NAME`: Office name already exists
- `INVALID_OFFICE_HIERARCHY`: Office hierarchy is invalid (circular reference)
- `USER_SUSPENDED`: User account is suspended
- `USER_DELETED`: User account is deleted
- `INVALID_ROLE_ASSIGNMENT`: Role assignment is invalid
- `INVALID_SETTING_VALUE`: Setting value is invalid
- `RATE_LIMIT_EXCEEDED`: Too many requests
- `INTERNAL_ERROR`: Internal server error


---

## Application Layer Design

### Application Services

The Application Layer contains Spring Services that orchestrate domain logic, manage transactions, and coordinate between aggregates and infrastructure components.

#### AuthenticationApplicationService

**Responsibilities**:
- Handle user login requests
- Validate credentials against stored hashed passwords
- Generate JWT tokens with appropriate expiration
- Manage user sessions
- Handle password reset requests
- Validate session tokens
- Implement rate limiting for login attempts

**Key Methods**:
- `login(email, password): AuthenticationResult`
  - Validates email format
  - Retrieves user from repository
  - Validates password against stored hash
  - Checks user status (not suspended or deleted)
  - Generates JWT token with 30-minute expiration
  - Creates session record
  - Publishes UserLoginSuccessful event
  - Returns token and user information
  - On failure: Publishes UserLoginFailed event, increments failed attempt counter

- `logout(token): void`
  - Validates token
  - Invalidates session
  - Publishes UserLoggedOut event

- `refreshToken(token): RefreshTokenResult`
  - Validates token
  - Checks if token is close to expiration
  - Generates new token
  - Returns new token with updated expiration

- `requestPasswordReset(email): void`
  - Validates email exists
  - Generates secure reset token (UUID)
  - Stores reset token with 24-hour expiration
  - Sends password reset email via Notification Service
  - Publishes PasswordResetRequested event

- `confirmPasswordReset(token, newPassword): void`
  - Validates reset token
  - Validates password meets requirements
  - Hashes new password
  - Updates user password
  - Invalidates reset token
  - Publishes PasswordResetConfirmed event

- `validateSessionToken(token): boolean`
  - Validates JWT signature
  - Checks token expiration
  - Checks if session is still valid
  - Returns true if valid, false otherwise

#### AuthorizationApplicationService

**Responsibilities**:
- Check user permissions for specific actions
- Enforce role-based access control
- Manage permission caching
- Handle authorization failures

**Key Methods**:
- `hasPermission(userId, permission): boolean`
  - Loads user from cache or repository
  - Gets user roles
  - Checks if any role has permission
  - Returns true if user has permission

- `hasRole(userId, roleId): boolean`
  - Loads user from cache or repository
  - Checks if user has specific role
  - Returns true if user has role

- `hasAnyRole(userId, roleIds): boolean`
  - Loads user from cache or repository
  - Checks if user has any of the specified roles
  - Returns true if user has at least one role

- `hasAllRoles(userId, roleIds): boolean`
  - Loads user from cache or repository
  - Checks if user has all specified roles
  - Returns true if user has all roles

- `canPerformAction(userId, action, resource): boolean`
  - Loads user from cache or repository
  - Gets user permissions
  - Checks if user can perform action on resource
  - Returns true if authorized

- `getEffectivePermissions(userId): List<Permission>`
  - Loads user from cache or repository
  - Gets all user roles
  - Aggregates permissions from all roles
  - Returns combined permission list

#### UserManagementApplicationService

**Responsibilities**:
- Create new users
- Update user information
- Manage user status
- Query users with various filters
- Coordinate user creation with role assignment

**Key Methods**:
- `createUser(email, password, profile): UserDTO`
  - Validates email uniqueness
  - Validates password meets requirements
  - Calls UserFactory to create user aggregate
  - Saves user to repository
  - Assigns default role (Employee)
  - Invalidates user cache
  - Publishes UserCreated event
  - Returns user DTO

- `updateUser(userId, profile): UserDTO`
  - Loads user from repository
  - Updates profile information
  - Validates office assignment
  - Saves user to repository
  - Invalidates user cache
  - Publishes UserUpdated event
  - Returns updated user DTO

- `suspendUser(userId, reason): UserDTO`
  - Loads user from repository
  - Updates status to Suspended
  - Saves user to repository
  - Invalidates user cache
  - Publishes UserSuspended event
  - Returns updated user DTO

- `activateUser(userId): UserDTO`
  - Loads user from repository
  - Updates status to Active
  - Saves user to repository
  - Invalidates user cache
  - Publishes UserActivated event
  - Returns updated user DTO

- `deleteUser(userId): void`
  - Loads user from repository
  - Updates status to Deleted (soft delete)
  - Saves user to repository
  - Invalidates user cache
  - Publishes UserDeleted event

- `getUser(userId): UserDTO`
  - Checks cache first
  - If not cached, loads from repository
  - Caches result (TTL: 1 hour)
  - Returns user DTO

- `listUsers(filters): Page<UserDTO>`
  - Applies filters (office, role, status)
  - Uses UserRepository specifications
  - Returns paginated results

- `searchUsers(query): List<UserDTO>`
  - Performs full-text search on email, firstName, lastName
  - Applies filters
  - Returns search results

#### RoleManagementApplicationService

**Responsibilities**:
- Create and manage roles
- Assign permissions to roles
- Query roles with various filters
- Manage role hierarchy

**Key Methods**:
- `createRole(name, description, permissions): RoleDTO`
  - Validates role name uniqueness
  - Validates permissions exist
  - Calls RoleFactory to create role aggregate
  - Saves role to repository
  - Invalidates role cache
  - Publishes RoleCreated event
  - Returns role DTO

- `updateRole(roleId, description, permissions): RoleDTO`
  - Loads role from repository
  - Updates description and permissions
  - Saves role to repository
  - Invalidates role cache
  - Publishes RoleUpdated event
  - Returns updated role DTO

- `getRole(roleId): RoleDTO`
  - Checks cache first
  - If not cached, loads from repository
  - Caches result (TTL: 1 hour)
  - Returns role DTO

- `listRoles(): List<RoleDTO>`
  - Queries all roles
  - Returns list of roles

- `getRolesByUser(userId): List<RoleDTO>`
  - Loads user from repository
  - Gets user roles
  - Returns list of roles

#### OfficeManagementApplicationService

**Responsibilities**:
- Create and manage offices
- Manage office hierarchy
- Validate office relationships
- Query offices with various filters

**Key Methods**:
- `createOffice(name, parentOfficeId, headOfOfficeId): OfficeDTO`
  - Validates office name uniqueness within parent
  - Validates parent office exists
  - Validates head of office exists and is in same office
  - Validates hierarchy is acyclic
  - Calls OfficeFactory to create office aggregate
  - Saves office to repository
  - Invalidates office cache
  - Publishes OfficeCreated event
  - Returns office DTO

- `updateOffice(officeId, name, headOfOfficeId): OfficeDTO`
  - Loads office from repository
  - Validates name uniqueness within parent
  - Validates head of office exists
  - Updates office information
  - Saves office to repository
  - Invalidates office cache
  - Publishes OfficeUpdated event
  - Returns updated office DTO

- `getOffice(officeId): OfficeDTO`
  - Checks cache first
  - If not cached, loads from repository
  - Caches result (TTL: 1 hour)
  - Returns office DTO

- `getOfficeHierarchy(officeId): OfficeHierarchyDTO`
  - Loads office from repository
  - Builds hierarchy tree (parent and children)
  - Returns hierarchy DTO

- `listOffices(parentOfficeId): List<OfficeDTO>`
  - Queries offices by parent
  - Returns list of offices

- `getOfficesByUser(userId): List<OfficeDTO>`
  - Loads user from repository
  - Gets user's office
  - Returns office DTO

#### SystemSettingsApplicationService

**Responsibilities**:
- Manage system configuration settings
- Validate setting values
- Cache settings for performance
- Handle setting versioning

**Key Methods**:
- `getSetting(key): SettingDTO`
  - Checks cache first
  - If not cached, loads from repository
  - Caches result (TTL: 1 hour)
  - Returns setting DTO

- `getAllSettings(): Map<String, SettingDTO>`
  - Checks cache first
  - If not cached, loads all settings from repository
  - Caches result (TTL: 1 hour)
  - Returns map of settings

- `updateSetting(key, value): SettingDTO`
  - Loads setting from repository
  - Validates value type and format
  - Saves old value for audit
  - Updates setting
  - Saves new version
  - Invalidates cache
  - Publishes SystemSettingsUpdated event
  - Returns updated setting DTO

- `testSetting(key, value): TestResultDTO`
  - Validates setting value without saving
  - Returns validation result

#### AuditApplicationService

**Responsibilities**:
- Log all system activities
- Query audit logs with various filters
- Export audit logs
- Manage audit log retention

**Key Methods**:
- `logAction(userId, action, resourceType, resourceId, details): void`
  - Creates audit log entry
  - Saves to repository
  - Publishes AuditLogCreated event

- `logLogin(userId, ipAddress): void`
  - Creates login audit log entry
  - Saves to repository

- `logLogout(userId): void`
  - Creates logout audit log entry
  - Saves to repository

- `logRoleAssignment(userId, roleId, assignedBy): void`
  - Creates role assignment audit log entry
  - Saves to repository

- `logSettingChange(settingKey, oldValue, newValue, changedBy): void`
  - Creates setting change audit log entry
  - Saves to repository

- `getAuditLogs(criteria): Page<AuditLogDTO>`
  - Applies filters (user, action, resource, date range)
  - Uses AuditLogRepository specifications
  - Returns paginated results

- `searchAuditLogs(query): List<AuditLogDTO>`
  - Performs full-text search
  - Applies filters
  - Returns search results

- `exportAuditLogs(fromDate, toDate, format): ExportedFile`
  - Queries audit logs in date range
  - Formats as CSV, PDF, or Excel
  - Returns exported file

#### ReportingApplicationService

**Responsibilities**:
- Generate reports on request processing
- Generate performance metrics
- Generate user activity reports
- Generate audit reports
- Provide dashboard metrics

**Key Methods**:
- `generateRequestReport(fromDate, toDate, filters): RequestReportDTO`
  - Queries requests in date range
  - Applies filters (office, access type, status)
  - Aggregates statistics
  - Calculates approval rates and processing times
  - Returns report DTO

- `generatePerformanceReport(fromDate, toDate): PerformanceReportDTO`
  - Queries requests in date range
  - Calculates average time by stage
  - Identifies bottlenecks
  - Calculates throughput
  - Returns report DTO

- `generateUserActivityReport(userId, fromDate, toDate): UserActivityReportDTO`
  - Queries audit logs for user
  - Aggregates activity statistics
  - Calculates activity by day
  - Returns report DTO

- `generateAuditReport(fromDate, toDate, filters): AuditReportDTO`
  - Queries audit logs in date range
  - Applies filters
  - Aggregates statistics
  - Returns report DTO

- `exportReport(reportType, fromDate, toDate, format): ExportedFile`
  - Generates appropriate report
  - Formats as CSV, PDF, or Excel
  - Returns exported file

- `getDashboardMetrics(): DashboardMetricsDTO`
  - Queries current request statistics
  - Calculates pending by stage
  - Calculates approval rate
  - Returns metrics DTO

- `getTrendData(period): TrendDataDTO`
  - Queries historical data
  - Groups by period (day, week, month)
  - Calculates trends
  - Returns trend data DTO

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
- Publishes to Kafka topic `admin.events`
- Handles serialization/deserialization
- Implements retry logic with exponential backoff
- Logs all published events
- Tracks publishing metrics

#### Published Events

**UserRoleAssigned**
- Trigger: When a role is assigned to a user
- Payload: userId, roleId, assignedAt, assignedBy
- Subscribers: Administration Service (audit log)

**UserRoleRevoked**
- Trigger: When a role is revoked from a user
- Payload: userId, roleId, revokedAt, revokedBy
- Subscribers: Administration Service (audit log)

**SystemSettingsUpdated**
- Trigger: When system settings are changed
- Payload: settingKey, oldValue, newValue, updatedAt, updatedBy
- Subscribers: All services (may need to refresh configuration)

**UserLoginSuccessful**
- Trigger: When user successfully logs in
- Payload: userId, loginAt, ipAddress
- Subscribers: Administration Service (audit log)

**UserLoginFailed**
- Trigger: When user login fails
- Payload: email, failedAt, reason, ipAddress
- Subscribers: Administration Service (audit log, security alert)

**UserStatusChanged**
- Trigger: When user status changes
- Payload: userId, oldStatus, newStatus, changedAt, changedBy
- Subscribers: Administration Service (audit log)

### Event Consumption Strategy

#### Event Listener Interface

```
interface DomainEventListener {
  void handle(DomainEvent event);
  String getEventType();
}
```

#### Kafka Event Listener Implementation

- Listens to Kafka topics `request.events`, `document.events`, `notification.events`
- Deserializes JSON to domain events
- Routes events to appropriate handlers
- Implements idempotency (prevents duplicate processing)
- Logs all consumed events
- Handles processing errors with dead letter queue

#### Consumed Events

**RequestCreated** (from Request Management Service)
- Handler: RequestCreatedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created for this request

**RequestSubmitted** (from Request Management Service)
- Handler: RequestSubmittedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**RequestApprovedByHeadOfOffice** (from Request Management Service)
- Handler: RequestApprovedByHeadOfOfficeEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**RequestEndorsed** (from Request Management Service)
- Handler: RequestEndorsedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**RequestFinallyApproved** (from Request Management Service)
- Handler: RequestFinallyApprovedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**RequestDeclined** (from Request Management Service)
- Handler: RequestDeclinedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**RequestReturned** (from Request Management Service)
- Handler: RequestReturnedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**RequestImplemented** (from Request Management Service)
- Handler: RequestImplementedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**DocumentUploaded** (from Document Management Service)
- Handler: DocumentUploadedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**DocumentDownloaded** (from Document Management Service)
- Handler: DocumentDownloadedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**DocumentDeleted** (from Document Management Service)
- Handler: DocumentDeletedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

**AccessTypeAdded** (from Request Management Service)
- Handler: AccessTypeAddedEventHandler
- Action: Create audit log entry
- Idempotency: Check if audit log already created

### Transaction Management

#### Transaction Boundaries

- Each application service method is a transaction boundary
- Uses Spring @Transactional annotation
- Propagation: REQUIRED (join existing or create new)
- Isolation: READ_COMMITTED (default)
- Rollback on RuntimeException

#### Saga Pattern for Distributed Transactions

For operations that span multiple aggregates or services:
- Use choreography-based sagas (event-driven)
- Each service publishes events that trigger next step
- Implement compensating transactions for rollback
- Example: User creation saga
  1. Create user aggregate
  2. Publish UserCreated event
  3. Assign default role (triggered by UserCreated event)
  4. Publish UserRoleAssigned event
  5. Create audit log entry (triggered by UserRoleAssigned event)


---

## Domain Layer Design

### Aggregates

The Domain Layer contains five main aggregates that encapsulate business logic and maintain invariants.

#### User Aggregate

**Aggregate Root**: User

**Entities within Aggregate**:
- **User** (Aggregate Root)
  - Unique identifier: UserId
  - Core attributes: email, hashedPassword, firstName, lastName, position, officeId, status, createdAt, updatedAt
  - Relationships: references to office, roles, sessions
  - Behavior: manages authentication, role assignments, session management

- **UserRole** (Entity)
  - Tracks role assignments
  - Attributes: roleId, assignedAt, assignedBy
  - Behavior: immutable once created

- **UserSession** (Entity)
  - Tracks user sessions
  - Attributes: sessionToken, createdAt, expiresAt, lastActivityAt
  - Behavior: manages session lifecycle

**Value Objects within Aggregate**:
- **UserId**: Unique identifier for the user (UUID)
- **Email**: Email address with validation
  - Validates email format (RFC 5322)
  - Normalizes to lowercase
  - Immutable
- **HashedPassword**: Encapsulates password hashing
  - Attributes: hash, salt, algorithm (bcrypt, PBKDF2)
  - Never exposes plain password
  - Immutable
- **UserStatus**: Enumeration of user states
  - Values: Active, Inactive, Suspended, Deleted
  - Validates state transitions
- **UserProfile**: Encapsulates user profile information
  - Attributes: firstName, lastName, position, officeId
  - Immutable
- **SessionToken**: Encapsulates session information
  - Attributes: token, createdAt, expiresAt, lastActivityAt
  - Validates token format
  - Immutable

**Aggregate Invariants**:
- A user must have a valid email
- A user must have a hashed password
- A user must be associated with an office
- A user must have at least one role
- A user cannot have duplicate roles
- A user session must not be expired
- A user cannot login if status is Suspended or Deleted
- Email must be unique across all users

**Aggregate Lifecycle**:
1. Created with Active status
2. Can be assigned multiple roles
3. Can create sessions upon login
4. Can be suspended or deactivated
5. Can be deleted (soft delete)

#### Role Aggregate

**Aggregate Root**: Role

**Entities within Aggregate**:
- **Role** (Aggregate Root)
  - Unique identifier: RoleId
  - Core attributes: name, description, permissions, createdAt, updatedAt
  - Relationships: references to permissions
  - Behavior: manages permissions, role hierarchy

- **Permission** (Entity)
  - Defines a specific permission
  - Attributes: permissionId, name, description, resource, action
  - Behavior: immutable

**Value Objects within Aggregate**:
- **RoleId**: Unique identifier for the role (UUID)
- **RoleName**: Name of the role
  - Values: Employee, HeadOfOffice, Reviewer, SmdHead, SystemAdmin, DbAdmin
  - Immutable
- **Permission**: Encapsulates permission information
  - Attributes: name, description, resource, action
  - Immutable
- **RoleHierarchy**: Encapsulates role relationships
  - Attributes: parentRoles (list), childRoles (list)
  - Validates hierarchy
  - Immutable

**Aggregate Invariants**:
- A role must have a name
- A role must have at least one permission
- Role names must be unique
- Role hierarchy must be acyclic

#### Office Aggregate

**Aggregate Root**: Office

**Entities within Aggregate**:
- **Office** (Aggregate Root)
  - Unique identifier: OfficeId
  - Core attributes: name, parentOfficeId, headOfOfficeId, createdAt, updatedAt
  - Relationships: references to parent office, head of office
  - Behavior: manages office hierarchy

**Value Objects within Aggregate**:
- **OfficeId**: Unique identifier for the office (UUID)
- **OfficeName**: Name of the office
  - Immutable
- **OfficeHierarchy**: Encapsulates office structure
  - Attributes: parentOfficeId, childOffices (list)
  - Validates hierarchy
  - Immutable

**Aggregate Invariants**:
- An office must have a name
- An office must have a head of office
- Office hierarchy must be acyclic
- Office names must be unique within parent office

#### SystemSettings Aggregate

**Aggregate Root**: SystemSettings

**Entities within Aggregate**:
- **SystemSettings** (Aggregate Root)
  - Unique identifier: SettingsId
  - Core attributes: settings (map of key-value pairs), version, createdAt, updatedAt
  - Behavior: manages configuration, versioning

- **SettingVersion** (Entity)
  - Tracks setting versions
  - Attributes: version, settings, createdAt, createdBy
  - Behavior: immutable

**Value Objects within Aggregate**:
- **SettingKey**: Key for a setting
  - Values: FILE_UPLOAD_SIZE_LIMIT, SESSION_TIMEOUT_MINUTES, EMAIL_SERVER_HOST, etc.
  - Immutable
- **SettingValue**: Value for a setting
  - Attributes: value, type (String, Integer, Boolean)
  - Validates value type
  - Immutable
- **SettingVersion**: Version information
  - Attributes: version, createdAt, createdBy
  - Immutable

**Aggregate Invariants**:
- Settings must have valid keys
- Settings must have valid values
- Settings must be versioned
- Settings changes must be audited

#### AuditLog Aggregate

**Aggregate Root**: AuditLog

**Entities within Aggregate**:
- **AuditLog** (Aggregate Root)
  - Unique identifier: AuditLogId
  - Core attributes: userId, action, resourceType, resourceId, timestamp, details
  - Behavior: immutable, append-only

**Value Objects within Aggregate**:
- **AuditLogId**: Unique identifier for the audit log entry (UUID)
- **AuditAction**: Type of action
  - Values: CREATE, READ, UPDATE, DELETE, APPROVE, DECLINE, LOGIN, LOGOUT, UPLOAD, DOWNLOAD, etc.
  - Immutable
- **AuditDetails**: Encapsulates audit details
  - Attributes: oldValue, newValue, reason, ipAddress
  - Immutable

**Aggregate Invariants**:
- An audit log entry must have a user
- An audit log entry must have an action
- An audit log entry must have a timestamp
- Audit log entries are immutable
- Audit log entries cannot be deleted

### Domain Services

#### AuthenticationService

**Purpose**: Validates user credentials and manages authentication.

**Responsibilities**:
- Validate email and password
- Create user sessions
- Validate session tokens
- Handle session expiration
- Support password reset

**Methods**:
- `authenticate(email, password): AuthenticationResult`
- `validateSessionToken(token): boolean`
- `createSession(userId): SessionToken`
- `invalidateSession(token): void`
- `isSessionExpired(token): boolean`
- `resetPassword(email): void`

#### AuthorizationService

**Purpose**: Checks user permissions and enforces access control.

**Responsibilities**:
- Check if user has permission
- Check if user has role
- Enforce role-based access control
- Handle authorization failures

**Methods**:
- `hasPermission(userId, permission): boolean`
- `hasRole(userId, role): boolean`
- `hasAnyRole(userId, roles): boolean`
- `hasAllRoles(userId, roles): boolean`
- `canPerformAction(userId, action, resource): boolean`

#### UserManagementService

**Purpose**: Manages users and role assignments.

**Responsibilities**:
- Create new users
- Update user information
- Assign roles to users
- Revoke roles from users
- Manage user status

**Methods**:
- `createUser(email, password, profile): User`
- `updateUser(userId, profile): void`
- `assignRole(userId, roleId): void`
- `revokeRole(userId, roleId): void`
- `suspendUser(userId): void`
- `activateUser(userId): void`
- `deleteUser(userId): void`

#### AuditService

**Purpose**: Logs all system activities.

**Responsibilities**:
- Log user actions
- Log system changes
- Maintain immutable audit trail
- Query audit logs

**Methods**:
- `logAction(userId, action, resourceType, resourceId, details): void`
- `logLogin(userId, ipAddress): void`
- `logLogout(userId): void`
- `logRoleAssignment(userId, roleId, assignedBy): void`
- `logSettingChange(settingKey, oldValue, newValue, changedBy): void`
- `getAuditLogs(criteria): List<AuditLog>`

#### ReportingService

**Purpose**: Generates reports and metrics.

**Responsibilities**:
- Generate request reports
- Generate performance metrics
- Generate user activity reports
- Generate audit reports

**Methods**:
- `generateRequestReport(startDate, endDate, filters): Report`
- `generatePerformanceMetrics(startDate, endDate): Metrics`
- `generateUserActivityReport(userId, startDate, endDate): Report`
- `generateAuditReport(startDate, endDate, filters): Report`
- `exportReport(report, format): ExportedReport`

### Repositories

#### UserRepository

**Purpose**: Persists and retrieves User aggregates.

**Query Methods**:
- `findById(userId): User`
- `findByEmail(email): User`
- `findByRole(roleId): List<User>`
- `findByOffice(officeId): List<User>`
- `findByStatus(status): List<User>`
- `findAll(): List<User>`
- `search(criteria): List<User>`

**Persistence Methods**:
- `save(user): void`
- `update(user): void`
- `delete(userId): void`

#### RoleRepository

**Purpose**: Persists and retrieves Role aggregates.

**Query Methods**:
- `findById(roleId): Role`
- `findByName(name): Role`
- `findAll(): List<Role>`
- `findByPermission(permission): List<Role>`

**Persistence Methods**:
- `save(role): void`
- `update(role): void`
- `delete(roleId): void`

#### OfficeRepository

**Purpose**: Persists and retrieves Office aggregates.

**Query Methods**:
- `findById(officeId): Office`
- `findByName(name): Office`
- `findAll(): List<Office>`
- `findByParentOffice(parentOfficeId): List<Office>`
- `getOfficeHierarchy(officeId): OfficeHierarchy`

**Persistence Methods**:
- `save(office): void`
- `update(office): void`
- `delete(officeId): void`

#### SystemSettingsRepository

**Purpose**: Persists and retrieves SystemSettings aggregates.

**Query Methods**:
- `findByKey(key): SettingValue`
- `findAll(): Map<SettingKey, SettingValue>`
- `findByVersion(version): Map<SettingKey, SettingValue>`
- `getLatestVersion(): Integer`

**Persistence Methods**:
- `save(key, value): void`
- `update(key, value): void`
- `saveVersion(version, settings): void`

#### AuditLogRepository

**Purpose**: Persists and retrieves AuditLog aggregates.

**Query Methods**:
- `findById(auditLogId): AuditLog`
- `findByUser(userId): List<AuditLog>`
- `findByAction(action): List<AuditLog>`
- `findByDateRange(startDate, endDate): List<AuditLog>`
- `findByResource(resourceType, resourceId): List<AuditLog>`
- `search(criteria): List<AuditLog>`

**Persistence Methods**:
- `save(auditLog): void`
- (No update or delete - immutable)

### Specifications (Query Objects)

#### UsersByRoleSpecification

**Purpose**: Query users with a specific role.

**Criteria**: roleId

**Returns**: List<User>

#### UsersByOfficeSpecification

**Purpose**: Query users in a specific office.

**Criteria**: officeId

**Returns**: List<User>

#### AuditLogsByDateRangeSpecification

**Purpose**: Query audit logs within a date range.

**Criteria**: startDate, endDate

**Returns**: List<AuditLog>

#### AuditLogsByActionSpecification

**Purpose**: Query audit logs by action type.

**Criteria**: action

**Returns**: List<AuditLog>

#### AuditLogsByUserSpecification

**Purpose**: Query audit logs by user.

**Criteria**: userId

**Returns**: List<AuditLog>

#### AuditLogsByResourceSpecification

**Purpose**: Query audit logs by resource.

**Criteria**: resourceType, resourceId

**Returns**: List<AuditLog>

#### SuspiciousActivitySpecification

**Purpose**: Query suspicious activities (multiple failed logins, etc.).

**Criteria**: timeWindow, failureThreshold

**Returns**: List<AuditLog>

### Policies

#### AuthenticationPolicy

**Purpose**: Defines authentication rules and constraints.

**Rules**:
- Password must be at least 8 characters
- Password must contain uppercase, lowercase, numbers, and special characters
- Session timeout: 30 minutes of inactivity
- Maximum login attempts: 5 before account suspension
- Account suspension duration: 15 minutes
- Password reset token expires in 24 hours

**Validation Methods**:
- `isValidPassword(password): boolean`
- `isSessionValid(token): boolean`
- `isAccountLocked(email): boolean`
- `canAttemptLogin(email): boolean`

#### AuthorizationPolicy

**Purpose**: Defines role-based access control rules.

**Rules**:
- Employee: Can create requests, view own requests
- Head of Office: Can approve requests from team members
- Reviewer: Can review and endorse requests
- SMD/RDC Head: Can approve requests for implementation
- System Admin: Can manage system configuration, view all requests
- DB Admin: Can manage database access requests
- Users can have multiple roles
- Permissions are cumulative across roles

**Authorization Methods**:
- `isAuthorizedForAction(userId, action, resource): boolean`
- `getEffectivePermissions(userId): List<Permission>`

#### RoleAssignmentPolicy

**Purpose**: Validates role assignments against hierarchy.

**Rules**:
- Users must have at least one role
- Role assignments must respect office hierarchy
- Head of Office must be from same office
- Reviewer must be from SMD/RDC division
- Role assignments cannot create circular dependencies

**Validation Methods**:
- `isValidRoleAssignment(userId, roleId): boolean`
- `validateRoleHierarchy(roles): ValidationResult`

#### AuditPolicy

**Purpose**: Defines what actions to audit.

**Rules**:
- All user actions are audited
- All system changes are audited
- All login attempts are audited
- All role changes are audited
- All setting changes are audited
- Audit logs are immutable
- Audit logs are retained for 7 years (configurable)

**Audit Methods**:
- `shouldAudit(action): boolean`
- `getRetentionPeriod(): Integer`

### Factory Patterns

#### UserFactory

**Purpose**: Creates User aggregates with validation.

**Responsibilities**:
- Validate email uniqueness
- Hash password securely
- Generate unique UserId
- Initialize user with default role
- Create user in Active status

**Creation Method**:
- `createUser(email, password, profile): User`
  - Validates email format and uniqueness
  - Hashes password with salt
  - Generates UserId
  - Creates user in Active status
  - Returns new User aggregate

#### RoleFactory

**Purpose**: Creates Role aggregates with validation.

**Responsibilities**:
- Validate role name uniqueness
- Validate permissions
- Generate unique RoleId
- Initialize role with permissions

**Creation Method**:
- `createRole(name, description, permissions): Role`
  - Validates name is unique
  - Validates permissions exist
  - Generates RoleId
  - Returns new Role aggregate

#### OfficeFactory

**Purpose**: Creates Office aggregates with hierarchy.

**Responsibilities**:
- Validate office name uniqueness
- Validate office hierarchy
- Generate unique OfficeId
- Initialize office with parent relationship

**Creation Method**:
- `createOffice(name, parentOfficeId, headOfOfficeId): Office`
  - Validates name is unique within parent
  - Validates hierarchy is acyclic
  - Generates OfficeId
  - Returns new Office aggregate

#### AuditLogFactory

**Purpose**: Creates immutable AuditLog entries.

**Responsibilities**:
- Create immutable log entries
- Capture all relevant information
- Timestamp all entries

**Creation Method**:
- `createAuditLogEntry(userId, action, resourceType, resourceId, details): AuditLog`
  - Captures current timestamp
  - Creates immutable entry
  - Returns AuditLog entity

#### SystemSettingsFactory

**Purpose**: Creates SystemSettings with versioning.

**Responsibilities**:
- Create settings with validation
- Initialize versioning
- Set default values

**Creation Method**:
- `createSettings(settingsMap): SystemSettings`
  - Validates all settings
  - Initializes version 1
  - Returns new SystemSettings aggregate


---

## Infrastructure Layer Design

### Repository Implementations

#### UserRepository (JPA Implementation)

**Technology**: Spring Data JPA with Hibernate

**Database Table**: `users`

**Columns**:
- `user_id` (UUID, Primary Key)
- `email` (VARCHAR, Unique)
- `hashed_password` (VARCHAR)
- `password_salt` (VARCHAR)
- `password_algorithm` (VARCHAR)
- `first_name` (VARCHAR)
- `last_name` (VARCHAR)
- `position` (VARCHAR)
- `office_id` (UUID, Foreign Key)
- `status` (VARCHAR)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Indexes**:
- Primary key on `user_id`
- Unique index on `email`
- Index on `office_id`
- Index on `status`
- Index on `created_at`

**Query Methods Implementation**:
- Uses Spring Data JPA query methods
- Implements custom queries for complex searches
- Uses Specification pattern for dynamic queries
- Caches results in Redis (TTL: 1 hour)

#### RoleRepository (JPA Implementation)

**Technology**: Spring Data JPA with Hibernate

**Database Table**: `roles`

**Columns**:
- `role_id` (UUID, Primary Key)
- `role_name` (VARCHAR, Unique)
- `description` (TEXT)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Related Tables**:
- `role_permissions` (junction table)
  - `role_id` (UUID, Foreign Key)
  - `permission_id` (UUID, Foreign Key)

**Indexes**:
- Primary key on `role_id`
- Unique index on `role_name`
- Index on `created_at`

#### OfficeRepository (JPA Implementation)

**Technology**: Spring Data JPA with Hibernate

**Database Table**: `offices`

**Columns**:
- `office_id` (UUID, Primary Key)
- `name` (VARCHAR)
- `level` (INTEGER)
- `parent_office_id` (UUID, Foreign Key, Nullable)
- `head_of_office_id` (UUID, Foreign Key)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Indexes**:
- Primary key on `office_id`
- Index on `parent_office_id`
- Index on `head_of_office_id`
- Index on `level`
- Unique index on (name, parent_office_id)

#### SystemSettingsRepository (JPA Implementation)

**Technology**: Spring Data JPA with Hibernate

**Database Table**: `system_settings`

**Columns**:
- `setting_id` (UUID, Primary Key)
- `setting_key` (VARCHAR, Unique)
- `setting_value` (TEXT)
- `setting_type` (VARCHAR)
- `version` (INTEGER)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Related Tables**:
- `setting_versions` (audit trail)
  - `version_id` (UUID, Primary Key)
  - `setting_id` (UUID, Foreign Key)
  - `version` (INTEGER)
  - `settings_json` (JSONB)
  - `created_at` (TIMESTAMP)
  - `created_by` (UUID)

**Indexes**:
- Primary key on `setting_id`
- Unique index on `setting_key`
- Index on `version`

#### AuditLogRepository (JPA Implementation)

**Technology**: Spring Data JPA with Hibernate

**Database Table**: `audit_logs`

**Columns**:
- `audit_log_id` (UUID, Primary Key)
- `user_id` (UUID, Foreign Key)
- `action` (VARCHAR)
- `resource_type` (VARCHAR)
- `resource_id` (VARCHAR)
- `timestamp` (TIMESTAMP)
- `old_value` (TEXT, Nullable)
- `new_value` (TEXT, Nullable)
- `reason` (TEXT, Nullable)
- `ip_address` (VARCHAR, Nullable)

**Indexes**:
- Primary key on `audit_log_id`
- Index on `user_id`
- Index on `action`
- Index on `resource_type`
- Index on `resource_id`
- Index on `timestamp`
- Composite index on (user_id, timestamp)
- Composite index on (action, timestamp)

**Partitioning Strategy**:
- Partition by month on `timestamp` for performance
- Older partitions can be archived

### Event Bus Integration (Kafka)

#### Event Publisher

**Technology**: Spring Kafka

**Kafka Topics**:
- `admin.events`: All events published by Administration Service

**Event Publishing Flow**:
1. Domain aggregate publishes event (added to event list)
2. Application service saves aggregate to repository
3. Application service calls EventPublisher.publishAll()
4. EventPublisher converts events to JSON
5. EventPublisher publishes to Kafka topic
6. Kafka broker stores event
7. Other services consume events from Kafka

**Error Handling**:
- Retry logic with exponential backoff (3 retries, 1s initial delay, 2x multiplier)
- Dead letter queue for permanently failed messages
- Logging of all publishing failures

#### Event Listener

**Technology**: Spring Kafka

**Kafka Topics Consumed**:
- `request.events`: All events from Request Management Service
- `document.events`: All events from Document Management Service
- `notification.events`: All events from Notification Service

**Event Consumption Flow**:
1. Kafka consumer listens to topics
2. Receives event message
3. Deserializes JSON to domain event
4. Routes event to appropriate handler
5. Handler processes event (creates audit log entry)
6. Handler publishes response event if needed
7. Kafka consumer acknowledges message

**Idempotency Strategy**:
- Store processed event IDs in database
- Check if event already processed before handling
- Prevents duplicate processing of same event

**Error Handling**:
- Retry logic with exponential backoff
- Dead letter queue for permanently failed messages
- Logging of all consumption failures

### Database Access Layer

#### Connection Pooling

**Technology**: HikariCP (default in Spring Boot)

**Configuration**:
- Maximum pool size: 20
- Minimum idle connections: 5
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes
- Max lifetime: 30 minutes

#### Transaction Management

**Technology**: Spring @Transactional

**Configuration**:
- Propagation: REQUIRED
- Isolation: READ_COMMITTED
- Rollback on RuntimeException
- Read-only for query methods

### Redis Cache Integration

#### Cache Strategy

**Technology**: Spring Data Redis

**Cached Data**:
- User roles and permissions (TTL: 1 hour)
- Office hierarchy (TTL: 1 hour)
- System settings (TTL: 1 hour)
- Role definitions (TTL: 1 hour)

**Cache Keys**:
- `user:{userId}:roles` - User roles
- `user:{userId}:permissions` - User permissions
- `office:{officeId}:hierarchy` - Office hierarchy
- `role:{roleId}` - Role definition
- `settings:all` - All system settings
- `setting:{key}` - Specific setting

**Cache Invalidation**:
- Manual invalidation on update
- TTL-based expiration
- Event-based invalidation (when settings change)

### Spring Security Integration

#### Authentication

**Technology**: Spring Security with JWT

**Authentication Flow**:
1. User submits email and password
2. AuthenticationProvider validates credentials
3. If valid, generates JWT token
4. Token includes user ID, roles, and expiration
5. Client includes token in Authorization header for subsequent requests

**JWT Token Structure**:
- Header: Algorithm (HS256)
- Payload: userId, roles, email, expiresAt
- Signature: HMAC-SHA256 with secret key

#### Authorization

**Technology**: Spring Security with @PreAuthorize

**Authorization Flow**:
1. Request arrives with JWT token
2. JwtAuthenticationFilter extracts token
3. JwtTokenProvider validates token
4. UserDetailsService loads user details
5. SecurityContext stores authentication
6. @PreAuthorize checks permissions
7. If authorized, request proceeds
8. If not authorized, returns 403 Forbidden

**Role-Based Access Control**:
- Uses Spring Security's role hierarchy
- Supports multi-role users
- Permissions are cumulative across roles

### Logging and Monitoring

#### Logging

**Technology**: SLF4J with Logback

**Log Levels**:
- ERROR: System errors, exceptions
- WARN: Warnings, suspicious activities
- INFO: Important events (login, role assignment, settings change)
- DEBUG: Detailed information for debugging
- TRACE: Very detailed information

**Log Output**:
- Console: All log levels
- File: All log levels (rotated daily, 30-day retention)
- Structured logging: JSON format for ELK stack integration

#### Monitoring

**Technology**: Spring Boot Actuator with Micrometer

**Metrics**:
- HTTP request metrics (count, duration, status codes)
- Database connection pool metrics
- Cache hit/miss rates
- Event publishing/consumption metrics
- Authentication/authorization metrics

**Health Checks**:
- Database connectivity
- Kafka connectivity
- Redis connectivity
- Disk space
- Memory usage

**Endpoints**:
- `/actuator/health`: Overall health status
- `/actuator/metrics`: Available metrics
- `/actuator/metrics/{metric}`: Specific metric details


---

## Data Models & Database Design

### Database Schema Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Administration Service Database           │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Users & Authentication                                     │
│  ├─ users                                                   │
│  ├─ user_sessions                                           │
│  ├─ password_reset_tokens                                   │
│  └─ login_attempts                                          │
│                                                              │
│  Roles & Permissions                                        │
│  ├─ roles                                                   │
│  ├─ permissions                                             │
│  ├─ role_permissions (junction)                             │
│  └─ user_roles (junction)                                   │
│                                                              │
│  Offices & Hierarchy                                        │
│  ├─ offices                                                 │
│  └─ office_hierarchy (materialized view)                    │
│                                                              │
│  System Configuration                                       │
│  ├─ system_settings                                         │
│  └─ setting_versions (audit trail)                          │
│                                                              │
│  Audit & Compliance                                         │
│  ├─ audit_logs                                              │
│  └─ audit_log_archive (for old records)                     │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Table Relationships

```
users (1) ──────────────── (M) user_roles
  │                              │
  │                              │
  └──────────────────────────────┘
                                  │
                                  │
                                (M) roles
                                  │
                                  │
                                (M) role_permissions
                                  │
                                  │
                                (M) permissions

users (M) ──────────────── (1) offices
  │
  │
(M) user_sessions

users (M) ──────────────── (M) audit_logs

offices (1) ──────────────── (M) offices (self-referencing for hierarchy)

system_settings (1) ──────────────── (M) setting_versions
```

### Data Ownership Boundaries

**Administration Service owns and writes to**:
- users
- user_sessions
- password_reset_tokens
- login_attempts
- roles
- permissions
- role_permissions
- user_roles
- offices
- system_settings
- setting_versions
- audit_logs

**Other services can read from**:
- users (for user information)
- roles (for authorization)
- user_roles (for authorization)
- offices (for office hierarchy)
- system_settings (for configuration)
- audit_logs (for audit trail)

---

## Event Handling Design

### Published Events

#### UserRoleAssigned
- **Trigger**: When a role is assigned to a user
- **Payload**: userId, roleId, roleName, assignedAt, assignedBy
- **Subscribers**: Administration Service (audit log)
- **Topic**: `admin.events`

#### UserRoleRevoked
- **Trigger**: When a role is revoked from a user
- **Payload**: userId, roleId, roleName, revokedAt, revokedBy
- **Subscribers**: Administration Service (audit log)
- **Topic**: `admin.events`

#### SystemSettingsUpdated
- **Trigger**: When system settings are changed
- **Payload**: settingKey, oldValue, newValue, updatedAt, updatedBy
- **Subscribers**: All services (may need to refresh configuration)
- **Topic**: `admin.events`

#### UserLoginSuccessful
- **Trigger**: When user successfully logs in
- **Payload**: userId, loginAt, ipAddress
- **Subscribers**: Administration Service (audit log)
- **Topic**: `admin.events`

#### UserLoginFailed
- **Trigger**: When user login fails
- **Payload**: email, failedAt, reason, ipAddress
- **Subscribers**: Administration Service (audit log, security alert)
- **Topic**: `admin.events`

#### UserStatusChanged
- **Trigger**: When user status changes
- **Payload**: userId, oldStatus, newStatus, changedAt, changedBy
- **Subscribers**: Administration Service (audit log)
- **Topic**: `admin.events`

### Consumed Events

#### RequestCreated (from Request Management Service)
- **Purpose**: Log audit entry
- **Handler**: RequestCreatedEventHandler
- **Action**: Create AuditLog entry with action=CREATE, resourceType=REQUEST
- **Idempotency**: Check if audit log already created for this request

#### RequestSubmitted (from Request Management Service)
- **Purpose**: Log audit entry
- **Handler**: RequestSubmittedEventHandler
- **Action**: Create AuditLog entry with action=SUBMIT, resourceType=REQUEST
- **Idempotency**: Check if audit log already created

#### RequestApprovedByHeadOfOffice (from Request Management Service)
- **Purpose**: Log audit entry
- **Handler**: RequestApprovedByHeadOfOfficeEventHandler
- **Action**: Create AuditLog entry with action=APPROVE, resourceType=REQUEST
- **Idempotency**: Check if audit log already created

#### RequestEndorsed (from Request Management Service)
- **Purpose**: Log audit entry
- **Handler**: RequestEndorsedEventHandler
- **Action**: Create AuditLog entry with action=ENDORSE, resourceType=REQUEST
- **Idempotency**: Check if audit log already created

#### RequestFinallyApproved (from Request Management Service)
- **Purpose**: Log audit entry
- **Handler**: RequestFinallyApprovedEventHandler
- **Action**: Create AuditLog entry with action=FINALLY_APPROVE, resourceType=REQUEST
- **Idempotency**: Check if audit log already created

#### RequestDeclined (from Request Management Service)
- **Purpose**: Log audit entry
- **Handler**: RequestDeclinedEventHandler
- **Action**: Create AuditLog entry with action=DECLINE, resourceType=REQUEST
- **Idempotency**: Check if audit log already created

#### RequestReturned (from Request Management Service)
- **Purpose**: Log audit entry
- **Handler**: RequestReturnedEventHandler
- **Action**: Create AuditLog entry with action=RETURN, resourceType=REQUEST
- **Idempotency**: Check if audit log already created

#### RequestImplemented (from Request Management Service)
- **Purpose**: Log audit entry
- **Handler**: RequestImplementedEventHandler
- **Action**: Create AuditLog entry with action=IMPLEMENT, resourceType=REQUEST
- **Idempotency**: Check if audit log already created

#### DocumentUploaded (from Document Management Service)
- **Purpose**: Log audit entry
- **Handler**: DocumentUploadedEventHandler
- **Action**: Create AuditLog entry with action=UPLOAD, resourceType=DOCUMENT
- **Idempotency**: Check if audit log already created

#### DocumentDownloaded (from Document Management Service)
- **Purpose**: Log audit entry
- **Handler**: DocumentDownloadedEventHandler
- **Action**: Create AuditLog entry with action=DOWNLOAD, resourceType=DOCUMENT
- **Idempotency**: Check if audit log already created

#### DocumentDeleted (from Document Management Service)
- **Purpose**: Log audit entry
- **Handler**: DocumentDeletedEventHandler
- **Action**: Create AuditLog entry with action=DELETE, resourceType=DOCUMENT
- **Idempotency**: Check if audit log already created

#### AccessTypeAdded (from Request Management Service)
- **Purpose**: Log audit entry
- **Handler**: AccessTypeAddedEventHandler
- **Action**: Create AuditLog entry with action=CREATE, resourceType=ACCESS_TYPE
- **Idempotency**: Check if audit log already created

---

## Error Handling & Validation

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
    "path": "/api/v1/endpoint"
  }
}
```

### Common Error Codes

**Authentication Errors**:
- `INVALID_CREDENTIALS`: Email or password is incorrect
- `INVALID_EMAIL_FORMAT`: Email format is invalid
- `INVALID_PASSWORD_FORMAT`: Password does not meet requirements
- `UNAUTHORIZED`: Authentication required
- `TOKEN_EXPIRED`: JWT token has expired
- `INVALID_TOKEN`: JWT token is invalid

**Authorization Errors**:
- `FORBIDDEN`: Insufficient permissions
- `INSUFFICIENT_ROLE`: User does not have required role

**User Management Errors**:
- `USER_NOT_FOUND`: User does not exist
- `DUPLICATE_EMAIL`: Email already exists
- `USER_SUSPENDED`: User account is suspended
- `USER_DELETED`: User account is deleted
- `INVALID_USER_STATUS`: Invalid user status transition

**Role Management Errors**:
- `ROLE_NOT_FOUND`: Role does not exist
- `DUPLICATE_ROLE_NAME`: Role name already exists
- `INVALID_ROLE_ASSIGNMENT`: Role assignment is invalid
- `INVALID_ROLE_HIERARCHY`: Role hierarchy is invalid

**Office Management Errors**:
- `OFFICE_NOT_FOUND`: Office does not exist
- `DUPLICATE_OFFICE_NAME`: Office name already exists
- `INVALID_OFFICE_HIERARCHY`: Office hierarchy is invalid (circular reference)
- `INVALID_HEAD_OF_OFFICE`: Head of office is invalid

**Settings Errors**:
- `SETTING_NOT_FOUND`: Setting does not exist
- `INVALID_SETTING_VALUE`: Setting value is invalid
- `INVALID_SETTING_TYPE`: Setting type is invalid

**Validation Errors**:
- `INVALID_INPUT`: Input validation failed
- `MISSING_REQUIRED_FIELD`: Required field is missing
- `INVALID_FORMAT`: Field format is invalid

**Rate Limiting Errors**:
- `RATE_LIMIT_EXCEEDED`: Too many requests
- `TOO_MANY_LOGIN_ATTEMPTS`: Too many failed login attempts

**System Errors**:
- `INTERNAL_ERROR`: Internal server error
- `DATABASE_ERROR`: Database error
- `KAFKA_ERROR`: Message queue error
- `CACHE_ERROR`: Cache error

### Validation Strategies

#### Input Validation

**Email Validation**:
- Format: RFC 5322 compliant
- Uniqueness: Check against existing users
- Normalization: Convert to lowercase

**Password Validation**:
- Minimum length: 8 characters
- Must contain: uppercase, lowercase, numbers, special characters
- Cannot contain: user's email or name

**Office Hierarchy Validation**:
- Acyclic: No circular references
- Consistency: Parent office must exist
- Head of office: Must be a user in the same office

**Role Assignment Validation**:
- User must have at least one role
- Role must exist
- Cannot assign same role twice

#### Business Logic Validation

**User Status Transitions**:
- Active → Suspended, Inactive, Deleted
- Suspended → Active, Deleted
- Inactive → Active, Deleted
- Deleted → (no transitions allowed)

**Session Validation**:
- Token must not be expired
- Token must be valid (correct signature)
- Session must not be invalidated

**Setting Value Validation**:
- Type must match setting definition
- Value must be within allowed range
- Value must pass custom validation rules


---

## Authentication & Authorization Design

### Authentication Mechanism

#### JWT Token Generation

**Token Structure**:
```
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "userId": "string (UUID)",
  "email": "string",
  "roles": ["string"],
  "iat": "number (issued at)",
  "exp": "number (expiration)",
  "iss": "Administration Service"
}

Signature: HMAC-SHA256(header + payload, secret_key)
```

**Token Generation Flow**:
1. User submits email and password
2. AuthenticationService validates credentials
3. Retrieves user from database
4. Compares password hash
5. If valid, generates JWT token
6. Token includes userId, email, roles
7. Token expiration: 30 minutes
8. Returns token to client

**Token Validation Flow**:
1. Client includes token in Authorization header
2. JwtAuthenticationFilter extracts token
3. JwtTokenProvider validates signature
4. Checks token expiration
5. If valid, extracts user information
6. Creates Authentication object
7. Stores in SecurityContext

#### Session Management

**Session Creation**:
- Created upon successful login
- Stores session token (JWT)
- Stores creation time
- Stores expiration time (30 minutes from creation)
- Stores last activity time

**Session Expiration**:
- Automatic expiration after 30 minutes of inactivity
- Refresh token endpoint to extend session
- Logout endpoint to invalidate session

**Session Invalidation**:
- User logout
- Password change
- Role revocation
- User suspension
- User deletion

#### Password Management

**Password Hashing**:
- Algorithm: bcrypt
- Cost factor: 12
- Salt: Generated per user
- Never store plain password

**Password Reset Flow**:
1. User requests password reset
2. System generates reset token (UUID)
3. Token stored with 24-hour expiration
4. Email sent to user with reset link
5. User clicks link and submits new password
6. System validates reset token
7. Validates new password meets requirements
8. Hashes new password
9. Updates user password
10. Invalidates reset token

**Password Requirements**:
- Minimum length: 8 characters
- Must contain: uppercase, lowercase, numbers, special characters
- Cannot contain: user's email or name
- Cannot reuse last 5 passwords

### Authorization Mechanism

#### Role-Based Access Control (RBAC)

**Role Hierarchy**:
```
Employee
  ├─ Create requests
  ├─ View own requests
  └─ Upload documents

Head of Office
  ├─ All Employee permissions
  ├─ Approve requests from team
  └─ View team requests

Reviewer (SMD/RDC)
  ├─ Review requests
  ├─ Endorse requests
  ├─ View all requests
  └─ Download documents

SMD/RDC Head
  ├─ All Reviewer permissions
  ├─ Approve requests for implementation
  ├─ Return requests to reviewer
  └─ View reports

System Administrator
  ├─ Implement OS/Web App access
  ├─ View assigned requests
  └─ Mark requests as implemented

Database Administrator
  ├─ Implement database access
  ├─ View assigned requests
  └─ Mark requests as implemented

System Administrator (Super)
  ├─ All permissions
  ├─ Manage users and roles
  ├─ Configure system settings
  ├─ View audit logs
  └─ Generate reports
```

**Permission Checking**:
1. Extract user from SecurityContext
2. Get user roles
3. For each role, get permissions
4. Aggregate permissions from all roles
5. Check if user has required permission
6. Return true if authorized, false otherwise

**Multi-Role Support**:
- Users can have multiple roles
- Permissions are cumulative
- User can perform action if ANY role has permission
- Effective permissions = Union of all role permissions

#### Authorization Policies

**Resource-Based Authorization**:
- User can view own profile
- Administrator can view any profile
- User can view requests they created
- Head of Office can view requests from their team
- Reviewer can view all requests
- Administrator can view all requests

**Action-Based Authorization**:
- Only Administrator can create users
- Only Administrator can assign roles
- Only Administrator can manage settings
- Only Administrator can view audit logs
- Only Administrator can generate reports

**Office-Based Authorization**:
- Head of Office can only approve requests from their office
- Users can only be assigned to their office
- Office hierarchy must be respected

#### Authorization Enforcement

**Spring Security Integration**:
- Uses @PreAuthorize annotation
- Supports SpEL (Spring Expression Language)
- Examples:
  - `@PreAuthorize("hasRole('ADMIN')")`
  - `@PreAuthorize("hasPermission(#userId, 'VIEW')")`
  - `@PreAuthorize("@authorizationService.canPerformAction(#userId, #action, #resource)")`

**Custom Authorization Service**:
- AuthorizationService provides custom authorization logic
- Checks user roles and permissions
- Checks office hierarchy
- Checks resource ownership
- Returns true if authorized, false otherwise

---

## Reporting & Analytics Design

### Report Types

#### Request Report

**Purpose**: Analyze request processing statistics

**Metrics**:
- Total requests
- Approved requests
- Declined requests
- Pending requests
- Implemented requests
- Approval rate (percentage)
- Average processing time (hours)

**Dimensions**:
- By status (Created, Submitted, Approved, Endorsed, Finally Approved, Implemented, Declined)
- By access type (OS Access, Web App Access, Database Access)
- By office (breakdown by office)
- By date (daily, weekly, monthly)

**Filters**:
- Date range
- Office
- Access type
- Status

#### Performance Report

**Purpose**: Identify bottlenecks and performance issues

**Metrics**:
- Average time by stage:
  - Created to Submitted
  - Submitted to Approved by Head of Office
  - Approved to Endorsed
  - Endorsed to Finally Approved
  - Approved to Implemented
- Bottlenecks (stages with longest average time)
- Throughput (requests per day/week/month)

**Filters**:
- Date range

#### User Activity Report

**Purpose**: Track user activities and engagement

**Metrics**:
- Requests created
- Requests approved
- Requests declined
- Requests implemented
- Login count
- Last login time
- Activity by day

**Filters**:
- User ID
- Date range

#### Audit Report

**Purpose**: Compliance and security auditing

**Metrics**:
- Total audit log entries
- By action type
- By resource type
- By user
- By date

**Filters**:
- Date range
- User
- Action
- Resource type

### Dashboard Design

**Real-Time Metrics**:
- Total requests (all time)
- Pending requests (current)
- Approved requests (current)
- Declined requests (current)
- Implemented requests (current)
- Pending by stage (breakdown)
- Completed today
- Average processing time
- Approval rate

**Trend Data**:
- Requests by date (line chart)
- Approval rate by date (line chart)
- Processing time by date (line chart)
- Requests by access type (pie chart)
- Requests by office (bar chart)

**Alerts**:
- High pending request count
- Low approval rate
- Long processing time
- Suspicious activities

### Metrics Collection Strategy

**Real-Time Metrics**:
- Collected from current database state
- Cached for 5 minutes
- Updated on request

**Historical Metrics**:
- Aggregated daily
- Stored in separate analytics tables
- Used for trend analysis

**Aggregation Strategy**:
- Daily aggregation job (runs at midnight)
- Aggregates metrics by day
- Stores in analytics tables
- Enables fast trend queries

**Caching Strategy**:
- Dashboard metrics cached for 5 minutes
- Report data cached for 1 hour
- Cache invalidated on data changes

### Report Generation & Export

**Supported Formats**:
- CSV: Comma-separated values
- PDF: Portable Document Format
- Excel: Microsoft Excel format

**Export Flow**:
1. User requests report export
2. System generates report data
3. Formats data according to requested format
4. Creates file
5. Returns file to user for download

**Scheduled Reports**:
- Daily request report (sent to administrators)
- Weekly performance report (sent to administrators)
- Monthly audit report (sent to compliance team)

---

## Office Hierarchy Management Design

### Office Structure

**Hierarchy Levels**:
```
Level 0: Organization (root)
  │
  ├─ Level 1: ISG (Information Systems Group)
  │   ├─ Level 2: SMD (Security Management Division)
  │   ├─ Level 2: RDC (Revenue Data Center)
  │   └─ Level 2: Operations
  │
  ├─ Level 1: Finance
  │
  └─ Level 1: HR
```

**Office Attributes**:
- Office ID (UUID)
- Office name
- Level (0-N)
- Parent office ID
- Head of office ID
- Creation date
- Update date

### Hierarchy Validation

**Acyclic Validation**:
- No circular references
- Parent cannot be child of itself
- Prevents infinite loops

**Consistency Validation**:
- Parent office must exist
- Head of office must exist
- Head of office must be a user
- Head of office must be in same office

**Uniqueness Validation**:
- Office name must be unique within parent office
- Allows same name in different parent offices

### Office-Based Access Control

**Office Hierarchy Traversal**:
- Get parent office
- Get child offices
- Get all ancestors
- Get all descendants

**Office-Based Filtering**:
- Filter users by office
- Filter requests by office
- Filter audit logs by office

**Head of Office Assignment**:
- Head of office must be from same office
- Head of office must have HeadOfOffice role
- Only one head of office per office

### Office Hierarchy Queries

**Query Methods**:
- `getOffice(officeId): Office`
- `getOfficeHierarchy(officeId): OfficeHierarchy`
- `getParentOffice(officeId): Office`
- `getChildOffices(officeId): List<Office>`
- `getAllAncestors(officeId): List<Office>`
- `getAllDescendants(officeId): List<Office>`
- `getOfficesByLevel(level): List<Office>`

**Caching Strategy**:
- Cache office hierarchy (TTL: 1 hour)
- Cache parent-child relationships
- Invalidate cache on office updates

---

## Security Design

### Encryption

**Data at Rest**:
- Database passwords encrypted with AES-256
- Sensitive settings encrypted with AES-256
- Audit logs not encrypted (immutable instead)

**Data in Transit**:
- All API endpoints use HTTPS/TLS 1.2+
- JWT tokens signed with HMAC-SHA256
- Kafka messages encrypted with TLS

### Audit Logging for Security Events

**Security Events Logged**:
- Login attempts (successful and failed)
- Password changes
- Password resets
- Role assignments and revocations
- User suspensions and deletions
- Settings changes
- Failed authorization attempts
- Rate limit violations

**Audit Log Details**:
- User ID
- Action
- Resource type and ID
- Timestamp
- IP address
- Old value (for updates)
- New value (for updates)
- Reason (if provided)

### Rate Limiting

**Login Rate Limiting**:
- Maximum 5 failed login attempts per email per 15 minutes
- Account suspension for 15 minutes after exceeding limit
- Logs all failed attempts

**API Rate Limiting**:
- 100 requests per minute per user
- 1000 requests per minute per IP
- Returns 429 Too Many Requests when exceeded

### CORS Configuration

**Allowed Origins**:
- Configured per environment
- Development: localhost:3000, localhost:4200
- Production: Specific domain

**Allowed Methods**:
- GET, POST, PUT, DELETE, OPTIONS

**Allowed Headers**:
- Content-Type
- Authorization
- X-Requested-With

**Credentials**:
- Allowed (for JWT token in Authorization header)

---

## Performance & Scalability

### Caching Strategies

**Redis Cache**:
- User roles and permissions (TTL: 1 hour)
- Office hierarchy (TTL: 1 hour)
- System settings (TTL: 1 hour)
- Role definitions (TTL: 1 hour)
- Dashboard metrics (TTL: 5 minutes)

**Cache Invalidation**:
- Manual invalidation on update
- TTL-based expiration
- Event-based invalidation (when settings change)

### Query Optimization

**Database Indexes**:
- Primary keys on all tables
- Unique indexes on email, role name, office name
- Foreign key indexes
- Composite indexes on frequently queried columns
- Indexes on timestamp columns for date range queries

**Query Optimization**:
- Use SELECT specific columns instead of SELECT *
- Use pagination for large result sets
- Use database-level filtering instead of application-level
- Use prepared statements to prevent SQL injection

### Batch Processing

**Audit Log Batch Processing**:
- Batch size: 1000 records
- Process failed audit logs in batches
- Retry with exponential backoff

**Report Generation Batch Processing**:
- Generate reports in background jobs
- Use batch queries for large datasets
- Cache intermediate results

### Horizontal Scaling

**Stateless Services**:
- No session state stored in service
- All state stored in database or cache
- Services can be scaled horizontally

**Load Balancing**:
- Round-robin load balancing
- Session affinity not required
- Can add/remove instances dynamically

**Database Scaling**:
- Read replicas for reporting queries
- Write master for transactional queries
- Connection pooling for efficient resource usage

### Connection Pooling

**HikariCP Configuration**:
- Maximum pool size: 20
- Minimum idle connections: 5
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes
- Max lifetime: 30 minutes

---

## Testing Strategy

### Unit Testing

**Scope**:
- Domain layer (aggregates, value objects, domain services)
- Application layer (application services)
- Utility functions

**Approach**:
- Test business logic in isolation
- Mock external dependencies
- Test happy path and error cases
- Test invariants and validations

**Coverage Target**: 80%+

### Integration Testing

**Scope**:
- Application layer with repositories
- Event publishing and consumption
- Database transactions
- Cache integration

**Approach**:
- Use test database (H2 or PostgreSQL)
- Use test Kafka broker
- Use test Redis cache
- Test end-to-end flows

**Coverage Target**: 60%+

### Property-Based Testing

**Scope**:
- Authentication and authorization logic
- Office hierarchy validation
- Role assignment validation
- Audit log immutability

**Approach**:
- Use property-based testing framework (QuickCheck, Hypothesis, fast-check)
- Generate random inputs
- Test properties that should always hold
- Identify edge cases

**Coverage Target**: Key business logic

### Test Data Management

**Test Data Strategy**:
- Use fixtures for common test data
- Use builders for complex objects
- Use factories for aggregate creation
- Clean up test data after each test

**Test Database**:
- Use H2 for unit tests (fast, in-memory)
- Use PostgreSQL for integration tests (production-like)
- Reset database between tests

---

## Deployment & Operations

### Docker Containerization

**Dockerfile**:
- Base image: openjdk:11-jre-slim
- Copy application JAR
- Expose port 8080
- Set environment variables
- Health check endpoint

**Docker Compose**:
- Administration Service container
- PostgreSQL database container
- Redis cache container
- Kafka broker container

### Kubernetes Deployment

**Deployment Configuration**:
- Replicas: 3 (for high availability)
- Resource requests: CPU 500m, Memory 512Mi
- Resource limits: CPU 1000m, Memory 1024Mi
- Liveness probe: /actuator/health
- Readiness probe: /actuator/health/readiness

**Service Configuration**:
- Type: ClusterIP
- Port: 8080
- Target port: 8080

**ConfigMap**:
- Application properties
- Database configuration
- Kafka configuration
- Redis configuration

**Secrets**:
- Database password
- JWT secret key
- Kafka credentials
- Redis password

### Environment Configuration

**Development**:
- Database: PostgreSQL (local)
- Cache: Redis (local)
- Kafka: Local broker
- Logging: Console + File

**Staging**:
- Database: PostgreSQL (managed service)
- Cache: Redis (managed service)
- Kafka: Managed broker
- Logging: ELK stack

**Production**:
- Database: PostgreSQL (managed service, replicated)
- Cache: Redis (managed service, replicated)
- Kafka: Managed broker (replicated)
- Logging: ELK stack with long-term storage

### Monitoring and Alerting

**Metrics to Monitor**:
- HTTP request latency
- Database query latency
- Cache hit/miss rates
- Event publishing/consumption latency
- Error rates
- Authentication/authorization failures
- Audit log creation rate

**Alerts**:
- High error rate (>1%)
- High latency (>1 second)
- Low cache hit rate (<80%)
- Database connection pool exhaustion
- Kafka consumer lag
- Disk space low
- Memory usage high

### Logging Strategy

**Log Levels**:
- ERROR: System errors, exceptions
- WARN: Warnings, suspicious activities
- INFO: Important events (login, role assignment, settings change)
- DEBUG: Detailed information for debugging

**Log Output**:
- Console: All log levels
- File: All log levels (rotated daily, 30-day retention)
- ELK Stack: Structured logging in JSON format

### Health Checks

**Liveness Probe**:
- Endpoint: `/actuator/health`
- Checks if service is running
- Restarts container if unhealthy

**Readiness Probe**:
- Endpoint: `/actuator/health/readiness`
- Checks if service is ready to receive traffic
- Removes from load balancer if not ready

**Health Check Components**:
- Database connectivity
- Kafka connectivity
- Redis connectivity
- Disk space
- Memory usage

### Graceful Shutdown

**Shutdown Process**:
1. Stop accepting new requests
2. Wait for in-flight requests to complete (timeout: 30 seconds)
3. Close database connections
4. Close Kafka connections
5. Close Redis connections
6. Exit


---

## Architecture Diagrams

### System Overview Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Access Request Processing System                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                    Client Applications                           │   │
│  │  ├─ Web Browser (React/Angular)                                 │   │
│  │  ├─ Mobile App (iOS/Android)                                    │   │
│  │  └─ Desktop Client                                              │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                            ↓ HTTPS                                       │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │            API Gateway / Load Balancer                           │   │
│  │  ├─ Route requests to services                                   │   │
│  │  ├─ SSL/TLS termination                                          │   │
│  │  └─ Rate limiting                                                │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                            ↓                                             │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │            Administration Service (Unit 4)                       │   │
│  │  ├─ Authentication & Authorization                              │   │
│  │  ├─ User Management                                             │   │
│  │  ├─ Role Management                                             │   │
│  │  ├─ Office Management                                           │   │
│  │  ├─ System Settings                                             │   │
│  │  ├─ Audit Logging                                               │   │
│  │  └─ Reporting & Analytics                                       │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                            ↓                                             │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │            Other Services (via REST APIs & Kafka)                │   │
│  │  ├─ Request Management Service (Unit 1)                         │   │
│  │  ├─ Document Management Service (Unit 2)                        │   │
│  │  └─ Notification Service (Unit 3)                               │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                            ↓                                             │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │            Infrastructure Components                             │   │
│  │  ├─ PostgreSQL Database (Shared)                                 │   │
│  │  ├─ Kafka Message Queue                                          │   │
│  │  ├─ Redis Cache                                                  │   │
│  │  ├─ Elasticsearch (Logging)                                      │   │
│  │  ├─ Prometheus (Metrics)                                         │   │
│  │  └─ Grafana (Dashboards)                                         │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Component Interaction Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Administration Service Components                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ REST API Layer (Controllers)                                     │   │
│  │ ├─ AuthenticationController                                      │   │
│  │ ├─ UserManagementController                                      │   │
│  │ ├─ RoleManagementController                                      │   │
│  │ ├─ OfficeManagementController                                    │   │
│  │ ├─ SystemSettingsController                                      │   │
│  │ ├─ AuditLogController                                            │   │
│  │ └─ ReportingController                                           │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                            ↓                                             │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Application Layer (Services)                                     │   │
│  │ ├─ AuthenticationApplicationService                              │   │
│  │ ├─ AuthorizationApplicationService                               │   │
│  │ ├─ UserManagementApplicationService                              │   │
│  │ ├─ RoleManagementApplicationService                              │   │
│  │ ├─ OfficeManagementApplicationService                            │   │
│  │ ├─ SystemSettingsApplicationService                              │   │
│  │ ├─ AuditApplicationService                                       │   │
│  │ └─ ReportingApplicationService                                   │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                            ↓                                             │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Domain Layer (Business Logic)                                    │   │
│  │ ├─ User Aggregate                                                │   │
│  │ ├─ Role Aggregate                                                │   │
│  │ ├─ Office Aggregate                                              │   │
│  │ ├─ SystemSettings Aggregate                                      │   │
│  │ ├─ AuditLog Aggregate                                            │   │
│  │ ├─ Domain Services                                               │   │
│  │ ├─ Policies                                                      │   │
│  │ └─ Factories                                                     │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                            ↓                                             │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Infrastructure Layer                                             │   │
│  │ ├─ UserRepository                                                │   │
│  │ ├─ RoleRepository                                                │   │
│  │ ├─ OfficeRepository                                              │   │
│  │ ├─ SystemSettingsRepository                                      │   │
│  │ ├─ AuditLogRepository                                            │   │
│  │ ├─ EventPublisher (Kafka)                                        │   │
│  │ ├─ EventListener (Kafka)                                         │   │
│  │ ├─ RedisCache                                                    │   │
│  │ └─ Spring Security                                               │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                            ↓                                             │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ External Resources                                               │   │
│  │ ├─ PostgreSQL Database                                           │   │
│  │ ├─ Redis Cache                                                   │   │
│  │ ├─ Kafka Message Queue                                           │   │
│  │ └─ Spring Security                                               │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Authentication Flow Diagram

```
Client                          Administration Service
  │                                      │
  ├─ POST /api/v1/auth/login ──────────→ │
  │  {email, password}                   │
  │                                      ├─ Validate email format
  │                                      ├─ Retrieve user from DB
  │                                      ├─ Compare password hash
  │                                      ├─ Check user status
  │                                      ├─ Generate JWT token
  │                                      ├─ Create session
  │                                      ├─ Publish UserLoginSuccessful event
  │                                      │
  │ ←────────────────────────────────── │
  │  {token, userId, roles, expiresAt}  │
  │                                      │
  ├─ GET /api/v1/users ─────────────────→ │
  │  Authorization: Bearer {token}       │
  │                                      ├─ Extract token
  │                                      ├─ Validate signature
  │                                      ├─ Check expiration
  │                                      ├─ Load user from cache/DB
  │                                      ├─ Check authorization
  │                                      │
  │ ←────────────────────────────────── │
  │  {users: [...]}                      │
  │                                      │
  ├─ POST /api/v1/auth/logout ──────────→ │
  │  {token}                             │
  │                                      ├─ Validate token
  │                                      ├─ Invalidate session
  │                                      ├─ Publish UserLoggedOut event
  │                                      │
  │ ←────────────────────────────────── │
  │  {success: true}                     │
  │                                      │
```

### Authorization Flow Diagram

```
Client Request                  Administration Service
  │                                      │
  ├─ GET /api/v1/users ─────────────────→ │
  │  Authorization: Bearer {token}       │
  │                                      ├─ Extract JWT token
  │                                      ├─ Validate signature
  │                                      ├─ Extract userId, roles
  │                                      ├─ Create Authentication object
  │                                      ├─ Store in SecurityContext
  │                                      │
  │                                      ├─ Check @PreAuthorize("hasRole('ADMIN')")
  │                                      ├─ Load user from cache
  │                                      ├─ Get user roles
  │                                      ├─ Check if user has ADMIN role
  │                                      │
  │                                      ├─ If authorized:
  │                                      │  ├─ Execute controller method
  │                                      │  ├─ Call application service
  │                                      │  ├─ Query repository
  │                                      │  ├─ Return results
  │                                      │
  │                                      ├─ If not authorized:
  │                                      │  ├─ Return 403 Forbidden
  │                                      │
  │ ←────────────────────────────────── │
  │  {users: [...]} or {error: ...}      │
  │                                      │
```

### Event Flow Diagram

```
Request Management Service          Kafka Topic              Administration Service
  │                                    │                              │
  ├─ RequestCreated event ────────────→ request.events ────────────→ │
  │                                    │                              ├─ EventListener
  │                                    │                              ├─ RequestCreatedEventHandler
  │                                    │                              ├─ AuditApplicationService
  │                                    │                              ├─ Create AuditLog entry
  │                                    │                              ├─ Save to repository
  │                                    │                              ├─ Publish UserLoginSuccessful
  │                                    │                              │
  │                                    │                              ├─ EventPublisher
  │                                    │                              │
  │                                    │ ←─────────────────────────── admin.events
  │                                    │  UserLoginSuccessful event   │
  │                                    │                              │
  ├─ RequestSubmitted event ──────────→ request.events ────────────→ │
  │                                    │                              ├─ EventListener
  │                                    │                              ├─ RequestSubmittedEventHandler
  │                                    │                              ├─ AuditApplicationService
  │                                    │                              ├─ Create AuditLog entry
  │                                    │                              │
  │                                    │                              │
Document Management Service           │                              │
  │                                    │                              │
  ├─ DocumentUploaded event ──────────→ document.events ───────────→ │
  │                                    │                              ├─ EventListener
  │                                    │                              ├─ DocumentUploadedEventHandler
  │                                    │                              ├─ AuditApplicationService
  │                                    │                              ├─ Create AuditLog entry
  │                                    │                              │
```

### Database Schema Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Administration Service Database                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌──────────────────┐         ┌──────────────────┐                      │
│  │     users        │         │   user_roles     │                      │
│  ├──────────────────┤         ├──────────────────┤                      │
│  │ user_id (PK)     │◄────────│ user_id (FK)     │                      │
│  │ email (UNIQUE)   │         │ role_id (FK)     │                      │
│  │ hashed_password  │         │ assigned_at      │                      │
│  │ password_salt    │         │ assigned_by      │                      │
│  │ first_name       │         └──────────────────┘                      │
│  │ last_name        │                 │                                 │
│  │ position         │                 │                                 │
│  │ office_id (FK)   │                 │                                 │
│  │ status           │                 │                                 │
│  │ created_at       │                 │                                 │
│  │ updated_at       │                 │                                 │
│  └──────────────────┘                 │                                 │
│         │                             │                                 │
│         │                             ▼                                 │
│         │                    ┌──────────────────┐                      │
│         │                    │     roles        │                      │
│         │                    ├──────────────────┤                      │
│         │                    │ role_id (PK)     │                      │
│         │                    │ role_name        │                      │
│         │                    │ description      │                      │
│         │                    │ created_at       │                      │
│         │                    │ updated_at       │                      │
│         │                    └──────────────────┘                      │
│         │                             │                                 │
│         │                             │                                 │
│         │                    ┌────────┴──────────┐                     │
│         │                    │                   │                     │
│         │                    ▼                   ▼                     │
│         │           ┌──────────────────┐ ┌──────────────────┐         │
│         │           │ role_permissions │ │  permissions     │         │
│         │           ├──────────────────┤ ├──────────────────┤         │
│         │           │ role_id (FK)     │ │ permission_id(PK)│         │
│         │           │ permission_id(FK)│ │ name             │         │
│         │           └──────────────────┘ │ description      │         │
│         │                                 │ resource         │         │
│         │                                 │ action           │         │
│         │                                 └──────────────────┘         │
│         │                                                               │
│         ▼                                                               │
│  ┌──────────────────┐                                                  │
│  │     offices      │                                                  │
│  ├──────────────────┤                                                  │
│  │ office_id (PK)   │                                                  │
│  │ name             │                                                  │
│  │ level            │                                                  │
│  │ parent_office_id │◄─────────────────────────────────────┐           │
│  │ head_of_office_id│                                      │           │
│  │ created_at       │                                      │           │
│  │ updated_at       │                                      │           │
│  └──────────────────┘                                      │           │
│         │                                                  │           │
│         └──────────────────────────────────────────────────┘           │
│                                                                         │
│  ┌──────────────────┐         ┌──────────────────┐                    │
│  │ system_settings  │         │ setting_versions │                    │
│  ├──────────────────┤         ├──────────────────┤                    │
│  │ setting_id (PK)  │◄────────│ version_id (PK)  │                    │
│  │ setting_key      │         │ setting_id (FK)  │                    │
│  │ setting_value    │         │ version          │                    │
│  │ setting_type     │         │ settings_json    │                    │
│  │ version          │         │ created_at       │                    │
│  │ created_at       │         │ created_by       │                    │
│  │ updated_at       │         └──────────────────┘                    │
│  └──────────────────┘                                                  │
│                                                                         │
│  ┌──────────────────┐                                                  │
│  │   audit_logs     │                                                  │
│  ├──────────────────┤                                                  │
│  │ audit_log_id(PK) │                                                  │
│  │ user_id (FK)     │                                                  │
│  │ action           │                                                  │
│  │ resource_type    │                                                  │
│  │ resource_id      │                                                  │
│  │ timestamp        │                                                  │
│  │ old_value        │                                                  │
│  │ new_value        │                                                  │
│  │ reason           │                                                  │
│  │ ip_address       │                                                  │
│  └──────────────────┘                                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Office Hierarchy Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Office Hierarchy                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│                          Organization (Level 0)                          │
│                                  │                                       │
│                  ┌───────────────┼───────────────┐                       │
│                  │               │               │                       │
│            ISG (L1)         Finance (L1)      HR (L1)                    │
│                  │               │               │                       │
│        ┌─────────┼─────────┐     │               │                       │
│        │         │         │     │               │                       │
│    SMD(L2)   RDC(L2)  Ops(L2)    │               │                       │
│        │         │         │     │               │                       │
│        │         │         │     │               │                       │
│   Users Users Users    Users    Users           Users                    │
│                                                                           │
│  Legend:                                                                 │
│  - L0, L1, L2: Hierarchy levels                                          │
│  - Each office has a Head of Office                                      │
│  - Users are assigned to offices                                         │
│  - Office hierarchy is acyclic                                           │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Reporting Flow Diagram

```
User Request                    Administration Service
  │                                      │
  ├─ GET /api/v1/reports/requests ──────→ │
  │  ?fromDate=...&toDate=...            │
  │                                      ├─ ReportingApplicationService
  │                                      ├─ Query request data
  │                                      ├─ Aggregate statistics
  │                                      ├─ Calculate metrics
  │                                      ├─ Format report
  │                                      ├─ Cache result (TTL: 1 hour)
  │                                      │
  │ ←────────────────────────────────── │
  │  {summary, byStatus, byAccessType}   │
  │                                      │
  ├─ GET /api/v1/reports/export ────────→ │
  │  ?reportType=requests&format=pdf     │
  │                                      ├─ Generate report
  │                                      ├─ Format as PDF
  │                                      ├─ Create file
  │                                      │
  │ ←────────────────────────────────── │
  │  [PDF file download]                 │
  │                                      │
  ├─ GET /api/v1/dashboard/metrics ─────→ │
  │                                      ├─ Query current metrics
  │                                      ├─ Check cache
  │                                      ├─ If not cached:
  │                                      │  ├─ Query database
  │                                      │  ├─ Calculate metrics
  │                                      │  ├─ Cache result (TTL: 5 min)
  │                                      │
  │ ←────────────────────────────────── │
  │  {totalRequests, pending, approved}  │
  │                                      │
```

---

## Summary

The Administration Service is a comprehensive, event-driven service that provides cross-cutting administrative capabilities for the entire Access Request Processing System. It manages user authentication and authorization, role-based access control, office hierarchy management, system configuration, audit logging, and reporting and analytics.

The service is designed with:
- **Domain-Driven Design**: Rich domain model with aggregates, entities, and value objects
- **Event-Driven Architecture**: Asynchronous communication via Kafka
- **Layered Architecture**: Clear separation of concerns
- **Security First**: Authentication, authorization, encryption, audit logging
- **Performance**: Caching, query optimization, batch processing
- **Scalability**: Stateless services, horizontal scaling support
- **Reliability**: Transaction management, error handling, idempotent processing

The logical design provides a complete blueprint for implementing the Administration Service with all necessary components, interfaces, and interactions clearly defined.

---

**Document Version**: 1.0  
**Last Updated**: January 8, 2025  
**Status**: Design Phase Complete  
**Next Step**: Implementation Phase

