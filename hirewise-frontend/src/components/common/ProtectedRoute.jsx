import React from 'react';
import { Navigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ShieldAlert, ArrowLeft } from 'lucide-react';
import Button from './Button';

export default function ProtectedRoute({ children, allowedRoles }) {
  const { isAuthenticated, user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  // Not logged in -> redirect to login preserving intended path
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Role check if specific roles are required
  if (allowedRoles && (!user || !allowedRoles.includes(user.role))) {
    return (
      <div className="max-w-xl mx-auto my-16 p-8 bg-white rounded-2xl border border-red-200 shadow-sm text-center">
        <div className="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center mx-auto mb-4">
          <ShieldAlert className="w-6 h-6" />
        </div>
        <h2 className="text-xl font-bold text-navy-950 mb-2">Access Restricted</h2>
        <p className="text-sm text-slate-600 mb-6">
          This portal section requires a <span className="font-semibold">{allowedRoles.join(' or ')}</span> role.
          Your current account is registered as <span className="font-semibold text-blue-600">{user?.role}</span>.
        </p>
        <div className="flex items-center justify-center gap-3">
          <Link to={user?.role === 'JOB_SEEKER' ? '/seeker/dashboard' : '/recruiter/dashboard'}>
            <Button variant="primary">
              <span>Go to My Dashboard</span>
            </Button>
          </Link>
          <Link to="/">
            <Button variant="secondary">
              <ArrowLeft className="w-4 h-4" />
              <span>Back to Home</span>
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  return children;
}
