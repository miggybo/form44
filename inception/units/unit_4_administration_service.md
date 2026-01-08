# Unit 4: Administration Service

## Overview
The Administration Service manages user authentication, authorization, role management, system configuration, reporting, and audit logging. This service provides administrative capabilities and analytics for the entire system.

## Responsibilities
- User authentication and session management
- Role-based access control
- User and role management
- System configuration
- Generate reports and dashboards
- Maintain audit logs
- Provide analytics and metrics

## Database Ownership
- `users` table
- `roles` table
- `user_roles` table
- `offices` table
- `system_settings` table
- `audit_logs` table

---

## User Stories

### US-036: Login to System
**As an** Employee  
**I want to** login to the system using my credentials  
**So that** I can access the application securely

**Acceptance Criteria:**
- Provide login page with username/password fields
- Validate credentials against organization directory
- Display appropriate error messages for invalid credentials
- Redirect to appropriate dashboard based on user role
- Implement session timeout for security
- Support password reset functionality

---

### US-037: View Role-Based Dashboard
**As a** User  
**I want to** see a dashboard appropriate to my role  
**So that** I can quickly access relevant functions

**Acceptance Criteria:**
- Employee sees: Create Request, My Requests
- Head of Office sees: Pending Approvals, My Team's Requests
- SMD/RDC Reviewer sees: Pending Reviews, All Requests
- SMD/RDC Head sees: Pending Final Approvals, Dashboard, Reports
- System/DB Admin sees: Pending Implementations, Completed Requests
- Dashboard shows counts of pending items

---

### US-038: Manage User Roles
**As a** System Administrator  
**I want to** assign and manage user roles  
**So that** users have appropriate access permissions

**Acceptance Criteria:**
- Provide interface to view all users
- Assign roles: Employee, Head of Office, SMD/RDC Reviewer, SMD/RDC Head, System Admin, DB Admin
- Users can have multiple roles
- Changes take effect immediately
- System maintains audit log of role changes
- Validate role assignments against office hierarchy

**Events Published:**
- `UserRoleAssigned`
- `UserRoleRevoked`

---

### US-040: View Audit Logs
**As a** System Administrator  
**I want to** view system audit logs  
**So that** I can track all system activities for security and compliance

**Acceptance Criteria:**
- Display all user actions with timestamps
- Show user, action type, and affected request
- Filter by date range, user, or action type
- Export audit logs to CSV
- Logs are immutable and tamper-proof
- Retain logs for compliance period

---

### US-041: Configure System Settings
**As a** System Administrator  
**I want to** configure system settings  
**So that** the system operates according to organizational policies

**Acceptance Criteria:**
- Configure file upload size limits
- Set session timeout duration
- Configure email server settings
- Set request auto-escalation rules
- Define SLA timelines for each approval stage
- Test configuration changes before applying

**Events Published:**
- `SystemSettingsUpdated`

---

### US-034: Generate Request Reports
**As an** SMD/RDC Head  
**I want to** generate reports on access requests  
**So that** I can analyze trends and processing times

**Acceptance Criteria:**
- Generate reports by date range, office, access type
- Show metrics: total requests, approval rate, average processing time
- Display pending requests by stage
- Export reports to PDF or Excel
- Schedule automated report generation

---

### US-035: View Dashboard
**As an** SMD/RDC Head  
**I want to** view a dashboard with key metrics  
**So that** I can monitor the overall request processing system

**Acceptance Criteria:**
- Display total requests by status
- Show pending requests requiring action
- Display average processing time by stage
- Show requests by access type
- Display trend charts (requests over time)
- Provide drill-down capability to view details

---

## Events Published

| Event Name | Trigger | Payload |
|------------|---------|---------|
| `UserRoleAssigned` | Role assigned to user | userId, roleId, assignedBy |
| `UserRoleRevoked` | Role removed from user | userId, roleId, revokedBy |
| `SystemSettingsUpdated` | System settings changed | settingKey, oldValue, newValue, updatedBy |

---

## Events Consumed

| Event Name | Source Service | Purpose |
|------------|----------------|---------|
| `RequestCreated` | Request Management | Log audit entry |
| `RequestSubmitted` | Request Management | Log audit entry, update metrics |
| `RequestApprovedByHeadOfOffice` | Request Management | Log audit entry, update metrics |
| `RequestEndorsed` | Request Management | Log audit entry, update metrics |
| `RequestFinallyApproved` | Request Management | Log audit entry, update metrics |
| `RequestDeclined` | Request Management | Log audit entry, update metrics |
| `RequestReturned` | Request Management | Log audit entry, update metrics |
| `RequestImplemented` | Request Management | Log audit entry, update metrics |
| `DocumentUploaded` | Document Management | Log audit entry |
| `DocumentDownloaded` | Document Management | Log audit entry |
| `AccessTypeAdded` | Request Management | Log audit entry |

---

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Refresh session token
- `POST /api/auth/reset-password` - Request password reset

### Users & Roles
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user details
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `GET /api/users/{id}/roles` - Get user roles
- `POST /api/users/{id}/roles` - Assign role to user
- `DELETE /api/users/{id}/roles/{roleId}` - Revoke role from user
- `GET /api/roles` - List all roles

### System Settings
- `GET /api/settings` - Get all settings
- `GET /api/settings/{key}` - Get specific setting
- `PUT /api/settings/{key}` - Update setting
- `POST /api/settings/test` - Test configuration

### Audit Logs
- `GET /api/audit-logs` - List audit logs (with filters)
- `GET /api/audit-logs/{id}` - Get audit log details
- `GET /api/audit-logs/export` - Export audit logs to CSV

### Reports & Analytics
- `GET /api/reports/requests` - Generate request report
- `GET /api/reports/performance` - Generate performance report
- `GET /api/reports/export` - Export report
- `GET /api/dashboard/metrics` - Get dashboard metrics
- `GET /api/dashboard/trends` - Get trend data

### Offices
- `GET /api/offices` - List all offices
- `GET /api/offices/{id}` - Get office details
- `GET /api/offices/{id}/hierarchy` - Get office hierarchy

---

**Total User Stories**: 7
