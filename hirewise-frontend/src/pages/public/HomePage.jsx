import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { authApi } from '../../api/authApi';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import {
  Sparkles,
  Search,
  FileText,
  ShieldCheck,
  ArrowRight,
  Activity,
  BarChart3
} from 'lucide-react';

export default function HomePage() {
  const { isAuthenticated, isRecruiter, user } = useAuth();
  const [backendStatus, setBackendStatus] = useState({
    loading: true,
    connected: false,
    message: 'Checking Spring Boot connection...'
  });

  useEffect(() => {
    authApi
      .checkHealth()
      .then((data) => {
        setBackendStatus({
          loading: false,
          connected: true,
          message: `${data.status} - ${data.message || 'Connected to Spring Boot'}`
        });
      })
      .catch((err) => {
        setBackendStatus({
          loading: false,
          connected: false,
          message: err.message || 'Could not connect to Spring Boot backend at port 8080.'
        });
      });
  }, []);

  return (
    <div className="flex-1 flex flex-col">
      {/* Top Backend Status Banner */}
      <div className="bg-navy-900 text-white text-xs py-2 px-4 border-b border-navy-800">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="flex h-2 w-2 relative">
              <span
                className={`animate-ping absolute inline-flex h-full w-full rounded-full ${
                  backendStatus.connected ? 'bg-emerald-400 opacity-75' : 'bg-amber-400 opacity-75'
                }`}
              ></span>
              <span
                className={`relative inline-flex rounded-full h-2 w-2 ${
                  backendStatus.connected ? 'bg-emerald-500' : 'bg-amber-500'
                }`}
              ></span>
            </span>
            <span>
              Backend Proxy Status:{' '}
              <strong className="font-semibold">
                {backendStatus.loading ? 'Connecting...' : backendStatus.message}
              </strong>
            </span>
          </div>
          <div className="hidden sm:flex items-center gap-4 text-slate-300">
            <span>Spring Boot 4.x (Port 8080)</span>
            <span>•</span>
            <span>JWT Bearer Auth</span>
            <span>•</span>
            <span>MySQL 8.0</span>
          </div>
        </div>
      </div>

      {/* Hero Section */}
      <section className="relative overflow-hidden py-16 sm:py-24 bg-gradient-to-b from-white via-slate-50 to-slate-100 border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto">
            {/* Stage / Brand Pill */}
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-blue-50 border border-blue-200 text-blue-700 text-xs font-semibold uppercase tracking-wider mb-6">
              <Sparkles className="w-3.5 h-3.5 text-blue-600" />
              <span>Next-Generation Intelligent Hiring Platform</span>
            </div>

            {/* Headline */}
            <h1 className="text-4xl sm:text-6xl font-extrabold text-navy-950 tracking-tight leading-[1.12]">
              Smarter Hiring, Powered by{' '}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-700 via-blue-600 to-indigo-600">
                Explainable AI
              </span>
            </h1>

            {/* Subtitle */}
            <p className="mt-6 text-base sm:text-lg text-slate-600 leading-relaxed max-w-2xl mx-auto">
              HireWise AI connects top candidates with leading employers using deterministic resume parsing,
              transparent fit scores, and a rock-solid Spring Boot architecture.
            </p>

            {/* CTA Buttons */}
            <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3.5">
              {isAuthenticated ? (
                <Link to={isRecruiter ? '/recruiter/dashboard' : '/seeker/dashboard'}>
                  <Button variant="navy" size="lg" className="w-full sm:w-auto">
                    <span>Go to My Dashboard ({user?.role})</span>
                    <ArrowRight className="w-4 h-4" />
                  </Button>
                </Link>
              ) : (
                <>
                  <Link to="/register">
                    <Button variant="navy" size="lg" className="w-full sm:w-auto">
                      <span>Get Started as Job Seeker</span>
                      <ArrowRight className="w-4 h-4" />
                    </Button>
                  </Link>
                  <Link to="/login">
                    <Button variant="outline" size="lg" className="w-full sm:w-auto">
                      <span>Sign In to Account</span>
                    </Button>
                  </Link>
                </>
              )}
              <Link to="/jobs">
                <Button variant="secondary" size="lg" className="w-full sm:w-auto">
                  <Search className="w-4 h-4 text-slate-500" />
                  <span>Browse Jobs</span>
                </Button>
              </Link>
            </div>

            {/* Live API Health Card */}
            <div className="mt-10 p-4 max-w-md mx-auto bg-white rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between text-left">
              <div className="flex items-center gap-3">
                <div
                  className={`w-9 h-9 rounded-xl flex items-center justify-center ${
                    backendStatus.connected
                      ? 'bg-emerald-50 text-emerald-600 border border-emerald-200'
                      : 'bg-amber-50 text-amber-600 border border-amber-200'
                  }`}
                >
                  <Activity className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-slate-900">Backend API Status</h4>
                  <p className="text-[11px] text-slate-500 font-mono mt-0.5 truncate max-w-[220px]">
                    {backendStatus.loading ? 'Pinging /api/health...' : backendStatus.message}
                  </p>
                </div>
              </div>
              <Badge
                status={backendStatus.connected ? 'ACCEPTED' : 'UNDER_REVIEW'}
                label={backendStatus.connected ? 'Connected' : 'Connecting'}
                size="sm"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Feature Highlights Grid */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-12">
            <h2 className="text-2xl sm:text-3xl font-bold text-navy-950">
              Engineered for Precision & Transparency
            </h2>
            <p className="text-slate-600 text-sm mt-2">
              Discover how HireWise AI streamlines the application and evaluation lifecycle.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Feature 1 */}
            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-3 hover:shadow-md transition-shadow">
              <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center">
                <FileText className="w-5 h-5" />
              </div>
              <h3 className="text-lg font-bold text-navy-950">Deterministic Resume Analysis</h3>
              <p className="text-slate-600 text-xs leading-relaxed">
                Extract skills, work history, and education from PDF resumes with repeatable, verifiable algorithms.
              </p>
            </div>

            {/* Feature 2 */}
            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-3 hover:shadow-md transition-shadow">
              <div className="w-10 h-10 rounded-xl bg-indigo-100 text-indigo-700 flex items-center justify-center">
                <BarChart3 className="w-5 h-5" />
              </div>
              <h3 className="text-lg font-bold text-navy-950">Explainable Match Scores</h3>
              <p className="text-slate-600 text-xs leading-relaxed">
                Receive clear breakdowns of matched competencies, missing skills, and overall compatibility scores.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-3 hover:shadow-md transition-shadow">
              <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h3 className="text-lg font-bold text-navy-950">Role-Based Security</h3>
              <p className="text-slate-600 text-xs leading-relaxed">
                Strict recruiter ownership checks, job-seeker application isolation, and stateless JWT authorization.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
