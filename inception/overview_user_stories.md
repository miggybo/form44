# Access Request Processing System - User Stories

## Overview
This document contains user stories for the Access Request Processing System, a web application that enables employees to request access to various e-services (OS, Web Applications, Databases) with a multi-level approval workflow.

---

## Epic 1: Access Request Management

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

### US-004: Receive Implementation Notification
**As an** Employee  
**I want to** receive an email notification when my access request is implemented  
**So that** I know when I can start using the requested access

**Acceptance Criteria:**
- Email sent when request status changes to "Implemented"
- Email includes request details and access information
- Email includes contact information for support

---

## Epic 2: Initial Approval Process

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

## Epic 3: Review and Endorsement Process

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

## Epic 4: Final Approval Process

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

## Epic 5: Implementation Process

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

---

## Epic 6: Document Management

### US-021: Upload Supporting Documents
**As an** Employee  
**I want to** upload supporting documents (SAM, Justification) to my access request  
**So that** my request includes all required documentation

**Acceptance Criteria:**
- Provide file upload interface on request form
- Support common file formats (PDF, DOC, DOCX, JPG, PNG)
- Allow multiple file uploads
- Display uploaded file names and sizes
- Provide option to remove uploaded files before submission
- Validate file size limits (max 10MB per file)
- Show upload progress indicator

---

### US-022: Download Supporting Documents
**As an** SMD/RDC Reviewer  
**I want to** download and view supporting documents attached to requests  
**So that** I can verify compliance with security policies

**Acceptance Criteria:**
- Display list of attached documents with file names
- Provide download button for each document
- Support preview for PDF files
- Track document download history
- Ensure secure document access (only authorized users)

---

### US-023: View Document Attachment Status
**As an** SMD/RDC Reviewer  
**I want to** quickly see which requests have all required documents attached  
**So that** I can prioritize complete requests

**Acceptance Criteria:**
- Display document status indicator on request list
- Show which documents are attached (SAM, Justification)
- Highlight requests with missing documents
- Filter requests by document completeness
- Show document upload date

---

## Epic 7: Notification System

### US-024: Receive Request Assignment Notification
**As a** Head of Office  
**I want to** receive email notification when a request is submitted by my team member  
**So that** I can review it promptly

**Acceptance Criteria:**
- Email sent immediately upon request submission
- Email includes request ID, requestor name, and access type
- Email includes link to review the request
- Email includes request summary

---

### US-025: Receive Review Assignment Notification
**As an** SMD/RDC Reviewer  
**I want to** receive email notification when a request is assigned to me  
**So that** I can review it in a timely manner

**Acceptance Criteria:**
- Email sent when request is initially approved
- Email includes request details and approver comments
- Email includes link to review the request
- Email indicates urgency/priority

---

### US-026: Receive Final Approval Notification
**As an** SMD/RDC Head  
**I want to** receive email notification when a request is endorsed  
**So that** I can provide final approval

**Acceptance Criteria:**
- Email sent when request is endorsed by reviewer
- Email includes reviewer's endorsement comments
- Email includes link to approve the request
- Email includes complete request summary

---

### US-027: Receive Implementation Assignment Notification
**As a** System Administrator  
**I want to** receive email notification when a request is approved for implementation  
**So that** I can implement the access

**Acceptance Criteria:**
- Email sent when request is finally approved
- Email includes all request details and access specifications
- Email includes link to view request details
- Email includes requestor contact information

---

### US-028: Receive Status Update Notifications
**As an** Employee  
**I want to** receive email notifications when my request status changes  
**So that** I stay informed about my request progress

**Acceptance Criteria:**
- Email sent for each status change (Approved, Declined, Returned, Implemented)
- Email includes current status and next steps
- Email includes comments from approvers/reviewers
- Email includes estimated timeline for next action

---

## Epic 8: Access Type Management

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

---

## Epic 9: Request Tracking and Reporting

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

## Epic 10: User Authentication and Authorization

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

---

## Epic 11: System Administration

### US-039: Configure Email Templates
**As a** System Administrator  
**I want to** configure email notification templates  
**So that** notifications are clear and professional

**Acceptance Criteria:**
- Provide interface to edit email templates
- Support variables (requestor name, request ID, etc.)
- Preview email before saving
- Maintain separate templates for each notification type
- Support HTML formatting
- Revert to default template option

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

---

## Workflow Summary

```
1. Employee creates request → Submits to Head of Office
2. Head of Office reviews → Approves/Declines
3. If Approved → Routes to SMD/RDC Reviewer
4. SMD/RDC Reviewer reviews documents → Endorses/Declines
5. If Endorsed → Routes to SMD/RDC Head
6. SMD/RDC Head reviews → Approves/Returns to Reviewer
7. If Approved → Routes to System Admin or DB Admin (based on access type)
8. Administrator implements access → Marks as Implemented
9. System sends email notification to Requestor
```

---

## Glossary

- **SAM**: Security Access Matrix - Document defining authorized access levels
- **SMD**: Security Management Division - Part of ISG responsible for security
- **RDC**: Revenue Data Center - Part of ISG responsible for data center operations
- **ISG**: Information Systems Group - Division handling IT operations
- **Access Type**: Category of system access (OS, Web Application, Database, or custom)
- **Endorsement**: Reviewer's recommendation for approval
- **Implementation**: Actual granting of access by administrator

---

**Document Version**: 1.0  
**Last Updated**: 2025  
**Total User Stories**: 41
