import { RequestStatus, ApprovalType, UserRole } from '@/types';

export const REQUEST_STATUSES: RequestStatus[] = [
  'Draft',
  'PendingInitialApproval',
  'PendingReview',
  'PendingFinalApproval',
  'ReturnedToReviewer',
  'Approved',
  'Declined',
  'Implemented',
];

export const APPROVAL_TYPES: ApprovalType[] = ['HeadOfOffice', 'Reviewer', 'Head'];

export const USER_ROLES: UserRole[] = [
  'Employee',
  'HeadOfOffice',
  'Reviewer',
  'Head',
  'SystemAdmin',
  'DBAdmin',
];

export const ACCESS_TYPES = [
  { id: 'OS', label: 'Operating System' },
  { id: 'WebApp', label: 'Web Application' },
  { id: 'Database', label: 'Database' },
];

export const PAGINATION_SIZES = [10, 20, 50, 100];
export const DEFAULT_PAGE_SIZE = 20;

export const SLA_THRESHOLDS = {
  HeadOfOffice: 3, // days
  Reviewer: 5, // days
  Head: 2, // days
};

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  DASHBOARD: '/dashboard',
  REQUESTS: '/requests',
  REQUEST_DETAILS: '/requests/:id',
  CREATE_REQUEST: '/requests/create',
  APPROVALS: '/approvals',
  ACCESS_TYPES: '/access-types',
  SEARCH: '/search',
  SETTINGS: '/settings',
  PROFILE: '/profile',
};

export const API_ENDPOINTS = {
  REQUESTS: '/requests',
  ACCESS_TYPES: '/access-types',
  AUTH: '/auth',
  SEARCH: '/requests/search',
};
