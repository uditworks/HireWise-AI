import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { applicationsApi } from '../../api/applicationsApi';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  Mail,
  FileText,
  Briefcase,
  ArrowRight,
  Clock,
  Search,
  CheckCircle,
  AlertCircle
} from 'lucide-react';

export default function SeekerDashboardPage() {
  const { user, logout } = useAuth();
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    applicationsApi
      .getMyApplications({ size: 100, sort: 'createdAt,desc' })
      .then((data) => {
        setApplications(data?.content || []);
      })
      .catch((err) => {
        setError(err.message || 'Could not load application statistics.');
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  // Compute metrics from actual fetched applications
  const totalApps = applications.length;
  const activeApps = applications.filter(
    (app) => app.status === 'SUBMITTED' || app.status === 'UNDER_REVIEW'
  ).length;
  const shortlistedApps = applications.filter(
    (app) => app.status === 'SHORTLISTED' || app.status === 'ACCEPTED'
  ).length;
  const recentApps = applications.slice(0, 4);

  const formatDate = (isoString) => {
    if (!isoString) return '';
    try {
      const date = new Date(isoString);
      return date.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
    } catch {
      return '';
    }
  };

  return (
    <div className="flex-1 bg-slate-50 py-8 sm:py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        {/* Top Welcome Header */}
        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-navy-900 to-blue-600 text-white flex items-center justify-center font-bold text-xl shadow-md shadow-navy-900/10">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-extrabold text-navy-950">Welcome, {user?.fullName}</h1>
                <Badge status={user?.role} size="sm" />
              </div>
              <p className="text-xs text-slate-500 mt-1 flex items-center gap-1.5">
                <Mail className="w-3.5 h-3.5" /> {user?.email}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2.5">
            <Link to="/jobs">
              <Button variant="navy" size="sm">
                <Search className="w-4 h-4" />
                <span>Find Jobs</span>
              </Button>
            </Link>
            <Button variant="outline" size="sm" onClick={logout}>
              Sign Out
            </Button>
          </div>
        </div>

        {error && (
          <div className="bg-rose-50 border border-rose-200 text-rose-700 px-4 py-3 rounded-xl flex items-center gap-3 text-sm">
            <AlertCircle className="w-5 h-5 text-rose-500 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Real Metrics Summary Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
          {/* Card 1: Total Applications */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Applications</p>
              <h3 className="text-3xl font-extrabold text-navy-950">
                {loading ? <LoadingSpinner size="sm" /> : totalApps}
              </h3>
              <p className="text-[11px] text-slate-400">Lifetime submissions</p>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center">
              <Briefcase className="w-6 h-6" />
            </div>
          </div>

          {/* Card 2: In Review */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Under Review</p>
              <h3 className="text-3xl font-extrabold text-amber-600">
                {loading ? <LoadingSpinner size="sm" /> : activeApps}
              </h3>
              <p className="text-[11px] text-slate-400">Awaiting employer action</p>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center">
              <Clock className="w-6 h-6" />
            </div>
          </div>

          {/* Card 3: Shortlisted / Accepted */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Shortlisted / Offers</p>
              <h3 className="text-3xl font-extrabold text-emerald-600">
                {loading ? <LoadingSpinner size="sm" /> : shortlistedApps}
              </h3>
              <p className="text-[11px] text-slate-400">Positive candidate pipeline</p>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <CheckCircle className="w-6 h-6" />
            </div>
          </div>
        </div>

        {/* Recent Applications & Quick Actions Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left 2 Columns: Recent Applications Feed */}
          <div className="lg:col-span-2 bg-white p-6 sm:p-7 rounded-2xl border border-slate-200 shadow-sm space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div className="flex items-center gap-2">
                <Briefcase className="w-5 h-5 text-blue-600" />
                <h3 className="text-lg font-bold text-navy-950">Recent Applications</h3>
              </div>
              {totalApps > 0 && (
                <Link
                  to="/seeker/applications"
                  className="text-xs font-semibold text-blue-600 hover:text-blue-800 flex items-center gap-1"
                >
                  <span>View All ({totalApps})</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              )}
            </div>

            {loading ? (
              <div className="py-12 flex flex-col items-center justify-center space-y-2">
                <LoadingSpinner size="md" />
                <p className="text-xs text-slate-500">Loading recent submissions...</p>
              </div>
            ) : recentApps.length === 0 ? (
              <div className="py-10 text-center space-y-3">
                <div className="w-12 h-12 rounded-2xl bg-slate-100 text-slate-400 flex items-center justify-center mx-auto">
                  <Briefcase className="w-6 h-6" />
                </div>
                <p className="text-sm font-semibold text-slate-700">No applications yet</p>
                <p className="text-xs text-slate-500 max-w-sm mx-auto">
                  Find exciting job postings in our open catalog and submit your candidate profile.
                </p>
                <Link to="/jobs" className="inline-block pt-1">
                  <Button variant="navy" size="sm">
                    <Search className="w-3.5 h-3.5" />
                    <span>Explore Jobs</span>
                  </Button>
                </Link>
              </div>
            ) : (
              <div className="space-y-3">
                {recentApps.map((app) => (
                  <div
                    key={app.id}
                    className="p-4 rounded-xl bg-slate-50 border border-slate-200/80 hover:border-blue-200 transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                  >
                    <div>
                      <Link
                        to={`/jobs/${app.job?.id}`}
                        className="font-bold text-sm text-navy-950 hover:text-blue-600 transition-colors"
                      >
                        {app.job?.title}
                      </Link>
                      <div className="flex items-center gap-3 text-xs text-slate-500 mt-0.5">
                        {app.job?.location && <span>{app.job.location}</span>}
                        {app.createdAt && <span>Applied on {formatDate(app.createdAt)}</span>}
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge status={app.status} size="sm" />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Right Column: Portal Shortcuts */}
          <div className="space-y-6">
            {/* Action Card 1: My Applications */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3">
              <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center">
                <Briefcase className="w-5 h-5" />
              </div>
              <h4 className="font-bold text-navy-950 text-sm">Application Tracking</h4>
              <p className="text-xs text-slate-500 leading-relaxed">
                View your complete submission history, attached cover letters, and withdraw active applications when needed.
              </p>
              <Link to="/seeker/applications" className="block pt-1">
                <Button variant="outline" size="sm" className="w-full">
                  <span>Open Applications</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Button>
              </Link>
            </div>

            {/* Action Card 2: Resume Management & AI Matching */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3">
              <div className="w-10 h-10 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center">
                <FileText className="w-5 h-5" />
              </div>
              <h4 className="font-bold text-navy-950 text-sm">Resume & AI Skills</h4>
              <p className="text-xs text-slate-500 leading-relaxed">
                Upload PDF resumes, run deterministic skill extractions, and evaluate match compatibility with open positions.
              </p>
              <Link to="/seeker/resumes" className="block pt-1">
                <Button variant="outline" size="sm" className="w-full">
                  <span>Manage Resumes & AI</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
