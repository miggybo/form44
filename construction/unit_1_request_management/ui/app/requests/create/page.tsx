'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createRequestSchema, CreateRequestFormData } from '@/utils/validators';
import { useAuth } from '@/hooks/useAuth';
import { useRequests } from '@/hooks/useRequests';
import { useNotification } from '@/hooks/useNotification';
import { ACCESS_TYPES } from '@/utils/constants';

export default function CreateRequestPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const { createRequest, isLoading } = useRequests();
  const { success, error: notifyError } = useNotification();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateRequestFormData>({
    resolver: zodResolver(createRequestSchema),
  });

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, router]);

  const onSubmit = async (data: CreateRequestFormData) => {
    try {
      const request = await createRequest(data);
      success('Request created successfully');
      router.push(`/requests/${request.requestId}`);
    } catch (err: any) {
      notifyError(err.response?.data?.message || 'Failed to create request');
    }
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-4xl mx-auto px-4 py-6">
          <Link href="/requests" className="text-blue-600 hover:text-blue-700 mb-4 block">
            ← Back to Requests
          </Link>
          <h1 className="text-3xl font-bold text-gray-900">Create New Request</h1>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow p-8">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Access Type */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Access Type *
              </label>
              <select
                {...register('accessType')}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select an access type</option>
                {ACCESS_TYPES.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.label}
                  </option>
                ))}
              </select>
              {errors.accessType && (
                <p className="text-red-500 text-sm mt-1">{errors.accessType.message}</p>
              )}
            </div>

            {/* System Name */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                System Name *
              </label>
              <input
                type="text"
                {...register('systemName')}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="e.g., SAP ERP System"
              />
              {errors.systemName && (
                <p className="text-red-500 text-sm mt-1">{errors.systemName.message}</p>
              )}
            </div>

            {/* Justification */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Justification * (minimum 10 characters)
              </label>
              <textarea
                {...register('justification')}
                rows={6}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Provide a detailed justification for this access request..."
              />
              {errors.justification && (
                <p className="text-red-500 text-sm mt-1">{errors.justification.message}</p>
              )}
            </div>

            {/* Documents */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Supporting Documents (optional)
              </label>
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
                <p className="text-gray-600">Drag and drop files here or click to browse</p>
                <input
                  type="file"
                  multiple
                  className="hidden"
                  id="file-upload"
                />
                <label
                  htmlFor="file-upload"
                  className="mt-2 inline-block px-4 py-2 bg-blue-600 text-white rounded-lg cursor-pointer hover:bg-blue-700"
                >
                  Choose Files
                </label>
              </div>
            </div>

            {/* Buttons */}
            <div className="flex gap-4 pt-6 border-t border-gray-200">
              <button
                type="submit"
                disabled={isLoading}
                className="flex-1 bg-blue-600 text-white py-2 rounded-lg font-semibold hover:bg-blue-700 transition disabled:opacity-50"
              >
                {isLoading ? 'Creating...' : 'Create Request'}
              </button>
              <Link
                href="/requests"
                className="flex-1 bg-gray-200 text-gray-800 py-2 rounded-lg font-semibold hover:bg-gray-300 transition text-center"
              >
                Cancel
              </Link>
            </div>
          </form>
        </div>

        {/* Info Box */}
        <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <p className="text-sm text-blue-800">
            <strong>Note:</strong> After creating the request, you'll be able to add documents and submit it for approval.
          </p>
        </div>
      </main>
    </div>
  );
}
