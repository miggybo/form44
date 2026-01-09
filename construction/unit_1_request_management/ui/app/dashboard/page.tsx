'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';
import { useRequests } from '@/hooks/useRequests';
import { formatDate, getStatusColor, getStatusLabel } from '@/utils/formatters';

export default function DashboardPage() {
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();
  const { requests, listRequests, isLoading } = useRequests();
  const [stats, setStats] = useState({
    total: 0,
    pending: 0,
    approved: 0,
    declined: 0,
  });

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    // Load requests
    listRequests({ page: 1, size: 10 }).catch(() => {
      // Handle error silently for demo
    });
  }, [isAuthenticated, router, listRequests]);

  useEffect(() => {
    // Calculate stats
    const total = requests.length;
    const pending = requests.filter(
      (r) =>
        r.status === 'PendingInitialApproval' ||
        r.status === 'PendingReview' ||
        r.status === 'PendingFinalApproval'
    ).length;
    const approved = requests.filter((r) => r.status === 'Approved').length;
    const declined = requests.filter((r) => r.status === 'Declined').length;

    setStats({ total, pending, approved, declined });
  }, [requests]);

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-6 flex justify-between items-center">
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <div className="flex items-center gap-4">
            <span className="text-gray-600">{user?.name}</span>
            <Link
              href="/login"
              className="text-red-600 hover:text-red-700 font-semibold"
            >
              Logout
            </Link>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-gray-600 text-sm font-medium">Total Requests</p>
            <p className="text-3xl font-bold text-gray-900 mt-2">{stats.total}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-gray-600 text-sm font-medium">Pending</p>
            <p className="text-3xl font-bold text-yellow-600 mt-2">{stats.pending}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-gray-600 text-sm font-medium">Approved</p>
            <p className="text-3xl font-bold text-green-600 mt-2">{stats.approved}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-gray-600 text-sm font-medium">Declined</p>
            <p className="text-3xl font-bold text-red-600 mt-2">{stats.declined}</p>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <Link
            href="/requests/create"
            className="bg-blue-600 text-white rounded-lg shadow p-6 hover:bg-blue-700 transition text-center"
          >
            <h3 className="text-lg font-semibold">Create Request</h3>
            <p className="text-blue-100 mt-2">Submit a new access request</p>
          </Link>
          <Link
            href="/requests"
            className="bg-indigo-600 text-white rounded-lg shadow p-6 hover:bg-indigo-700 transition text-center"
          >
            <h3 className="text-lg font-semibold">View Requests</h3>
            <p className="text-indigo-100 mt-2">Browse all requests</p>
          </Link>
          <Link
            href="/approvals"
            className="bg-purple-600 text-white rounded-lg shadow p-6 hover:bg-purple-700 transition text-center"
          >
            <h3 className="text-lg font-semibold">Pending Approvals</h3>
            <p className="text-purple-100 mt-2">Review pending requests</p>
          </Link>
        </div>

        {/* Recent Requests */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">Recent Requests</h2>
          </div>
          <div className="overflow-x-auto">
            {isLoading ? (
              <div className="p-6 text-center text-gray-500">Loading requests...</div>
            ) : requests.length === 0 ? (
              <div className="p-6 text-center text-gray-500">No requests found</div>
            ) : (
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Request ID
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
                      Date
                    </th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">
                      Action
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {requests.map((request) => (
                    <tr key={request.requestId} className="border-b border-gray-200 hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm text-gray-900 font-mono">
                        {request.requestId.substring(0, 8)}...
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
                        {formatDate(request.createdAt)}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <Link
                          href={`/requests/${request.requestId}`}
                          className="text-blue-600 hover:text-blue-700 font-semibold"
                        >
                          View
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
