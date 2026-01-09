'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';
import { useRequests } from '@/hooks/useRequests';
import { formatDate, getStatusColor, getStatusLabel } from '@/utils/formatters';

export default function ApprovalsPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const { requests, listRequests, isLoading } = useRequests();

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    // Load pending requests
    listRequests({
      status: 'PendingInitialApproval',
      page: 1,
      size: 20,
    }).catch(() => {
      // Handle error silently for demo
    });
  }, [isAuthenticated, router, listRequests]);

  if (!isAuthenticated) {
    return null;
  }

  const pendingRequests = requests.filter(
    (r) =>
      r.status === 'PendingInitialApproval' ||
      r.status === 'PendingReview' ||
      r.status === 'PendingFinalApproval'
  );

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <h1 className="text-3xl font-bold text-gray-900">Pending Approvals</h1>
          <p className="text-gray-600 mt-1">{pendingRequests.length} requests awaiting your action</p>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            {isLoading ? (
              <div className="p-6 text-center text-gray-500">Loading approvals...</div>
            ) : pendingRequests.length === 0 ? (
              <div className="p-6 text-center text-gray-500">No pending approvals</div>
            ) : (
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Request ID
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Requestor
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      System
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Access Type
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Submitted
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {pendingRequests.map((request) => (
                    <tr key={request.requestId} className="border-b border-gray-200 hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm text-gray-900 font-mono">
                        {request.requestId.substring(0, 8)}...
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {request.requestorName}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {request.systemName}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {request.accessType}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusColor(request.status)}`}>
                          {getStatusLabel(request.status)}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {request.submittedAt ? formatDate(request.submittedAt) : 'N/A'}
                      </td>
                      <td className="px-6 py-4 text-sm space-x-2">
                        <Link
                          href={`/requests/${request.requestId}`}
                          className="text-blue-600 hover:text-blue-700 font-semibold"
                        >
                          Review
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
