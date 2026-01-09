'use client';

import { useState, useCallback } from 'react';
import { requestService } from '@/services/requestService';
import { Request, CreateRequestForm, PaginatedResponse } from '@/types';

export const useRequests = () => {
  const [requests, setRequests] = useState<Request[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState({
    page: 1,
    size: 20,
    totalCount: 0,
    totalPages: 0,
  });

  const listRequests = useCallback(
    async (params?: {
      status?: string;
      requestorId?: string;
      accessType?: string;
      fromDate?: string;
      toDate?: string;
      page?: number;
      size?: number;
      sortBy?: string;
      sortOrder?: string;
    }) => {
      setIsLoading(true);
      setError(null);
      try {
        const response: PaginatedResponse<Request> = await requestService.listRequests(params);
        setRequests(response.items);
        setPagination({
          page: response.page,
          size: response.size,
          totalCount: response.totalCount,
          totalPages: response.totalPages,
        });
        return response;
      } catch (err: any) {
        const errorMessage = err.response?.data?.message || 'Failed to load requests';
        setError(errorMessage);
        throw err;
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  const getRequest = useCallback(async (requestId: string) => {
    setIsLoading(true);
    setError(null);
    try {
      const request = await requestService.getRequest(requestId);
      return request;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Failed to load request';
      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const createRequest = useCallback(async (data: CreateRequestForm) => {
    setIsLoading(true);
    setError(null);
    try {
      const request = await requestService.createRequest(data);
      return request;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Failed to create request';
      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const submitRequest = useCallback(async (requestId: string, headOfOfficeId: string) => {
    setIsLoading(true);
    setError(null);
    try {
      const request = await requestService.submitRequest(requestId, headOfOfficeId);
      return request;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Failed to submit request';
      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const searchRequests = useCallback(
    async (params?: {
      q?: string;
      accessType?: string;
      status?: string;
      officeId?: string;
      fromDate?: string;
      toDate?: string;
      page?: number;
      size?: number;
    }) => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await requestService.searchRequests(params);
        setRequests(response.results);
        setPagination({
          page: params?.page || 1,
          size: params?.size || 20,
          totalCount: response.totalCount,
          totalPages: Math.ceil(response.totalCount / (params?.size || 20)),
        });
        return response;
      } catch (err: any) {
        const errorMessage = err.response?.data?.message || 'Failed to search requests';
        setError(errorMessage);
        throw err;
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  return {
    requests,
    isLoading,
    error,
    pagination,
    listRequests,
    getRequest,
    createRequest,
    submitRequest,
    searchRequests,
  };
};
