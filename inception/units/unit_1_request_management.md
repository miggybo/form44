# Unit 1: Request Management Service

## Overview
The Request Management Service is the core service responsible for managing the complete lifecycle of access requests, including creation, submission, approval workflow orchestration, and implementation tracking. This service owns the request data and coordinates the multi-level approval process.

## Responsibilities
- Create and manage access requests
- Orchestrate approval workflow (Head of Office → Reviewer → Head → Administrator)
- Track request status and history
- Route requests to appropriate approvers/administrators
- Manage access type configuration
- Provide request search and tracking capabilities

## Database Ownership
- `access_requests` table
- `request_history` table
- `access_types` table
- `workflow_states` table

---

## User Stories

### US-001: Create Access Request
**As an** Employee  
**I want to** create an access request by filling out a form  
**So that** I can request access to the systems I need to perform my job

**Acceptance Criteria:**
- Form includes requestor information (name, position, office)
- Form allows selection of access type (OS, Web Application, Database)
- Form allows specification of system/application name
- Form includes justification field
- Form allows attachment of supporting documents (SAM, Justification)
- System validates all required fields before submission
- System generates unique request ID upon submission
- Requestor receives confirmation of submission

**Events Published:**
- `RequestCreated`

---

### US-002: Submit Request for Initial Approval
**As an** Employee  
**I want to** submit my completed access request to my Head of Office  
**So that** my request can be reviewed and initially approved

**Acceptance Criteria:**
- System routes request to the employee's Head of Office
- Head of Office receives notification of pending request
- Request status changes to "Pending Initial Approval"
- Employee can view request status
- System records submission timestamp

**Events Published:**
- `RequestSubmitted`

---

### US-003: View My Access Requests
**As an** Employee  
**I want to** view all my access requests and their current status  
**So that** I can track the progress of my requests

**Acceptance Criteria:**
- Display list of all requests created by the employee
- Show request ID, access type, system name, status, and submission date
- Allow filtering by status (Draft, Pending, Approved, Declined, Implemented)
- Allow sorting by date
- Provide option to view request details

---

### US-005: Review Access Request
**As a** Head of Office  
**I want to** review access requests submitted by my team members  
**So that** I can verify the legitimacy and necessity of the request

**Acceptance Criteria:**
- View list of pending requests from team members
- View complete request details including justification
- View attached supporting documents
- See requestor's profile information
- Access request history of the requestor

---

### US-006: Approve Access Request
**As a** Head of Office  
**I want to** approve access requests that are justified and necessary  
**So that** the request can proceed to the next approval stage

**Acceptance Criteria:**
- Provide "Approve" button on request detail page
- Require confirmation before approval
- Add optional comments/remarks
- System routes approved request to SMD/RDC Reviewer
- System sends notification to requestor and reviewer
- Request status changes to "Pending Review"
- System records approval timestamp and approver details

**Events Published:**
- `RequestApprovedByHeadOfOffice`

---

### US-007: Decline Access Request
**As a** Head of Office  
**I want to** decline access requests that are not justified  
**So that** inappropriate access requests are prevented

**Acceptance Criteria:**
- Provide "Decline" button on request detail page
- Require mandatory reason for declining
- System sends notification to requestor with decline reason
- Request status changes to "Declined"
- System records decline timestamp and decliner details
- Declined requests cannot be resubmitted (must create new request)

**Events Published:**
- `RequestDeclined`

---

### US-008: View Pending Approvals
**As a** Head of Office  
**I want to** view all pending access requests requiring my approval  
**So that** I can prioritize and process them efficiently

**Acceptance Criteria:**
- Display list of all pending requests
- Show request priority/urgency
- Show how long request has been pending
- Allow filtering by access type, requestor, or date
- Provide bulk view option

---

### US-009: Review Request and Attachments
**As an** SMD/RDC Reviewer  
**I want to** review access requests including all attached documents  
**So that** I can verify compliance with security policies

**Acceptance Criteria:**
- View complete request details
- Download and view attached documents (SAM, Justification)
- View approval history and comments
- Check requestor's existing access rights
- View office hierarchy information

---

### US-010: Endorse Request for Approval
**As an** SMD/RDC Reviewer  
**I want to** endorse compliant requests to the SMD/RDC Head  
**So that** valid requests can proceed to final approval

