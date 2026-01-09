'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';
import { useRequests } from '@/hooks/useRequests';
import { useNotification } from '@/hooks/useNotification';
import { formatDateTime, getStatusColor, getStatusLabel } from '@/utils/formatters';
import { Request } from '@/types';

export default function RequestDetailsPage() {
  const router = useRouter();
  const params = useParams();
  const requestId = params.id as string;
  const { isAuthenticated } = useAuth();
  const { getRequest, isLoading } = useRequests();
  const { error: notifyError } = useNotification();
  const [request, setRequest] = useState<Request | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    if (!requestId) return;

    getRequest(requestId)
      .then(setRequest)
      .catch((err) => {
        notifyError('Failed to load request details');
      });
  }, [isAuthenticated, router, requestId, getRequest, notifyError]);

  if (!isAuthenticated) {
    return null;
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-600">Loading request details...</p>
      </div>
    );
  }

  if (!request) {
    return (
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white shadow">
          <div className="max-w-7xl mx-auto px-4 py-6">
            <Link href="/requests" className="text-blue-600 hover:text-blue-700">
              ← Back to Requests
            </Link>
          </div>
        </header>
        <main className="max-w-7xl mx-auto px-4 py-8">
          <p className="text-gray-600">Request not found</p>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <Link href="/requests" className="text-blue-600 hover:text-blue-700 mb-4 block">
            ← Back to Requests
          </Link>
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Request Details</h1>
              <p className="text-gray-600 mt-1 font-mono">{request.requestId}</p>
            </div>
            <span className={`px-4 py-2 rounded-lg text-sm font-semibold ${getStatusColor(request.status)}`}>
              {getStatusLabel(request.status)}
            </span>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Request Information */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Request Information</h2>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-gray-600">Requestor</p>
                  <p className="text-lg font-semibold text-gray-900">{request.requestorName}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">System Name</p>
                  <p className="text-lg font-semibold text-gray-900">{request.systemName}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Access Type</p>
                  <p className="text-lg font-semibold text-gray-900">{request.accessType}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Created Date</p>
                  <p className="text-lg font-semibold text-gray-900">
                    {formatDateTime(request.createdAt)}
                  </p>
                </div>
              </div>
            </div>

            {/* Justification */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Justification</h2>
              <p className="text-gray-700 whitespace-pre-wrap">{request.justification}</p>
            </div>

            {/* Approvals */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Approvals</h2>
              {request.approvals.length === 0 ? (
                <p className="text-gray-600">No approvals yet</p>
              ) : (
                <div className="space-y-4">
                  {request.approvals.map((approval) => (
                    <div key={approval.approvalId} className="border border-gray-200 rounded-lg p-4">
                      <div className="flex justify-between items-start mb-2">
                        <div>
                          <p className="font-semibold text-gray-900">{approval.approverName}</p>
                          <p className="text-sm text-gray-600">{approval.approvalType}</p>
                        </div>
                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          approval.status === 'Approved'
                            ? 'bg-green-100 text-green-800'
                            : approval.status === 'Declined'
                            ? 'bg-red-100 text-red-800'
                            : 'bg-yellow-100 text-yellow-800'
                        }`}>
                          {approval.status}
                        </span>
                      </div>
                      {approval.comments && (
                        <p className="text-gray-700 text-sm mt-2">{approval.comments}</p>
                      )}
                      <p className="text-xs text-gray-500 mt-2">
                        {formatDateTime(approval.createdAt)}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Documents */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Documents</h2>
              {request.documents.length === 0 ? (
                <p className="text-gray-600">No documents attached</p>
              ) : (
                <div className="space-y-2">
                  {request.documents.map((doc) => (
                    <div key={doc.documentId} className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
                      <div>
                        <p className="font-semibold text-gray-900">{doc.fileName}</p>
                        <p className="text-xs text-gray-500">{formatDateTime(doc.uploadedAt)}</p>
                      </div>
                      <button className="text-blue-600 hover:text-blue-700 font-semibold">
                        Download
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* History */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">History</h2>
              {request.history.length === 0 ? (
                <p className="text-gray-600 text-sm">No history</p>
              ) : (
                <div className="space-y-3">
                  {request.history.slice(0, 5).map((entry, idx) => (
                    <div key={idx} className="text-sm border-l-2 border-blue-300 pl-3">
                      <p className="font-semibold text-gray-900">{entry.action}</p>
                      <p className="text-gray-600 text-xs">{entry.actor}</p>
                      <p className="text-gray-500 text-xs">{formatDateTime(entry.timestamp)}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Actions */}
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Actions</h2>
              <div className="space-y-2">
                {request.status === 'Draft' && (
                  <button className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition">
                    Submit Request
                  </button>
                )}
                {request.status === 'PendingInitialApproval' && (
                  <>
                    <button className="w-full bg-green-600 text-white py-2 rounded-lg hover:bg-green-700 transition">
                      Approve
                    </button>
                    <button className="w-full bg-red-600 text-white py-2 rounded-lg hover:bg-red-700 transition">
                      Decline
                    </button>
                  </>
                )}
                <button className="w-full bg-gray-200 text-gray-800 py-2 rounded-lg hover:bg-gray-300 transition">
                  View History
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
