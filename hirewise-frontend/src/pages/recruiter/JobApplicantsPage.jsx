import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { jobsApi } from '../../api/jobsApi';
import { applicationsApi } from '../../api/applicationsApi';
import axiosClient from '../../api/axiosClient';
import { useToast } from '../../context/ToastContext';
import { APPLICATION_STATUS } from '../../utils/constants';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  ArrowLeft,
  Users,
  Briefcase,
  Mail,
  FileText,
  Download,
  Filter,
  ChevronLeft,
  ChevronRight,
  ShieldAlert
} from 'lucide-react';

export default function JobApplicantsPage() {
  const { jobId } = useParams();
  const { showSuccess, showError } = useToast();

  const [job, setJob] = useState(null);
  const [applicationsData, setApplicationsData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [page, setPage] = useState(0);
  const [updatingId, setUpdatingId] = useState(null);
  const [downloadingId, setDownloadingId] = useState(null);

  const fetchJobAndApplicants = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [jobData, appsData] = await Promise.all([
        jobsApi.getJobById(jobId),
        applicationsApi.getApplicationsForJob(jobId, {
          status: statusFilter !== 'ALL' ? statusFilter : undefined,
          page,
          size: 10,
          sort: 'createdAt,desc'
        })
      ]);
      setJob(jobData);
      setApplicationsData(appsData);
    } catch (err) {
      setError(
        err.response?.status === 403
          ? 'You do not have permission to view applicants for this job.'
          : err.message || 'Could not retrieve applicants.'
      );
    } finally {
      setLoading(false);
    }
  }, [jobId, statusFilter, page]);

  useEffect(() => {
    fetchJobAndApplicants();
  }, [fetchJobAndApplicants]);

  const handleStatusChange = async (appId, newStatus) => {
    setUpdatingId(appId);
    try {
      await applicationsApi.updateApplicationStatus(appId, newStatus);
      showSuccess(`Application status updated to ${newStatus}.`);
      // Update in local state
      setApplicationsData((prev) => ({
        ...prev,
        content: prev.content.map((app) =>
          app.id === appId ? { ...app, status: newStatus } : app
        )
      }));
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to update status.';
      showError(msg);
    } finally {
      setUpdatingId(null);
    }
  };

  const handleDownloadResume = async (resumeId, filename) => {
    if (!resumeId) return;
    setDownloadingId(resumeId);
    try {
      const response = await axiosClient.get(`/resumes/${resumeId}/download`, {
        responseType: 'blob'
      });
      const blobUrl = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = blobUrl;
      link.setAttribute('download', filename || `resume_${resumeId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(blobUrl);
      showSuccess('Resume downloaded successfully.');
    } catch (_err) {
      showError('Could not download resume. File may not be available.');
    } finally {
      setDownloadingId(null);
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

  const applicantsList = applicationsData?.content || [];
  const totalElements = applicationsData?.totalElements || 0;
  const totalPages = applicationsData?.totalPages || 0;

  return (
    <div className="flex-1 bg-slate-50 py-8 sm:py-12">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
        {/* Back Link */}
        <div>
          <Link
            to="/recruiter/dashboard"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-blue-600 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Recruiter Hub</span>
          </Link>
        </div>

        {/* Header Summary */}
        <div className="bg-white p-6 sm:p-7 rounded-2xl border border-slate-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <Badge status={job?.status} size="sm" />
              <span className="text-xs text-slate-400 font-mono">Job #{jobId}</span>
            </div>
            <h1 className="text-2xl font-extrabold text-navy-950">{job?.title || 'Job Applicants'}</h1>
            <p className="text-xs text-slate-500 flex items-center gap-2">
              <span>{job?.location}</span>
              <span>•</span>
              <span>{job?.employmentType}</span>
            </p>
          </div>

          <div className="flex items-center gap-2.5">
            <Link to={`/recruiter/jobs/${jobId}/edit`}>
              <Button variant="outline" size="sm">
                <span>Edit Job Details</span>
              </Button>
            </Link>
            <Link to={`/jobs/${jobId}`}>
              <Button variant="secondary" size="sm">
                <span>Public View</span>
              </Button>
            </Link>
          </div>
        </div>

        {/* Filter Controls Bar */}
        <div className="bg-white p-4 sm:p-5 rounded-2xl border border-slate-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-slate-400" />
            <span className="text-xs font-semibold text-slate-700">Filter by Review Stage:</span>
          </div>

          <div className="flex items-center gap-2">
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
              className="rounded-xl border border-slate-300 bg-white text-slate-900 text-xs font-semibold px-3 py-2 transition-all focus:outline-none focus:ring-2 focus:ring-blue-100"
            >
              <option value="ALL">All Application Stages ({totalElements})</option>
              <option value="SUBMITTED">Submitted (New)</option>
              <option value="UNDER_REVIEW">Under Review</option>
              <option value="SHORTLISTED">Shortlisted</option>
              <option value="ACCEPTED">Accepted</option>
              <option value="REJECTED">Rejected</option>
              <option value="WITHDRAWN">Withdrawn</option>
            </select>
          </div>
        </div>

        {/* Content Viewport */}
        {loading ? (
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
            <LoadingSpinner size="lg" />
            <p className="text-sm font-medium text-slate-600">Retrieving applicant submissions...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-2xl border border-red-200 p-12 text-center space-y-4 shadow-sm">
            <ShieldAlert className="w-12 h-12 text-red-600 mx-auto" />
            <div className="space-y-1">
              <h3 className="text-lg font-bold text-navy-950">Access Restricted</h3>
              <p className="text-xs text-slate-600">{error}</p>
            </div>
            <Link to="/recruiter/dashboard">
              <Button variant="navy" size="sm">
                <span>Return to Recruiter Hub</span>
              </Button>
            </Link>
          </div>
        ) : applicantsList.length === 0 ? (
          /* Empty State */
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
            <div className="w-14 h-14 rounded-2xl bg-slate-100 text-slate-400 flex items-center justify-center mx-auto">
              <Users className="w-7 h-7" />
            </div>
            <h3 className="text-lg font-bold text-navy-950">No Candidates Found</h3>
            <p className="text-xs text-slate-500 max-w-sm">
              {statusFilter !== 'ALL'
                ? `No candidates currently match the "${statusFilter}" status filter.`
                : 'No candidates have applied to this position yet. Ensure the job status is set to PUBLISHED.'}
            </p>
          </div>
        ) : (
          /* Applicants List */
          <div className="space-y-4">
            {applicantsList.map((app) => (
              <div
                key={app.id}
                className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-sm space-y-4"
              >
                {/* Applicant Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-navy-900 text-white flex items-center justify-center font-bold text-sm">
                      {app.applicant?.fullName?.charAt(0) || 'C'}
                    </div>
                    <div>
                      <h4 className="font-bold text-navy-950 text-base">{app.applicant?.fullName}</h4>
                      <p className="text-xs text-slate-500 flex items-center gap-2">
                        <Mail className="w-3.5 h-3.5" />
                        <span>{app.applicant?.email}</span>
                        <span>•</span>
                        <span>Applied {formatDate(app.createdAt)}</span>
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span className="text-xs text-slate-400 font-semibold">Stage:</span>
                    <Badge status={app.status} size="md" />
                  </div>
                </div>

                {/* Candidate Content: Cover Letter & Attached Resume */}
                <div className="space-y-3 text-xs">
                  {/* Cover Letter */}
                  {app.coverLetter && (
                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/70 space-y-1">
                      <span className="font-bold text-slate-700 uppercase tracking-wider text-[10px]">
                        Candidate Note / Cover Letter
                      </span>
                      <p className="text-slate-700 whitespace-pre-line leading-relaxed">
                        {app.coverLetter}
                      </p>
                    </div>
                  )}

                  {/* Resume Download Action */}
                  <div className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-200/70">
                    <div className="flex items-center gap-2">
                      <FileText className="w-4 h-4 text-blue-600" />
                      <span className="font-semibold text-slate-800">
                        {app.resume ? app.resume.originalFilename : 'No resume document attached'}
                      </span>
                    </div>

                    {app.resume && (
                      <Button
                        type="button"
                        variant="secondary"
                        size="sm"
                        onClick={() => handleDownloadResume(app.resume.id, app.resume.originalFilename)}
                        loading={downloadingId === app.resume.id}
                      >
                        <Download className="w-3.5 h-3.5" />
                        <span>Download PDF</span>
                      </Button>
                    )}
                  </div>
                </div>

                {/* Recruiter Status Transition Controls */}
                <div className="pt-3 border-t border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <span className="text-[11px] text-slate-400 font-mono">
                    Application ID: #{app.id}
                  </span>

                  {app.status === 'WITHDRAWN' ? (
                    <span className="text-xs text-slate-400 italic">
                      Candidate has withdrawn this application. Status cannot be modified.
                    </span>
                  ) : (
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-xs font-semibold text-slate-600">Update Stage:</span>
                      <select
                        value={app.status}
                        onChange={(e) => handleStatusChange(app.id, e.target.value)}
                        disabled={updatingId === app.id}
                        className="rounded-xl border border-slate-300 bg-white text-slate-900 text-xs font-semibold px-2.5 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-100"
                      >
                        <option value="SUBMITTED">Submitted</option>
                        <option value="UNDER_REVIEW">Under Review</option>
                        <option value="SHORTLISTED">Shortlisted</option>
                        <option value="ACCEPTED">Accepted</option>
                        <option value="REJECTED">Rejected</option>
                      </select>
                    </div>
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
                  onClick={() => setPage((prev) => Math.max(0, prev - 1))}
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
                  onClick={() => setPage((prev) => prev + 1)}
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
    </div>
  );
}
