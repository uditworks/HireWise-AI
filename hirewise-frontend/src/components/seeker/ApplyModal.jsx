import React, { useState, useEffect } from 'react';
import { applicationsApi } from '../../api/applicationsApi';
import { resumesApi } from '../../api/resumesApi';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  X,
  Send,
  FileText,
  AlertCircle,
  CheckCircle2,
  Sparkles,
  Info
} from 'lucide-react';

export default function ApplyModal({ job, isOpen, onClose, onSuccess }) {
  const { user } = useAuth();
  const { showSuccess, showError } = useToast();

  const [coverLetter, setCoverLetter] = useState('');
  const [resumeId, setResumeId] = useState('');
  const [resumes, setResumes] = useState([]);
  const [loadingResumes, setLoadingResumes] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  // Fetch candidate's resumes on modal open
  useEffect(() => {
    if (isOpen) {
      setFormError('');
      setCoverLetter('');
      setResumeId('');
      setLoadingResumes(true);

      resumesApi
        .getMyResumes()
        .then((data) => {
          setResumes(Array.isArray(data) ? data : []);
          if (Array.isArray(data) && data.length > 0) {
            setResumeId(String(data[0].id));
          }
        })
        .catch(() => {
          // If no resumes or resume service is empty, proceed gracefully
          setResumes([]);
        })
        .finally(() => {
          setLoadingResumes(false);
        });
    }
  }, [isOpen]);

  if (!isOpen || !job) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    if (coverLetter.length > 5000) {
      setFormError('Cover letter must not exceed 5000 characters.');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        coverLetter: coverLetter.trim() || undefined,
        resumeId: resumeId ? Number(resumeId) : undefined
      };

      const response = await applicationsApi.applyToJob(job.id, payload);
      showSuccess(`Application submitted successfully for "${job.title}"!`);

      if (onSuccess) {
        onSuccess(response);
      }
      onClose();
    } catch (err) {
      let msg = 'Failed to submit application. Please try again.';
      if (err.response) {
        if (err.response.status === 409 || err.response.data?.message?.includes('already applied')) {
          msg = 'You have already submitted an application for this position.';
        } else if (err.response.data?.message) {
          msg = err.response.data.message;
        }
      } else if (err.message) {
        msg = err.message;
      }
      setFormError(msg);
      showError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="relative w-full max-w-lg bg-white rounded-2xl border border-slate-200 shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="px-6 py-5 bg-gradient-to-r from-navy-950 to-navy-900 text-white flex items-center justify-between">
          <div className="space-y-0.5">
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-blue-300 uppercase tracking-wider">Job Application</span>
            </div>
            <h3 className="text-lg font-bold text-white leading-tight truncate max-w-[340px]">
              {job.title}
            </h3>
          </div>
          <button
            onClick={onClose}
            disabled={submitting}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition-colors focus:outline-none"
            aria-label="Close modal"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content & Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* Candidate Info Badge */}
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/80 flex items-center justify-between text-xs">
            <div>
              <span className="text-slate-500">Applicant: </span>
              <strong className="text-slate-900">{user?.fullName}</strong>
            </div>
            <span className="text-slate-400 font-mono text-[11px]">{user?.email}</span>
          </div>

          {/* Form Error Banner */}
          {formError && (
            <div className="p-3.5 bg-red-50 border border-red-200 rounded-xl text-xs text-red-800 flex items-start gap-2.5">
              <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Submission Conflict</p>
                <p className="mt-0.5">{formError}</p>
              </div>
            </div>
          )}

          {/* Resume Selection */}
          <div className="space-y-1.5">
            <label htmlFor="resume-select" className="text-xs font-semibold text-slate-700 block">
              Attached Resume (Optional)
            </label>
            {loadingResumes ? (
              <div className="py-2 flex items-center gap-2 text-xs text-slate-500">
                <LoadingSpinner size="sm" />
                <span>Checking uploaded resumes...</span>
              </div>
            ) : resumes.length > 0 ? (
              <select
                id="resume-select"
                value={resumeId}
                onChange={(e) => setResumeId(e.target.value)}
                className="w-full rounded-xl border border-slate-300 bg-white text-slate-900 text-sm px-3.5 py-2.5 transition-all focus:outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-600"
              >
                <option value="">No Resume (Submit without attachment)</option>
                {resumes.map((r) => (
                  <option key={r.id} value={r.id}>
                    📄 {r.originalFilename} (Uploaded {new Date(r.createdAt).toLocaleDateString()})
                  </option>
                ))}
              </select>
            ) : (
              <div className="p-3 bg-blue-50/70 border border-blue-200/70 rounded-xl text-xs text-blue-900 flex items-start gap-2">
                <Info className="w-4 h-4 text-blue-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold">No resumes uploaded yet</p>
                  <p className="text-[11px] text-blue-800 mt-0.5">
                    You can submit your application now with a cover letter. PDF resume uploads and AI scoring will be available in Stage 7.
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Cover Letter Field */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between text-xs font-semibold text-slate-700">
              <label htmlFor="cover-letter">Cover Letter / Note to Recruiter (Optional)</label>
              <span className={`text-[11px] ${coverLetter.length > 4500 ? 'text-amber-600 font-bold' : 'text-slate-400'}`}>
                {coverLetter.length} / 5000
              </span>
            </div>
            <textarea
              id="cover-letter"
              rows={4}
              maxLength={5000}
              placeholder="Introduce yourself, highlight your relevant experience, and share why you are a great fit for this role..."
              value={coverLetter}
              onChange={(e) => setCoverLetter(e.target.value)}
              className="w-full rounded-xl border border-slate-300 bg-white text-slate-900 text-sm p-3.5 transition-all focus:outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-600 resize-none"
            />
          </div>

          {/* Action Buttons */}
          <div className="pt-3 flex items-center justify-end gap-3 border-t border-slate-100">
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={submitting}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="navy"
              loading={submitting}
            >
              <Send className="w-4 h-4" />
              <span>Submit Application</span>
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
