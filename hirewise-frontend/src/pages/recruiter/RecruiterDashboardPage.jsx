import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { jobsApi } from '../../api/jobsApi';
import { useToast } from '../../context/ToastContext';
import { EMPLOYMENT_TYPE_LABELS } from '../../utils/constants';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  Briefcase,
  PlusCircle,
  Users,
  Edit,
  Trash2,
  Mail,
  MapPin,
  DollarSign,
  Calendar,
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  ExternalLink
} from 'lucide-react';

export default function RecruiterDashboardPage() {
  const { user, logout } = useAuth();
  const { showSuccess, showError } = useToast();

  const [jobsData, setJobsData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const pageSize = 10;

  // Deletion modal state
  const [deletingJob, setDeletingJob] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [updatingStatusId, setUpdatingStatusId] = useState(null);

  const fetchMyJobs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await jobsApi.getMyJobs({
        page,
        size: pageSize,
        sort: 'createdAt,desc'
      });
      setJobsData(data);
    } catch (err) {
      setError(err.message || 'Could not load your job postings.');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchMyJobs();
  }, [fetchMyJobs]);

  const handleStatusChange = async (jobId, newStatus) => {
    setUpdatingStatusId(jobId);
    try {
      await jobsApi.updateJobStatus(jobId, newStatus);
      showSuccess(`Job status updated to ${newStatus}.`);
      setJobsData((prev) => ({
        ...prev,
        content: prev.content.map((job) =>
          job.id === jobId ? { ...job, status: newStatus } : job
        )
      }));
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to update job status.';
      showError(msg);
    } finally {
      setUpdatingStatusId(null);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deletingJob) return;
    setDeleting(true);
    try {
      await jobsApi.deleteJob(deletingJob.id);
      showSuccess(`Job "${deletingJob.title}" deleted successfully.`);
      setDeletingJob(null);
      fetchMyJobs();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to delete job.';
      showError(msg);
    } finally {
      setDeleting(false);
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

  const jobsList = jobsData?.content || [];
  const totalElements = jobsData?.totalElements || 0;
  const totalPages = jobsData?.totalPages || 0;

  const publishedCount = jobsList.filter((j) => j.status === 'PUBLISHED').length;
  const draftCount = jobsList.filter((j) => j.status === 'DRAFT').length;
  const closedCount = jobsList.filter((j) => j.status === 'CLOSED').length;

  return (
    <div className="flex-1 bg-slate-50 py-8 sm:py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        {/* Top Header Card */}
        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-indigo-900 to-blue-600 text-white flex items-center justify-center font-bold text-xl shadow-md shadow-indigo-900/10">
              {user?.fullName?.charAt(0) || 'R'}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-extrabold text-navy-950">Recruiter Hub: {user?.fullName}</h1>
                <Badge status={user?.role} size="sm" />
              </div>
              <p className="text-xs text-slate-500 mt-1 flex items-center gap-1.5">
                <Mail className="w-3.5 h-3.5" /> {user?.email}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Link to="/recruiter/jobs/create">
              <Button variant="navy" size="sm">
                <PlusCircle className="w-4 h-4" />
                <span>Post New Job</span>
              </Button>
            </Link>
            <Button variant="outline" size="sm" onClick={logout}>
              Sign Out
            </Button>
          </div>
        </div>

        {/* Real Job Stats Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-1">
            <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Total Postings</p>
            <h3 className="text-2xl font-extrabold text-navy-950">
              {loading ? <LoadingSpinner size="sm" /> : totalElements}
            </h3>
            <p className="text-[11px] text-slate-400">Created by your account</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-1">
            <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Published</p>
            <h3 className="text-2xl font-extrabold text-emerald-600">
              {loading ? <LoadingSpinner size="sm" /> : publishedCount}
            </h3>
            <p className="text-[11px] text-slate-400">Accepting applications</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-1">
            <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Drafts</p>
            <h3 className="text-2xl font-extrabold text-slate-600">
              {loading ? <LoadingSpinner size="sm" /> : draftCount}
            </h3>
            <p className="text-[11px] text-slate-400">Unpublished postings</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-1">
            <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Closed</p>
            <h3 className="text-2xl font-extrabold text-red-600">
              {loading ? <LoadingSpinner size="sm" /> : closedCount}
            </h3>
            <p className="text-[11px] text-slate-400">Archived positions</p>
          </div>
        </div>

        {/* Content Viewport */}
        {loading ? (
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
            <LoadingSpinner size="lg" />
            <p className="text-sm font-medium text-slate-600">Retrieving your job postings...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-2xl border border-red-200 p-10 flex flex-col items-center justify-center text-center space-y-4 shadow-sm">
            <div className="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center">
              <AlertCircle className="w-6 h-6" />
            </div>
            <div className="max-w-md">
              <h3 className="text-base font-bold text-slate-900">Unable to Load Jobs</h3>
              <p className="text-xs text-slate-500 mt-1">{error}</p>
            </div>
            <Button variant="primary" size="sm" onClick={fetchMyJobs}>
              <span>Try Again</span>
            </Button>
          </div>
        ) : jobsList.length === 0 ? (
          /* Empty State */
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-4 shadow-sm">
            <div className="w-16 h-16 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto">
              <Briefcase className="w-8 h-8" />
            </div>
            <div className="max-w-md mx-auto space-y-1">
              <h3 className="text-lg font-bold text-navy-950">No Job Postings Yet</h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                You haven't posted any jobs under this recruiter account. Create your first opening to begin receiving qualified candidates.
              </p>
            </div>
            <div className="pt-2">
              <Link to="/recruiter/jobs/create">
                <Button variant="navy" size="md">
                  <PlusCircle className="w-4 h-4" />
                  <span>Post Your First Job</span>
                </Button>
              </Link>
            </div>
          </div>
        ) : (
          /* Jobs Management Grid */
          <div className="space-y-4">
            <div className="flex items-center justify-between text-xs text-slate-500 px-1">
              <span>
                Showing <strong className="font-semibold text-navy-950">{jobsList.length}</strong> of{' '}
                <strong className="font-semibold text-navy-950">{totalElements}</strong> job postings
              </span>
              <Link
                to="/recruiter/jobs/create"
                className="text-blue-600 hover:text-blue-800 font-semibold hover:underline flex items-center gap-1"
              >
                <PlusCircle className="w-3.5 h-3.5" />
                <span>Add Another Position</span>
              </Link>
            </div>

            {jobsList.map((job) => (
              <div
                key={job.id}
                className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm hover:border-slate-300 transition-all space-y-4"
              >
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-2.5">
                      <Link
                        to={`/recruiter/jobs/${job.id}/applicants`}
                        className="text-lg font-bold text-navy-950 hover:text-blue-600 transition-colors"
                      >
                        {job.title}
                      </Link>
                      <span className="text-xs text-slate-400 font-mono">#{job.id}</span>
                    </div>

                    <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500 mt-1">
                      {job.location && (
                        <span className="flex items-center gap-1">
                          <MapPin className="w-3.5 h-3.5 text-slate-400" />
                          <span>{job.location}</span>
                        </span>
                      )}
                      <span>{EMPLOYMENT_TYPE_LABELS[job.employmentType] || job.employmentType}</span>
                      {job.salaryRange && (
                        <span className="flex items-center gap-1 text-emerald-700 font-semibold">
                          <DollarSign className="w-3.5 h-3.5 text-emerald-600" />
                          <span>{job.salaryRange}</span>
                        </span>
                      )}
                      {job.createdAt && (
                        <span className="flex items-center gap-1 text-slate-400">
                          <Calendar className="w-3.5 h-3.5" />
                          <span>Created {formatDate(job.createdAt)}</span>
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Status Dropdown Controls */}
                  <div className="flex items-center gap-2">
                    <select
                      value={job.status}
                      onChange={(e) => handleStatusChange(job.id, e.target.value)}
                      disabled={updatingStatusId === job.id}
                      className="rounded-xl border border-slate-300 bg-white text-slate-900 text-xs font-semibold px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-100"
                    >
                      <option value="PUBLISHED">🟢 PUBLISHED</option>
                      <option value="DRAFT">⚪ DRAFT</option>
                      <option value="CLOSED">🔴 CLOSED</option>
                    </select>
                    <Badge status={job.status} size="sm" />
                  </div>
                </div>

                {/* Description Snippet & Skills */}
                <div className="space-y-2">
                  <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                    {job.description}
                  </p>
                  {job.requiredSkills && job.requiredSkills.length > 0 && (
                    <div className="flex flex-wrap gap-1.5 pt-1">
                      {Array.from(job.requiredSkills).map((skill, idx) => (
                        <span
                          key={idx}
                          className="px-2.5 py-0.5 rounded-lg bg-slate-100 text-slate-700 text-[11px] font-medium border border-slate-200"
                        >
                          {skill}
                        </span>
                      ))}
                    </div>
                  )}
                </div>

                {/* Footer Action Bar */}
                <div className="pt-3 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3">
                  <div className="flex items-center gap-2">
                    {/* View Applicants Button */}
                    <Link to={`/recruiter/jobs/${job.id}/applicants`}>
                      <Button variant="navy" size="sm">
                        <Users className="w-3.5 h-3.5" />
                        <span>View Applicants</span>
                      </Button>
                    </Link>

                    {/* Edit Job Button */}
                    <Link to={`/recruiter/jobs/${job.id}/edit`}>
                      <Button variant="outline" size="sm">
                        <Edit className="w-3.5 h-3.5" />
                        <span>Edit</span>
                      </Button>
                    </Link>

                    {/* Public Preview */}
                    <Link to={`/jobs/${job.id}`} target="_blank" rel="noopener noreferrer">
                      <Button variant="secondary" size="sm">
                        <ExternalLink className="w-3.5 h-3.5" />
                        <span>Public Link</span>
                      </Button>
                    </Link>
                  </div>

                  {/* Delete Trigger */}
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => setDeletingJob(job)}
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                    <span>Delete</span>
                  </Button>
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
                  disabled={jobsData?.last}
                >
                  <span>Next</span>
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Delete Confirmation Modal Dialog */}
      {deletingJob && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="relative w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-2xl p-6 space-y-4 animate-in fade-in zoom-in-95">
            <div className="w-12 h-12 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center">
              <AlertCircle className="w-6 h-6" />
            </div>

            <div className="space-y-1">
              <h3 className="text-lg font-bold text-navy-950">Delete Job Posting?</h3>
              <p className="text-xs text-slate-600 leading-relaxed">
                Are you sure you want to permanently delete{' '}
                <strong className="text-slate-900 font-semibold">"{deletingJob.title}"</strong> (Job #{deletingJob.id})?
                This will remove the posting from the database.
              </p>
            </div>

            <div className="pt-2 flex items-center justify-end gap-3 border-t border-slate-100">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setDeletingJob(null)}
                disabled={deleting}
              >
                Cancel
              </Button>
              <Button
                type="button"
                variant="danger"
                size="sm"
                onClick={handleConfirmDelete}
                loading={deleting}
              >
                <span>Delete Job</span>
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
