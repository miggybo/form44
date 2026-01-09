// Request Types
export type RequestStatus = 
  | 'Draft'
  | 'PendingInitialApproval'
  | 'PendingReview'
  | 'PendingFinalApproval'
  | 'ReturnedToReviewer'
  | 'Approved'
  | 'Declined'
  | 'Implemented';

export type ApprovalType = 'HeadOfOffice' | 'Reviewer' | 'Head';
export type ApprovalStatus = 'Approved' | 'Declined' | 'Pending';

export interface Request {
  requestId: string;
  requestorId: string;
  requestorName: string;
  accessType: string;
  systemName: string;
  justification: string;
  status: RequestStatus;
  createdAt: string;
  submittedAt?: string;
  approvedAt?: string;
  implementedAt?: string;
  approvals: Approval[];
  history: HistoryEntry[];
  documents: Document[];
}

export interface Approval {
  approvalId: string;
  approverId: string;
  approverName: string;
  approvalType: ApprovalType;
  status: ApprovalStatus;
  comments?: string;
  createdAt: string;
}

export interface HistoryEntry {
  historyId: string;
  timestamp: string;
  action: string;
  actor: string;
  previousStatus?: RequestStatus;
  newStatus?: RequestStatus;
  comments?: string;
}

export interface Document {
  documentId: string;
  fileName: string;
  uploadedAt: string;
  uploadedBy: string;
}

export interface AccessType {
  accessTypeId: string;
  name: string;
  description: string;
  routingRules: RoutingRule[];
  createdAt: string;
  updatedAt: string;
}

export interface RoutingRule {
  routingId: string;
  administratorRole: string;
  isDefault: boolean;
}

// User Types
export type UserRole = 'Employee' | 'HeadOfOffice' | 'Reviewer' | 'Head' | 'SystemAdmin' | 'DBAdmin';

export interface User {
  userId: string;
  name: string;
  email: string;
  role: UserRole;
  officeId?: string;
  officeName?: string;
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  timestamp: string;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string>;
  timestamp: string;
  path: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  totalCount: number;
  page: number;
  size: number;
  totalPages: number;
}

// Form Types
export interface CreateRequestForm {
  accessType: string;
  systemName: string;
  justification: string;
  documents?: string[];
}

export interface ApproveRequestForm {
  comments?: string;
}

export interface DeclineRequestForm {
  reason: string;
}

export interface EndorseRequestForm {
  comments?: string;
}

export interface ReturnRequestForm {
  reason: string;
}

export interface ImplementRequestForm {
  notes?: string;
}
