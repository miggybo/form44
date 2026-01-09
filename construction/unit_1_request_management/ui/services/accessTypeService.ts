import { apiClient } from './api';
import { AccessType } from '@/types';

export const accessTypeService = {
  // List all access types
  listAccessTypes: async (): Promise<AccessType[]> => {
    const response = await apiClient.get<{ accessTypes: AccessType[] }>('/access-types');
    return response.data.accessTypes;
  },

  // Get access type details
  getAccessType: async (accessTypeId: string): Promise<AccessType> => {
    const response = await apiClient.get<AccessType>(`/access-types/${accessTypeId}`);
    return response.data;
  },

  // Create access type
  createAccessType: async (data: {
    name: string;
    description: string;
    administratorRoles: string[];
  }): Promise<AccessType> => {
    const response = await apiClient.post<AccessType>('/access-types', data);
    return response.data;
  },

  // Update access type routing
  updateAccessTypeRouting: async (
    accessTypeId: string,
    data: {
      administratorRoles: string[];
      defaultRole: string;
    }
  ): Promise<AccessType> => {
    const response = await apiClient.put<AccessType>(
      `/access-types/${accessTypeId}/routing`,
      data
    );
    return response.data;
  },
};
