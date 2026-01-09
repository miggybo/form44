import { format, parseISO } from 'date-fns';
import { RequestStatus } from '@/types';

export const formatDate = (dateString: string): string => {
  try {
    return format(parseISO(dateString), 'MMM dd, yyyy');
  } catch {
    return dateString;
  }
};

export const formatDateTime = (dateString: string): string => {
  try {
    return format(parseISO(dateString), 'MMM dd, yyyy HH:mm');
  } catch {
    return dateString;
  }
};

export const formatTime = (dateString: string): string => {
  try {
    return format(parseISO(dateString), 'HH:mm');
  } catch {
    return dateString;
  }
};

export const getStatusColor = (status: RequestStatus): string => {
  const colors: Record<RequestStatus, string> = {
    Draft: 'bg-gray-100 text-gray-800',
    PendingInitialApproval: 'bg-blue-100 text-blue-800',
    PendingReview: 'bg-yellow-100 text-yellow-800',
    PendingFinalApproval: 'bg-orange-100 text-orange-800',
    ReturnedToReviewer: 'bg-purple-100 text-purple-800',
    Approved: 'bg-green-100 text-green-800',
    Declined: 'bg-red-100 text-red-800',
    Implemented: 'bg-emerald-100 text-emerald-800',
  };
  return colors[status] || 'bg-gray-100 text-gray-800';
};

export const getStatusLabel = (status: RequestStatus): string => {
  const labels: Record<RequestStatus, string> = {
    Draft: 'Draft',
    PendingInitialApproval: 'Pending Initial Approval',
    PendingReview: 'Pending Review',
    PendingFinalApproval: 'Pending Final Approval',
    ReturnedToReviewer: 'Returned to Reviewer',
    Approved: 'Approved',
    Declined: 'Declined',
    Implemented: 'Implemented',
  };
  return labels[status] || status;
};

export const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
};
