# Unit 2: Document Management Service

## Overview
The Document Management Service handles all file operations including uploading, storing, retrieving, and managing supporting documents (SAM, Justification) attached to access requests. This service ensures secure document storage and access control.

## Responsibilities
- Upload and store documents
- Download and retrieve documents
- Manage document metadata
- Enforce file size and format restrictions
- Track document access history
- Provide document status information

## Database Ownership
- `documents` table
- `document_access_log` table

---

## User Stories

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

**Events Published:**
- `DocumentUploaded`

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

**Events Published:**
- `DocumentDownloaded`

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

## Events Published

| Event Name | Trigger | Payload |
|------------|---------|---------|
| `DocumentUploaded` | Document successfully uploaded | documentId, requestId, fileName, fileSize, fileType, uploadedBy |
| `DocumentDownloaded` | Document downloaded | documentId, requestId, downloadedBy, timestamp |
| `DocumentDeleted` | Document removed | documentId, requestId, deletedBy |

---

## Events Consumed

| Event Name | Source Service | Purpose |
|------------|----------------|---------|
| `RequestCreated` | Request Management | Initialize document tracking for request |
| `RequestDeclined` | Request Management | Archive documents for declined request |
| `RequestImplemented` | Request Management | Archive documents for completed request |

---

## API Endpoints

### Documents
- `POST /api/documents/upload` - Upload document
- `GET /api/documents/{id}` - Get document metadata
- `GET /api/documents/{id}/download` - Download document
- `DELETE /api/documents/{id}` - Delete document
- `GET /api/documents/request/{requestId}` - List documents for request
- `GET /api/documents/{id}/preview` - Preview document (PDF only)

### Document Status
- `GET /api/documents/request/{requestId}/status` - Get document completeness status

---

**Total User Stories**: 3
