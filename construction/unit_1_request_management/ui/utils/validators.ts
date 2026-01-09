import { z } from 'zod';

// Request validation schemas
export const createRequestSchema = z.object({
  accessType: z.string().min(1, 'Access type is required'),
  systemName: z.string().min(1, 'System name is required'),
  justification: z
    .string()
    .min(10, 'Justification must be at least 10 characters')
    .max(10000, 'Justification must not exceed 10000 characters'),
});

export const submitRequestSchema = z.object({
  headOfOfficeId: z.string().min(1, 'Head of Office is required'),
});

export const approveRequestSchema = z.object({
  comments: z.string().optional(),
});

export const declineRequestSchema = z.object({
  reason: z.string().min(10, 'Reason must be at least 10 characters'),
});

export const endorseRequestSchema = z.object({
  comments: z.string().optional(),
});

export const returnRequestSchema = z.object({
  reason: z.string().min(10, 'Reason must be at least 10 characters'),
});

export const implementRequestSchema = z.object({
  notes: z.string().optional(),
});

// Access type validation schemas
export const createAccessTypeSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  administratorRoles: z.array(z.string()).min(1, 'At least one administrator role is required'),
});

export const updateAccessTypeRoutingSchema = z.object({
  administratorRoles: z.array(z.string()).min(1, 'At least one administrator role is required'),
  defaultRole: z.string().min(1, 'Default role is required'),
});

// Auth validation schemas
export const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

// Search validation schema
export const searchSchema = z.object({
  q: z.string().optional(),
  accessType: z.string().optional(),
  status: z.string().optional(),
  officeId: z.string().optional(),
  fromDate: z.string().optional(),
  toDate: z.string().optional(),
  page: z.number().optional(),
  size: z.number().optional(),
});

// Type exports for form usage
export type CreateRequestFormData = z.infer<typeof createRequestSchema>;
export type SubmitRequestFormData = z.infer<typeof submitRequestSchema>;
export type ApproveRequestFormData = z.infer<typeof approveRequestSchema>;
export type DeclineRequestFormData = z.infer<typeof declineRequestSchema>;
export type EndorseRequestFormData = z.infer<typeof endorseRequestSchema>;
export type ReturnRequestFormData = z.infer<typeof returnRequestSchema>;
export type ImplementRequestFormData = z.infer<typeof implementRequestSchema>;
export type CreateAccessTypeFormData = z.infer<typeof createAccessTypeSchema>;
export type UpdateAccessTypeRoutingFormData = z.infer<typeof updateAccessTypeRoutingSchema>;
export type LoginFormData = z.infer<typeof loginSchema>;
export type SearchFormData = z.infer<typeof searchSchema>;