**Acceptance Criteria:**
- Provide "Endorse" button on request detail page
- Add endorsement comments/recommendations
- System routes endorsed request to SMD/RDC Head
- System sends notification to SMD/RDC Head
- Request status changes to "Pending Final Approval"
- System records endorsement timestamp and reviewer details

**Events Published:**
- `RequestEndorsed`

---

### US-011: Decline Request Due to Missing Documents
**As an** SMD/RDC Reviewer  
**I want to** decline requests that lack required supporting documents  
**So that** only properly documented requests are processed

**Acceptance Criteria:**
- Provide "Decline" button with reason selection
- Specify which documents are missing (SAM, Justification)
- System sends notification to requestor and Head of Office
- Request status changes to "Declined - Missing Documents"
- System records decline timestamp and reason
- Notification includes list of required documents

**Events Published:**
- `RequestDeclined`

---

### US-012: View Pending Reviews
**As an** SMD/RDC Reviewer  
**I want to** view all requests pending my review  
**So that** I can manage my workload effectively

**Acceptance Criteria:**
- Display list of all requests pending review
- Show request age and priority
- Filter by access type, office, or date
- Sort by submission date or priority
- Show document attachment status

---

### US-013: Review Endorsed Request
**As an** SMD/RDC Head  
**I want to** review requests endorsed by reviewers  
**So that** I can make final approval decisions

**Acceptance Criteria:**
- View complete request details and history
- View reviewer's endorsement comments
- View all attached documents
- See complete approval chain
- Access requestor and office information

---

### US-014: Approve Request for Implementation
**As an** SMD/RDC Head  
**I want to** approve requests that meet all requirements  
**So that** they can be forwarded to administrators for implementation

**Acceptance Criteria:**
- Provide "Approve" button on request detail page
- Add final approval comments
- System routes to appropriate administrator (System Admin or DB Admin based on access type)
- System sends notification to assigned administrator
- Request status changes to "Approved - Pending Implementation"
- System records approval timestamp and approver details

**Events Published:**
- `RequestFinallyApproved`

---

### US-015: Return Request to Reviewer
**As an** SMD/RDC Head  
**I want to** return requests that lack proper documentation  
**So that** reviewers can address documentation issues

**Acceptance Criteria:**
- Provide "Return" button on request detail page
- Specify reason for return
- System sends notification to reviewer
- Request status changes to "Returned to Reviewer"
- Reviewer can re-review and re-endorse after issues are addressed
- System maintains history of returns

**Events Published:**
- `RequestReturned`

---

### US-016: View Pending Final Approvals
**As an** SMD/RDC Head  
**I want to** view all requests pending my final approval  
**So that** I can process them in a timely manner

**Acceptance Criteria:**
- Display list of all endorsed requests
- Show request details and reviewer recommendations
- Filter by access type, office, or reviewer
- Sort by endorsement date or priority
- Show complete approval history

---

### US-017: View Assigned Requests
**As a** System Administrator  
**I want to** view access requests assigned to me for implementation  
**So that** I can implement OS and web application access

**Acceptance Criteria:**
- Display list of approved requests for OS and Web Application access
- Show request details and approved access level
- Filter by status (Pending Implementation, In Progress, Implemented)
- Sort by approval date
- View complete request history

---

### US-018: View Assigned Database Requests
**As a** Database Administrator  
**I want to** view database access requests assigned to me  
**So that** I can implement database access

**Acceptance Criteria:**
- Display list of approved requests for Database access
- Show database name, access level, and user details
- Filter by status and database system
- Sort by approval date
- View complete request history

---

### US-019: Mark Request as Implemented
**As a** System Administrator  
**I want to** mark a request as implemented after completing the access setup  
**So that** the requestor is notified and the request is closed

**Acceptance Criteria:**
- Provide "Mark as Implemented" button
- Add implementation notes/details
- System sends email notification to requestor
- Request status changes to "Implemented"
- System records implementation timestamp and implementer details
- Request is moved to completed requests

**Events Published:**
- `RequestImplemented`

---

### US-020: Mark Database Request as Implemented
**As a** Database Administrator  
**I want to** mark a database request as implemented after granting access  
**So that** the requestor is notified and the request is closed

