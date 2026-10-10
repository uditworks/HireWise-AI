import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link, useNavigate, useLocation } from 'react-router-dom';
import { jobsApi } from '../../api/jobsApi';
import { applicationsApi } from '../../api/applicationsApi';
import { useAuth } from '../../context/AuthContext';
import { EMPLOYMENT_TYPE_LABELS } from '../../utils/constants';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ApplyModal from '../../components/seeker/ApplyModal';
import {
  ArrowLeft,
  MapPin,
  Briefcase,
  DollarSign,
  UserCheck,
  Sparkles,
  AlertCircle,
  CheckCircle2,
  Lock,
  Send,
  ShieldAlert,
  Clock
} from 'lucide-react';

export default function JobDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isAuthenticated, isSeeker, isRecruiter } = useAuth();

  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isApplyModalOpen, setIsApplyModalOpen] = useState(false);
  const [existingApplication, setExistingApplication] = useState(null);

  const fetchJobDetails = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await jobsApi.getJobById(id);
      setJob(data);

      // If authenticated job seeker, check if already applied to this specific job
      if (isAuthenticated && isSeeker) {
        try {
          const appsData = await applicationsApi.getMyApplications({ size: 100 });
          const myApp = appsData?.content?.find((app) => app.job?.id === Number(id));
          if (myApp) {
            setExistingApplication(myApp);
          }
        } catch {
          // Ignore secondary check errors
        }
      }
    } catch (err) {
      if (err.response?.status === 404) {
        setError('This job posting either does not exist, has been unpublished, or is not accessible.');
      } else {
        setError(err.message || 'Could not retrieve job posting details.');
      }
    } finally {
      setLoading(false);
    }
  }, [id, isAuthenticated, isSeeker]);

  useEffect(() => {
    fetchJobDetails();
  }, [fetchJobDetails]);

  const formatDate = (isoString) => {
    if (!isoString) return 'Recently';
    try {
      const date = new Date(isoString);
      return date.toLocaleDateString(undefined, {
        month: 'long',
        day: 'numeric',
        year: 'numeric'
      });
    } catch {
      return '';
    }
  };

  const handleApplyClick = () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: location } });
      return;
    }

    if (isSeeker) {
      setIsApplyModalOpen(true);
    }
  };

  const handleApplicationSuccess = (appResponse) => {
    setExistingApplication(appResponse);
  };

  const isJobOwner = isRecruiter && job?.recruiter?.email?.toLowerCase() === user?.email?.toLowerCase();

  return (
    <div className="flex-1 bg-slate-50 py-8 sm:py-12">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
        {/* Breadcrumb / Back Link */}
        <div className="flex items-center justify-between">
          <Link
            to="/jobs"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-blue-600 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to All Jobs</span>
          </Link>
          <span className="text-xs text-slate-400 font-mono">Job Reference #{id}</span>
        </div>

        {/* Loading State */}
        {loading ? (
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
            <LoadingSpinner size="lg" />
            <p className="text-sm font-medium text-slate-600">Loading position details...</p>
          </div>
        ) : error ? (
          /* Not Found / Error State */
          <div className="bg-white rounded-2xl border border-red-200 p-12 text-center space-y-4 shadow-sm">
            <div className="w-14 h-14 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center mx-auto">
              <AlertCircle className="w-7 h-7" />
            </div>
            <div className="max-w-md mx-auto space-y-1">
              <h2 className="text-xl font-bold text-navy-950">Position Not Found</h2>
              <p className="text-xs text-slate-600 leading-relaxed">{error}</p>
            </div>
            <div className="pt-2">
              <Link to="/jobs">
                <Button variant="navy" size="sm">
                  <ArrowLeft className="w-4 h-4" />
                  <span>Return to Opportunities</span>
                </Button>
              </Link>
            </div>
          </div>
        ) : (
          /* Job Details Content */
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left 2 Columns: Main Details */}
            <div className="lg:col-span-2 space-y-6">
              {/* Main Card */}
              <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/90 shadow-sm space-y-6">
                {/* Header Info */}
                <div className="space-y-3 pb-6 border-b border-slate-100">
                  <div className="flex flex-wrap items-center gap-2">
                    <Badge
                      status={job.employmentType}
                      label={EMPLOYMENT_TYPE_LABELS[job.employmentType] || job.employmentType}
                      size="sm"
                    />
                    <Badge status={job.status} size="sm" />
                  </div>

                  <h1 className="text-2xl sm:text-3xl font-extrabold text-navy-950 tracking-tight leading-tight">
                    {job.title}
                  </h1>

                  <div className="flex flex-wrap items-center gap-4 text-xs text-slate-600 pt-1">
                    {job.location && (
                      <span className="flex items-center gap-1.5 font-medium">
                        <MapPin className="w-4 h-4 text-blue-600" />
                        <span>{job.location}</span>
                      </span>
                    )}
                    {job.salaryRange && (
                      <span className="flex items-center gap-1.5 font-semibold text-emerald-700">
                        <DollarSign className="w-4 h-4 text-emerald-600" />
                        <span>{job.salaryRange}</span>
                      </span>
                    )}
                    {job.createdAt && (
                      <span className="flex items-center gap-1.5 text-slate-500">
                        <Clock className="w-4 h-4 text-slate-400" />
                        <span>Posted on {formatDate(job.createdAt)}</span>
                      </span>
                    )}
                  </div>
                </div>

                {/* Job Description */}
                <div className="space-y-3">
                  <h3 className="text-base font-bold text-navy-950 flex items-center gap-2">
                    <Briefcase className="w-4 h-4 text-blue-600" />
                    <span>Role Description</span>
                  </h3>
                  <div className="text-sm text-slate-700 leading-relaxed whitespace-pre-line space-y-3 bg-slate-50/50 p-4 rounded-xl border border-slate-100">
                    {job.description}
                  </div>
                </div>

                {/* Required Skills */}
                {job.requiredSkills && job.requiredSkills.length > 0 && (
                  <div className="space-y-3 pt-2">
                    <h3 className="text-base font-bold text-navy-950 flex items-center gap-2">
                      <Sparkles className="w-4 h-4 text-blue-600" />
                      <span>Required Skills & Tech Stack</span>
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {Array.from(job.requiredSkills).map((skill, idx) => (
                        <span
                          key={idx}
                          className="px-3 py-1.5 rounded-xl bg-blue-50 text-blue-800 text-xs font-semibold border border-blue-200/70"
                        >
                          {skill}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Right Column: Application & Recruiter Meta Card */}
            <div className="space-y-6">
              {/* Application Action Card */}
              <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
                <h3 className="text-base font-bold text-navy-950">Application Status</h3>

                {/* State: Guest (Not Authenticated) */}
                {!isAuthenticated && (
                  <div className="space-y-3">
                    <p className="text-xs text-slate-600 leading-relaxed">
                      Sign in or create a free Job Seeker account to apply for this position and track your application status.
                    </p>
                    <Button
                      onClick={handleApplyClick}
                      variant="navy"
                      className="w-full"
                    >
                      <Lock className="w-4 h-4" />
                      <span>Sign In to Apply</span>
                    </Button>
                    <div className="text-center pt-1">
                      <Link
                        to="/register"
                        className="text-xs font-semibold text-blue-600 hover:text-blue-700 hover:underline"
                      >
                        New candidate? Create account
                      </Link>
                    </div>
                  </div>
                )}

                {/* State: Authenticated Job Seeker */}
                {isAuthenticated && isSeeker && (
                  <div className="space-y-3">
                    {existingApplication ? (
                      <div className="p-4 bg-emerald-50 rounded-xl border border-emerald-200 space-y-3">
                        <div className="flex items-center gap-2 text-emerald-800 font-bold text-xs">
                          <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                          <span>Application Submitted</span>
                        </div>
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-slate-600">Current Status:</span>
                          <Badge status={existingApplication.status} size="sm" />
                        </div>
                        <Link to="/seeker/applications" className="block pt-1">
                          <Button variant="outline" size="sm" className="w-full">
                            <span>View in My Applications</span>
                          </Button>
                        </Link>
                      </div>
                    ) : (
                      <>
                        <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs text-slate-700 flex items-start gap-2">
                          <CheckCircle2 className="w-4 h-4 text-blue-600 flex-shrink-0 mt-0.5" />
                          <div>
                            <p className="font-semibold text-slate-900">Verified Candidate</p>
                            <p className="text-[11px] text-slate-500 mt-0.5">
                              Applying as <strong>{user?.fullName}</strong>
                            </p>
                          </div>
                        </div>
                        <Button
                          onClick={handleApplyClick}
                          variant="primary"
                          className="w-full"
                        >
                          <Send className="w-4 h-4" />
                          <span>Apply for this Position</span>
                        </Button>
                      </>
                    )}
                  </div>
                )}

                {/* State: Authenticated Recruiter */}
                {isAuthenticated && isRecruiter && (
                  <div className="space-y-3">
                    {isJobOwner ? (
                      <div className="p-3 bg-purple-50 rounded-xl border border-purple-200 text-xs text-purple-900 space-y-2">
                        <p className="font-semibold flex items-center gap-1.5">
                          <UserCheck className="w-4 h-4 text-purple-700" />
                          <span>You Posted This Job</span>
                        </p>
                        <p className="text-[11px] text-purple-800">
                          This job posting is managed by your recruiter account.
                        </p>
                        <Link to="/recruiter/dashboard" className="block pt-1">
                          <Button variant="outline" size="sm" className="w-full">
                            <span>Open Recruiter Hub</span>
                          </Button>
                        </Link>
                      </div>
                    ) : (
                      <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs text-slate-600 space-y-1">
                        <p className="font-semibold text-slate-800 flex items-center gap-1">
                          <ShieldAlert className="w-4 h-4 text-slate-500" />
                          <span>Recruiter Account</span>
                        </p>
                        <p className="text-[11px]">
                          Applications can only be submitted by registered Job Seekers.
                        </p>
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Recruiter Details Card */}
              {job.recruiter && (
                <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3">
                  <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                    Hiring Contact
                  </h4>
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-navy-900 text-white flex items-center justify-center font-bold text-sm">
                      {job.recruiter.fullName?.charAt(0) || 'R'}
                    </div>
                    <div>
                      <h5 className="text-sm font-bold text-navy-950">{job.recruiter.fullName}</h5>
                      <p className="text-xs text-slate-500">{job.recruiter.email}</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Apply Modal */}
      <ApplyModal
        job={job}
        isOpen={isApplyModalOpen}
        onClose={() => setIsApplyModalOpen(false)}
        onSuccess={handleApplicationSuccess}
      />
    </div>
  );
}
