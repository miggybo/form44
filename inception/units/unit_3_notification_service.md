# Unit 3: Notification Service

## Overview
The Notification Service handles all email notifications throughout the access request lifecycle. It listens to events from other services and sends appropriate email notifications to relevant stakeholders.

## Responsibilities
- Send email notifications for all request status changes
- Manage email templates
- Track notification delivery status
- Handle notification failures and retries
- Support email template customization

## Database Ownership
- `notifications` table
- `email_templates` table
- `notification_log` table

---

## User Stories

### US-004: Receive Implementation Notification
**As an** Employee  
**I want to** receive an email notification when my access request is implemented  
**So that** I know when I can start using the requested access

**Acceptance Criteria:**
- Email sent when request status changes to "Implemented"
- Email includes request details and access information
- Email includes contact information for support

---

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

## Events Consumed

| Event Name | Source Service | Action |
|------------|----------------|--------|
| `RequestCreated` | Request Management | Send confirmation to requestor |
| `RequestSubmitted` | Request Management | Send notification to Head of Office |
| `RequestApprovedByHeadOfOffice` | Request Management | Send notification to requestor and reviewer |
| `RequestEndorsed` | Request Management | Send notification to SMD/RDC Head |
| `RequestFinallyApproved` | Request Management | Send notification to administrator |
| `RequestDeclined` | Request Management | Send notification to requestor |
| `RequestReturned` | Request Management | Send notification to reviewer |
| `RequestImplemented` | Request Management | Send notification to requestor |

---

## API Endpoints

### Notifications
- `GET /api/notifications` - List all notifications
- `GET /api/notifications/{id}` - Get notification details
- `POST /api/notifications/send` - Send manual notification
- `GET /api/notifications/user/{userId}` - Get user's notifications

### Email Templates
- `GET /api/email-templates` - List all templates
- `GET /api/email-templates/{type}` - Get template by type
- `PUT /api/email-templates/{type}` - Update template
- `POST /api/email-templates/{type}/preview` - Preview template
- `POST /api/email-templates/{type}/reset` - Reset to default

---

**Total User Stories**: 7