**Acceptance Criteria:**
- Provide "Mark as Implemented" button
- Add implementation notes (credentials, connection details)
- System sends email notification to requestor
- Request status changes to "Implemented"
- System records implementation timestamp and implementer details
- Request is moved to completed requests

**Events Published:**
- `RequestImplemented`

---

### US-029: Select Access Type
**As an** Employee  
**I want to** select the type of access I need (OS, Web Application, Database)  
**So that** my request is routed to the appropriate administrator

**Acceptance Criteria:**
- Provide dropdown/radio buttons for access type selection
- Display description for each access type
- Show examples of systems for each type
- Access type determines routing to System Admin or DB Admin
- Access type is prominently displayed throughout approval workflow

---

### US-030: Add New Access Type
**As a** System Administrator  
**I want to** add new access types to the system  
**So that** the system can accommodate future access requirements

**Acceptance Criteria:**
- Provide admin interface to add new access types
- Specify access type name and description
- Define routing rules (which administrator handles this type)
- New access types appear in request form dropdown
- Existing requests are not affected
- System maintains audit log of access type changes

**Events Published:**
- `AccessTypeAdded`

---

### US-031: Configure Access Type Routing
**As a** System Administrator  
**I want to** configure which administrator role handles each access type  
**So that** requests are routed correctly

**Acceptance Criteria:**
- Provide interface to map access types to administrator roles
- Support multiple administrators per access type
- Define default administrator for each type
- Changes take effect for new requests only
- System validates routing configuration

**Events Published:**
- `AccessTypeRoutingConfigured`

---

### US-032: View Request History
**As an** Employee  
**I want to** view the complete history of my access request  
**So that** I can see all actions taken and comments made

**Acceptance Criteria:**
- Display chronological timeline of all actions
- Show who performed each action and when
- Display all comments and remarks
- Show status changes with timestamps
- Include document upload/download history

---

### US-033: Search Requests
**As an** SMD/RDC Reviewer  
**I want to** search for requests by various criteria  
**So that** I can quickly find specific requests

**Acceptance Criteria:**
- Search by request ID, requestor name, office, access type
- Search by date range
- Search by status
- Display search results with key information
- Provide option to export search results

---

## Events Published

| Event Name | Trigger | Payload |
|------------|---------|---------|
| `RequestCreated` | Request is created | requestId, requestorId, accessType, systemName |
| `RequestSubmitted` | Request is submitted for approval | requestId, requestorId, headOfOfficeId |
| `RequestApprovedByHeadOfOffice` | Head of Office approves | requestId, approverId, comments, reviewerId |
| `RequestEndorsed` | Reviewer endorses | requestId, reviewerId, comments, smdHeadId |
| `RequestFinallyApproved` | SMD/RDC Head approves | requestId, approverId, comments, administratorId, accessType |
| `RequestDeclined` | Request is declined | requestId, declinerId, reason, requestorId |
| `RequestReturned` | Request returned to reviewer | requestId, returnerId, reason, reviewerId |
| `RequestImplemented` | Administrator completes implementation | requestId, implementerId, notes, requestorId |
| `AccessTypeAdded` | New access type created | accessTypeId, name, description |
| `AccessTypeRoutingConfigured` | Routing rules updated | accessTypeId, administratorRoles |

---

## Events Consumed

| Event Name | Source Service | Purpose |
|------------|----------------|---------|
| `DocumentUploaded` | Document Management | Update request with document metadata |
| `DocumentDeleted` | Document Management | Update request document status |

---

## API Endpoints

### Requests
- `POST /api/requests` - Create new request
- `GET /api/requests/{id}` - Get request details
- `GET /api/requests` - List requests (with filters)
- `PUT /api/requests/{id}/submit` - Submit request
- `PUT /api/requests/{id}/approve` - Approve request
- `PUT /api/requests/{id}/decline` - Decline request
- `PUT /api/requests/{id}/endorse` - Endorse request
- `PUT /api/requests/{id}/return` - Return request
- `PUT /api/requests/{id}/implement` - Mark as implemented
- `GET /api/requests/{id}/history` - Get request history

### Access Types
- `GET /api/access-types` - List all access types
- `POST /api/access-types` - Create new access type
- `PUT /api/access-types/{id}` - Update access type
- `PUT /api/access-types/{id}/routing` - Configure routing

### Search
- `GET /api/requests/search` - Search requests

---

**Total User Stories**: 26
