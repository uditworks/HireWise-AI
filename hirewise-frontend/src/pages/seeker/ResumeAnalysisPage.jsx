import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { resumesApi } from '../../api/resumesApi';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  ArrowLeft,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
  Lightbulb,
  FileText,
  Target,
  ShieldAlert,
  Info,
  Clock,
  ArrowRight
} from 'lucide-react';

export default function ResumeAnalysisPage() {
  const { id } = useParams();

  const [analysis, setAnalysis] = useState(null);
  const [resumeMeta, setResumeMeta] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAnalysis = async () => {
      setLoading(true);
      setError(null);
      try {
        const [analysisData, resumeData] = await Promise.all([
          resumesApi.analyzeResume(id),
          resumesApi.getResumeById(id).catch(() => null)
        ]);
        setAnalysis(analysisData);
        setResumeMeta(resumeData);
      } catch (err) {
        setError(err.message || 'Could not perform AI resume analysis.');
      } finally {
        setLoading(false);
      }
    };

    fetchAnalysis();
  }, [id]);

  const formatDate = (isoString) => {
    if (!isoString) return '';
    try {
      const date = new Date(isoString);
      return date.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return '';
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
          <span className="text-xs text-slate-400 font-mono">Resume Reference #{id}</span>
        </div>

        {/* Loading State */}
        {loading ? (
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
            <LoadingSpinner size="lg" />
            <p className="text-sm font-medium text-slate-600">Extracting text & running deterministic AI analysis...</p>
          </div>
        ) : error ? (
          <div className="bg-white rounded-2xl border border-red-200 p-12 text-center space-y-4 shadow-sm">
            <ShieldAlert className="w-12 h-12 text-red-600 mx-auto" />
            <div className="space-y-1">
              <h3 className="text-lg font-bold text-navy-950">Analysis Unavailable</h3>
              <p className="text-xs text-slate-600">{error}</p>
            </div>
            <Link to="/seeker/resumes">
              <Button variant="navy" size="sm">
                <span>Return to My Resumes</span>
              </Button>
            </Link>
          </div>
        ) : (
          /* Analysis Results View */
          <div className="space-y-6">
            {/* Header Card */}
            <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="space-y-1">
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 text-blue-700 text-xs font-semibold uppercase tracking-wider mb-1">
                  <Sparkles className="w-3.5 h-3.5 text-blue-600" />
                  <span>Explainable AI Analysis</span>
                </div>
                <h1 className="text-2xl sm:text-3xl font-extrabold text-navy-950 tracking-tight">
                  {resumeMeta?.originalFilename || `Resume #${id}`} Analysis
                </h1>
                {analysis?.analyzedAt && (
                  <p className="text-xs text-slate-400 flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5" />
                    <span>Analyzed on {formatDate(analysis.analyzedAt)}</span>
                  </p>
                )}
              </div>

              {/* Action: Match Against Jobs */}
              <Link to={`/seeker/resumes/${id}/match`}>
                <Button variant="navy" size="sm">
                  <Target className="w-4 h-4" />
                  <span>Match Against Open Jobs</span>
                  <ArrowRight className="w-4 h-4" />
                </Button>
              </Link>
            </div>

            {/* Profile Summary Card */}
            {analysis?.summary && (
              <div className="bg-white p-6 sm:p-7 rounded-2xl border border-slate-200 shadow-sm space-y-2">
                <h3 className="text-sm font-bold text-navy-950 flex items-center gap-2">
                  <FileText className="w-4 h-4 text-blue-600" />
                  <span>Extracted Profile Summary</span>
                </h3>
                <p className="text-xs sm:text-sm text-slate-700 leading-relaxed bg-slate-50/70 p-4 rounded-xl border border-slate-100">
                  {analysis.summary}
                </p>
              </div>
            )}

            {/* Detected Skills Cloud */}
            {analysis?.detectedSkills && analysis.detectedSkills.size > 0 && (
              <div className="bg-white p-6 sm:p-7 rounded-2xl border border-slate-200 shadow-sm space-y-3">
                <h3 className="text-sm font-bold text-navy-950 flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-blue-600" />
                  <span>Extracted Technical Competencies & Skills ({Array.from(analysis.detectedSkills).length})</span>
                </h3>
                <div className="flex flex-wrap gap-2 pt-1">
                  {Array.from(analysis.detectedSkills).map((skill, idx) => (
                    <span
                      key={idx}
                      className="px-3 py-1.5 rounded-xl bg-blue-50 text-blue-800 text-xs font-semibold border border-blue-200/80"
                    >
                      {skill}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Strengths and Gaps Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Identified Strengths */}
              {analysis?.strengths && analysis.strengths.length > 0 && (
                <div className="bg-white p-6 rounded-2xl border border-emerald-100 shadow-sm space-y-3">
                  <div className="flex items-center gap-2 text-emerald-800 font-bold text-sm">
                    <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                    <span>Candidate Strengths</span>
                  </div>
                  <ul className="space-y-2 text-xs text-slate-700">
                    {analysis.strengths.map((item, idx) => (
                      <li key={idx} className="flex items-start gap-2 bg-emerald-50/50 p-2.5 rounded-xl border border-emerald-100">
                        <span className="text-emerald-600 font-bold">•</span>
                        <span>{item}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Identified Gaps / Areas to Strengthen */}
              {analysis?.gaps && analysis.gaps.length > 0 && (
                <div className="bg-white p-6 rounded-2xl border border-amber-100 shadow-sm space-y-3">
                  <div className="flex items-center gap-2 text-amber-800 font-bold text-sm">
                    <AlertTriangle className="w-4 h-4 text-amber-600" />
                    <span>Areas for Potential Growth</span>
                  </div>
                  <ul className="space-y-2 text-xs text-slate-700">
                    {analysis.gaps.map((item, idx) => (
                      <li key={idx} className="flex items-start gap-2 bg-amber-50/50 p-2.5 rounded-xl border border-amber-100">
                        <span className="text-amber-600 font-bold">•</span>
                        <span>{item}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>

            {/* Improvement Recommendations */}
            {analysis?.improvementSuggestions && analysis.improvementSuggestions.length > 0 && (
              <div className="bg-white p-6 sm:p-7 rounded-2xl border border-indigo-100 shadow-sm space-y-3">
                <div className="flex items-center gap-2 text-indigo-900 font-bold text-sm">
                  <Lightbulb className="w-4 h-4 text-indigo-600" />
                  <span>Actionable Recommendations for Candidates</span>
                </div>
                <ul className="space-y-2 text-xs text-slate-700">
                  {analysis.improvementSuggestions.map((suggestion, idx) => (
                    <li key={idx} className="flex items-start gap-2.5 bg-indigo-50/40 p-3 rounded-xl border border-indigo-100">
                      <span className="text-indigo-600 font-extrabold">{idx + 1}.</span>
                      <span className="leading-relaxed">{suggestion}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Official Advisory Disclaimer */}
            <div className="p-4 rounded-xl bg-slate-100 border border-slate-200 text-xs text-slate-500 flex items-start gap-2.5">
              <Info className="w-4 h-4 text-slate-400 flex-shrink-0 mt-0.5" />
              <p className="leading-relaxed">
                <strong className="font-semibold text-slate-700">Advisory Notice: </strong>
                {analysis?.disclaimer ||
                  'AI-generated resume analysis is an advisory estimate for candidate improvement and does not guarantee employment.'}
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
