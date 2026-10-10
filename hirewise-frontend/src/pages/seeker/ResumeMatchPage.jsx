import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { resumesApi } from '../../api/resumesApi';
import { jobsApi } from '../../api/jobsApi';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  ArrowLeft,
  Target,
  Sparkles,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Briefcase,
  FileText,
  Percent,
  BarChart3,
  Send,
  Info
} from 'lucide-react';

export default function ResumeMatchPage() {
  const { id } = useParams();

  const [resumes, setResumes] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState(id || '');
  const [selectedJobId, setSelectedJobId] = useState('');

  const [matchResult, setMatchResult] = useState(null);
  const [loadingInitial, setLoadingInitial] = useState(true);
  const [matching, setMatching] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadPrerequisites = async () => {
      setLoadingInitial(true);
      try {
        const [resumesList, jobsPage] = await Promise.all([
          resumesApi.getMyResumes(),
          jobsApi.getPublishedJobs({ size: 100 })
        ]);

        const validResumes = Array.isArray(resumesList) ? resumesList : [];
        setResumes(validResumes);

        if (!selectedResumeId && validResumes.length > 0) {
          setSelectedResumeId(String(validResumes[0].id));
        }

        const publishedJobs = jobsPage?.content || [];
        setJobs(publishedJobs);
        if (publishedJobs.length > 0) {
          setSelectedJobId(String(publishedJobs[0].id));
        }
      } catch (err) {
        setError('Could not load resumes or open job postings for matching.');
      } finally {
        setLoadingInitial(false);
      }
    };

    loadPrerequisites();
  }, [id]);

  const handleRunMatch = async (e) => {
    if (e) e.preventDefault();
    if (!selectedResumeId || !selectedJobId) return;

    setMatching(true);
    setError(null);
    try {
      const data = await resumesApi.matchResumeToJob(selectedResumeId, selectedJobId);
      setMatchResult(data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to calculate job compatibility match.');
    } finally {
      setMatching(false);
    }
  };

  return (
    <div className="flex-1 bg-slate-50 py-8 sm:py-12">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
        {/* Back Link */}
        <div className="flex items-center justify-between">
          <Link
            to="/seeker/resumes"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-blue-600 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to My Resumes</span>
          </Link>
          <span className="text-xs text-slate-400 font-mono">Deterministic Match Engine</span>
        </div>

        {/* Page Header */}
        <div className="space-y-1">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 text-blue-700 text-xs font-semibold uppercase tracking-wider mb-1">
            <Target className="w-3.5 h-3.5 text-blue-600" />
            <span>AI Match Engine</span>
          </div>
          <h1 className="text-3xl font-extrabold text-navy-950 tracking-tight">
            Job Compatibility & Skill Matching
          </h1>
          <p className="text-sm text-slate-500">
            Compare your uploaded resume against published job requirements to view fit scores and missing skills.
          </p>
        </div>

        {/* Loading Initial States */}
        {loadingInitial ? (
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
            <LoadingSpinner size="lg" />
            <p className="text-sm font-medium text-slate-600">Loading your profile & open job postings...</p>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Match Selector Card */}
            <div className="bg-white p-6 sm:p-7 rounded-2xl border border-slate-200 shadow-sm space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Select Resume */}
                <div className="space-y-1.5">
                  <label htmlFor="select-resume" className="text-xs font-semibold text-slate-700 block">
                    Choose Your Resume
                  </label>
                  {resumes.length === 0 ? (
                    <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-800">
                      No uploaded resumes found. Please upload a resume first.
                    </div>
                  ) : (
                    <select
                      id="select-resume"
                      value={selectedResumeId}
                      onChange={(e) => setSelectedResumeId(e.target.value)}
                      className="w-full rounded-xl border border-slate-300 bg-white text-slate-900 text-sm px-3.5 py-2.5 transition-all focus:outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-600"
                    >
                      {resumes.map((r) => (
                        <option key={r.id} value={r.id}>
                          📄 {r.originalFilename} (#{r.id})
                        </option>
                      ))}
                    </select>
                  )}
                </div>

                {/* Select Job */}
                <div className="space-y-1.5">
                  <label htmlFor="select-job" className="text-xs font-semibold text-slate-700 block">
                    Choose Target Job Opportunity
                  </label>
                  {jobs.length === 0 ? (
                    <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-600">
                      No published jobs available.
                    </div>
                  ) : (
                    <select
                      id="select-job"
                      value={selectedJobId}
                      onChange={(e) => setSelectedJobId(e.target.value)}
                      className="w-full rounded-xl border border-slate-300 bg-white text-slate-900 text-sm px-3.5 py-2.5 transition-all focus:outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-600"
                    >
                      {jobs.map((j) => (
                        <option key={j.id} value={j.id}>
                          💼 {j.title} ({j.location})
                        </option>
                      ))}
                    </select>
                  )}
                </div>
              </div>

              {/* Match Action Button */}
              <div className="flex justify-end pt-2">
                <Button
                  type="button"
                  variant="navy"
                  size="md"
                  onClick={handleRunMatch}
                  disabled={!selectedResumeId || !selectedJobId || matching}
                  loading={matching}
                >
                  <Sparkles className="w-4 h-4" />
                  <span>Calculate Compatibility Fit</span>
                </Button>
              </div>
            </div>

            {/* Error Banner */}
            {error && (
              <div className="p-4 rounded-xl bg-red-50 border border-red-200 flex items-start gap-3 text-red-800 text-sm animate-in fade-in">
                <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                <p className="text-xs">{error}</p>
              </div>
            )}

            {/* Results Display */}
            {matchResult && (
              <div className="space-y-6 animate-in fade-in zoom-in-95 duration-200">
                {/* Top Score Cards Grid */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  {/* Overall Match Score */}
                  <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex flex-col justify-between space-y-2">
                    <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                      Overall Match Score
                    </span>
                    <div className="flex items-baseline gap-2">
                      <span className="text-4xl font-black text-navy-950">
                        {matchResult.overallMatchScore}%
                      </span>
                      {matchResult.fitCategory && (
                        <span className="text-xs font-bold text-blue-600 capitalize">
                          {matchResult.fitCategory.replace(/_/g, ' ')}
                        </span>
                      )}
                    </div>
                    {/* Progress Bar */}
                    <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                      <div
                        className="bg-gradient-to-r from-blue-600 to-indigo-600 h-full rounded-full transition-all duration-500"
                        style={{ width: `${Math.min(100, Math.max(0, matchResult.overallMatchScore))}%` }}
                      ></div>
                    </div>
                  </div>

                  {/* Skill Match Score */}
                  <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex flex-col justify-between space-y-2">
                    <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                      Skills Match
                    </span>
                    <div className="flex items-baseline gap-2">
                      <span className="text-4xl font-black text-emerald-600">
                        {matchResult.skillMatchScore}%
                      </span>
                      <span className="text-xs text-slate-400">Direct tech match</span>
                    </div>
                    <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                      <div
                        className="bg-emerald-500 h-full rounded-full transition-all duration-500"
                        style={{ width: `${Math.min(100, Math.max(0, matchResult.skillMatchScore))}%` }}
                      ></div>
                    </div>
                  </div>

                  {/* Experience Match Score */}
                  <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex flex-col justify-between space-y-2">
                    <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                      Experience Fit
                    </span>
                    <div className="flex items-baseline gap-2">
                      <span className="text-4xl font-black text-indigo-600">
                        {matchResult.experienceMatchScore}%
                      </span>
                      <span className="text-xs text-slate-400">Seniority alignment</span>
                    </div>
                    <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                      <div
                        className="bg-indigo-500 h-full rounded-full transition-all duration-500"
                        style={{ width: `${Math.min(100, Math.max(0, matchResult.experienceMatchScore))}%` }}
                      ></div>
                    </div>
                  </div>
                </div>

                {/* Skills Breakdown Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Matched Skills */}
                  <div className="bg-white p-6 rounded-2xl border border-emerald-200/80 shadow-sm space-y-3">
                    <div className="flex items-center gap-2 text-emerald-800 font-bold text-sm">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                      <span>Matched Competencies ({matchResult.matchedSkills?.length || 0})</span>
                    </div>
                    {matchResult.matchedSkills && matchResult.matchedSkills.length > 0 ? (
                      <div className="flex flex-wrap gap-2">
                        {matchResult.matchedSkills.map((skill, idx) => (
                          <span
                            key={idx}
                            className="px-3 py-1 rounded-xl bg-emerald-50 text-emerald-800 text-xs font-semibold border border-emerald-200"
                          >
                            ✓ {skill}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <p className="text-xs text-slate-500 italic">No exact matching skills detected for this position.</p>
                    )}
                  </div>

                  {/* Missing Skills */}
                  <div className="bg-white p-6 rounded-2xl border border-amber-200/80 shadow-sm space-y-3">
                    <div className="flex items-center gap-2 text-amber-800 font-bold text-sm">
                      <XCircle className="w-4 h-4 text-amber-600" />
                      <span>Missing / Recommended Skills ({matchResult.missingSkills?.length || 0})</span>
                    </div>
                    {matchResult.missingSkills && matchResult.missingSkills.length > 0 ? (
                      <div className="flex flex-wrap gap-2">
                        {matchResult.missingSkills.map((skill, idx) => (
                          <span
                            key={idx}
                            className="px-3 py-1 rounded-xl bg-amber-50 text-amber-800 text-xs font-semibold border border-amber-200"
                          >
                            + {skill}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <p className="text-xs text-emerald-600 font-semibold">
                        🎉 Great fit! No key skills missing for this position.
                      </p>
                    )}
                  </div>
                </div>

                {/* Deterministic Explanation */}
                {matchResult.explanation && (
                  <div className="bg-white p-6 sm:p-7 rounded-2xl border border-slate-200 shadow-sm space-y-2">
                    <h4 className="text-sm font-bold text-navy-950 flex items-center gap-2">
                      <Sparkles className="w-4 h-4 text-blue-600" />
                      <span>AI Compatibility Reasoning</span>
                    </h4>
                    <p className="text-xs sm:text-sm text-slate-700 leading-relaxed bg-slate-50/70 p-4 rounded-xl border border-slate-100">
                      {matchResult.explanation}
                    </p>
                  </div>
                )}

                {/* Apply Directly Link */}
                <div className="bg-white p-6 rounded-2xl border border-blue-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <h4 className="text-base font-bold text-navy-950">Ready to apply for this role?</h4>
                    <p className="text-xs text-slate-500">
                      Submit your application directly to the hiring team with your analyzed resume attached.
                    </p>
                  </div>
                  <Link to={`/jobs/${selectedJobId}`}>
                    <Button variant="navy" size="md">
                      <Send className="w-4 h-4" />
                      <span>Go to Job Posting</span>
                    </Button>
                  </Link>
                </div>

                {/* Disclaimer */}
                <div className="p-4 rounded-xl bg-slate-100 border border-slate-200 text-xs text-slate-500 flex items-start gap-2.5">
                  <Info className="w-4 h-4 text-slate-400 flex-shrink-0 mt-0.5" />
                  <p className="leading-relaxed">
                    <strong className="font-semibold text-slate-700">Disclaimer: </strong>
                    {matchResult.disclaimer ||
                      'Fit scores are algorithmic estimates based on extracted keywords and do not guarantee hiring outcomes.'}
                  </p>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
