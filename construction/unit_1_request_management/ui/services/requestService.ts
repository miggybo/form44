import { apiClient } from './api';
import {
  Request,
  CreateRequestForm,
  ApproveRequestForm,
  DeclineRequestForm,
  EndorseRequestForm,
  ReturnRequestForm,
  ImplementRequestForm,
  PaginatedResponse,
} from '@/types';

export const requestService = {
  // Create a new request
  createRequest: async (data: CreateRequestForm): Promise<Request> => {
    const response = await apiClient.post<Request>('/requests', data);
    return response.data;
  },

  // Get request details
  getRequest: async (requestId: string): Promise<Request> => {
    const response = await apiClient.get<Request>(`/requests/${requestId}`);
    return response.data;
  },

  // List requests with pagination and filters
  listRequests: async (params?: {
    status?: string;
    requestorId?: string;
    accessType?: string;
    fromDate?: string;
    toDate?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortOrder?: string;
  }): Promise<PaginatedResponse<Request>> => {
    const response = await apiClient.get<PaginatedResponse<Request>>('/requests', { params });
    return response.data;
  },

  // Submit request for approval
  submitRequest: async (requestId: string, headOfOfficeId: string): Promise<Request> => {
    const response = await apiClient.put<Request>(`/requests/${requestId}/submit`, {
      headOfOfficeId,
    });
    return response.data;
  },

  // Approve request
  approveRequest: async (
    requestId: string,
    approverId: string,
    approvalType: string,
    data: ApproveRequestForm
  ): Promise<Request> => {
    const response = await apiClient.put<Request>(`/requests/${requestId}/approve`, {
      approverId,
      approvalType,
      comments: data.comments,
    });
    return response.data;
  },

  // Decline request
  declineRequest: async (
    requestId: string,
    declinerId: string,
    data: DeclineRequestForm
  ): Promise<Request> => {
    const response = await apiClient.put<Request>(`/requests/${requestId}/decline`, {
      declinerId,
      reason: data.reason,
    });
    return response.data;
  },

  // Endorse request
  endorseRequest: async (
    requestId: string,
    reviewerId: string,
    data: EndorseRequestForm
  ): Promise<Request> => {
    const response = await apiClient.put<Request>(`/requests/${requestId}/endorse`, {
      reviewerId,
      comments: data.comments,
    });
    return response.data;
  },

  // Return request
  returnRequest: async (
    requestId: string,
    returnerId: string,
    data: ReturnRequestForm
  ): Promise<Request> => {
    const response = await apiClient.put<Request>(`/requests/${requestId}/return`, {
      returnerId,
      reason: data.reason,
    });
    return response.data;
  },

  // Mark as implemented
  implementRequest: async (
    requestId: string,
    implementerId: string,
    data: ImplementRequestForm
  ): Promise<Request> => {
    const response = await apiClient.put<Request>(`/requests/${requestId}/implement`, {
      implementerId,
      notes: data.notes,
    });
    return response.data;
  },

  // Get request history
  getRequestHistory: async (requestId: string) => {
    const response = await apiClient.get(`/requests/${requestId}/history`);
    return response.data;
  },

  // Search requests
  searchRequests: async (params?: {
    q?: string;
    accessType?: string;
    status?: string;
    officeId?: string;
    fromDate?: string;
    toDate?: string;
    page?: number;
    size?: number;
  }) => {
    const response = await apiClient.get('/requests/search', { params });
    return response.data;
  },
};
