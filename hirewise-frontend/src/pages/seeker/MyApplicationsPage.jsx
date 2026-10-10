import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { applicationsApi } from '../../api/applicationsApi';
import { useToast } from '../../context/ToastContext';
import { EMPLOYMENT_TYPE_LABELS } from '../../utils/constants';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  Briefcase,
  MapPin,
  FileText,
  AlertCircle,
  Clock,
  ArrowRight,
  ChevronLeft,
  ChevronRight,
  Trash2,
  Search
} from 'lucide-react';

export default function MyApplicationsPage() {
  const { showSuccess, showError } = useToast();

  const [applicationsData, setApplicationsData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const pageSize = 10;

  // Withdrawal confirmation state
  const [withdrawingApp, setWithdrawingApp] = useState(null);
  const [withdrawing, setWithdrawing] = useState(false);

  const fetchApplications = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await applicationsApi.getMyApplications({
        page,
        size: pageSize,
        sort: 'createdAt,desc'
      });
      setApplicationsData(data);
    } catch (err) {
      setError(err.message || 'Failed to retrieve your job applications.');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

  const handleConfirmWithdraw = async () => {
    if (!withdrawingApp) return;

    setWithdrawing(true);
    try {
      await applicationsApi.withdrawApplication(withdrawingApp.id);
      showSuccess(`Application for "${withdrawingApp.job?.title}" successfully withdrawn.`);
      setWithdrawingApp(null);
      fetchApplications();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to withdraw application.';
      showError(msg);
    } finally {
      setWithdrawing(false);
    }
  };

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

  const isEligibleForWithdrawal = (status) => {
    return status === 'SUBMITTED' || status === 'UNDER_REVIEW' || status === 'SHORTLISTED';
  };

  const appsList = applicationsData?.content || [];
  const totalElements = applicationsData?.totalElements || 0;
  const totalPages = applicationsData?.totalPages || 0;

  return (
    <div className="flex-1 bg-slate-50 py-8 sm:py-12">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        {/* Page Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-50 border border-blue-200/80 text-blue-700 text-xs font-semibold uppercase tracking-wider mb-1">
              <Briefcase className="w-3.5 h-3.5 text-blue-600" />
              <span>Candidate Portal</span>
            </div>
            <h1 className="text-3xl font-extrabold text-navy-950 tracking-tight">
              My Job Applications
            </h1>
            <p className="text-sm text-slate-500">
              Track the live review status of all your submitted applications in real time.
            </p>
          </div>

          <Link to="/jobs">
            <Button variant="navy" size="sm">
              <Search className="w-4 h-4" />
              <span>Browse More Jobs</span>
            </Button>
          </Link>
        </div>

        {/* Content Viewport */}
        {loading ? (
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
            <LoadingSpinner size="lg" />
            <p className="text-sm font-medium text-slate-600">Retrieving your submitted applications...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-2xl border border-red-200 p-10 flex flex-col items-center justify-center text-center space-y-4 shadow-sm">
            <div className="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center">
              <AlertCircle className="w-6 h-6" />
            </div>
            <div className="max-w-md">
              <h3 className="text-base font-bold text-slate-900">Unable to Load Applications</h3>
              <p className="text-xs text-slate-500 mt-1">{error}</p>
            </div>
            <Button variant="primary" size="sm" onClick={fetchApplications}>
              <span>Try Again</span>
            </Button>
          </div>
        ) : appsList.length === 0 ? (
          /* Empty State */
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-4 shadow-sm">
            <div className="w-16 h-16 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center mx-auto">
              <Briefcase className="w-8 h-8" />
            </div>
            <div className="max-w-md mx-auto space-y-1">
              <h3 className="text-lg font-bold text-navy-950">No Applications Yet</h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                You have not submitted any job applications yet. Browse published opportunities and start applying with one click.
              </p>
            </div>
            <div className="pt-2">
              <Link to="/jobs">
                <Button variant="navy" size="md">
                  <Search className="w-4 h-4" />
                  <span>Explore Open Positions</span>
                </Button>
              </Link>
            </div>
          </div>
        ) : (
          /* Applications List */
          <div className="space-y-4">
            <div className="text-xs text-slate-500 px-1">
              Showing <strong className="font-semibold text-navy-950">{appsList.length}</strong> of{' '}
              <strong className="font-semibold text-navy-950">{totalElements}</strong> total applications
            </div>

            {appsList.map((app) => (
              <div
                key={app.id}
                className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm hover:border-slate-300 transition-all space-y-4"
              >
                {/* Header Row */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-2">
                      <Link
                        to={`/jobs/${app.job?.id}`}
                        className="text-lg font-bold text-navy-950 hover:text-blue-600 transition-colors"
                      >
                        {app.job?.title}
                      </Link>
                      <span className="text-xs text-slate-400 font-mono">App #{app.id}</span>
                    </div>

                    <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500 mt-1">
                      {app.job?.location && (
                        <span className="flex items-center gap-1">
                          <MapPin className="w-3.5 h-3.5 text-slate-400" />
                          <span>{app.job.location}</span>
                        </span>
                      )}
                      {app.job?.employmentType && (
                        <span>
                          {EMPLOYMENT_TYPE_LABELS[app.job.employmentType] || app.job.employmentType}
                        </span>
                      )}
                      {app.createdAt && (
                        <span className="flex items-center gap-1 text-slate-400">
                          <Clock className="w-3.5 h-3.5" />
                          <span>Applied on {formatDate(app.createdAt)}</span>
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Status Badge */}
                  <div className="self-start sm:self-auto flex items-center gap-2">
                    <Badge status={app.status} size="md" />
                  </div>
                </div>

                {/* Details Row: Attached Resume & Cover Letter */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pt-2 text-xs">
                  {/* Resume Info */}
                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200/70 flex items-center gap-2.5">
                    <FileText className="w-4 h-4 text-blue-600 flex-shrink-0" />
                    <div className="truncate">
                      <span className="font-semibold text-slate-700">Resume: </span>
                      <span className="text-slate-600 font-mono text-[11px]">
                        {app.resume ? app.resume.originalFilename : 'No document attached'}
                      </span>
                    </div>
                  </div>

                  {/* Cover letter summary */}
                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200/70 truncate">
                    <span className="font-semibold text-slate-700">Cover Letter: </span>
                    <span className="text-slate-600 italic">
                      {app.coverLetter ? `"${app.coverLetter.slice(0, 70)}..."` : 'None provided'}
                    </span>
                  </div>
                </div>

                {/* Footer Row: Actions */}
                <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                  <Link
                    to={`/jobs/${app.job?.id}`}
                    className="text-xs font-semibold text-blue-600 hover:text-blue-800 flex items-center gap-1"
                  >
                    <span>View Job Details</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>

                  {/* Withdrawal Action Button (if eligible) */}
                  {isEligibleForWithdrawal(app.status) && (
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => setWithdrawingApp(app)}
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      <span>Withdraw Application</span>
                    </Button>
                  )}
                </div>
              </div>
            ))}

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="pt-6 flex items-center justify-between border-t border-slate-200">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setPage((prev) => Math.max(0, prev - 1));
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                  }}
                  disabled={page === 0}
                >
                  <ChevronLeft className="w-4 h-4" />
                  <span>Previous</span>
                </Button>

                <div className="text-xs font-semibold text-slate-600">
                  Page <span className="text-navy-950 font-bold">{page + 1}</span> of{' '}
                  <span className="text-navy-950 font-bold">{totalPages}</span>
                </div>

                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setPage((prev) => prev + 1);
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                  }}
                  disabled={applicationsData?.last}
                >
                  <span>Next</span>
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Withdrawal Confirmation Dialog */}
      {withdrawingApp && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="relative w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-2xl p-6 space-y-4 animate-in fade-in zoom-in-95">
            <div className="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center">
              <AlertCircle className="w-6 h-6" />
            </div>

            <div className="space-y-1">
              <h3 className="text-lg font-bold text-navy-950">Withdraw Application?</h3>
              <p className="text-xs text-slate-600 leading-relaxed">
                Are you sure you want to withdraw your application for{' '}
                <strong className="text-slate-900 font-semibold">{withdrawingApp.job?.title}</strong>?
                This action is final and the recruiter will be notified of your withdrawal.
              </p>
            </div>

            <div className="pt-2 flex items-center justify-end gap-3 border-t border-slate-100">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setWithdrawingApp(null)}
                disabled={withdrawing}
              >
                Keep Application
              </Button>
              <Button
                type="button"
                variant="danger"
                size="sm"
                onClick={handleConfirmWithdraw}
                loading={withdrawing}
              >
                <span>Confirm Withdrawal</span>
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
