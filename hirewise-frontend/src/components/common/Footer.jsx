import React from 'react';
import { Sparkles, Shield, Cpu, Code2 } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="bg-navy-950 text-slate-400 text-xs border-t border-navy-900 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          {/* Brand Info */}
          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold">
                <Sparkles className="w-4 h-4" />
              </div>
              <span className="text-lg font-bold text-white tracking-tight">
                HireWise <span className="text-blue-500 font-extrabold">AI</span>
              </span>
            </div>
            <p className="text-slate-400 text-sm max-w-sm leading-relaxed">
              An intelligent, transparent recruitment platform connecting top talent with high-growth organizations through explainable algorithmic matching.
            </p>
            <div className="flex items-center gap-3 pt-2 text-slate-300">
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-navy-900 border border-navy-800 text-[11px]">
                <Shield className="w-3.5 h-3.5 text-emerald-400" /> Secure JWT & PDF Vault
              </span>
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-navy-900 border border-navy-800 text-[11px]">
                <Cpu className="w-3.5 h-3.5 text-blue-400" /> Explainable Heuristic AI
              </span>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-200 mb-4">Job Portal</h4>
            <ul className="space-y-2.5">
              <li>
                <Link to="/jobs" className="hover:text-white transition-colors">Explore All Jobs</Link>
              </li>
              <li>
                <Link to="/login" className="hover:text-white transition-colors">Seeker Sign In</Link>
              </li>
              <li>
                <Link to="/register" className="hover:text-white transition-colors">Create Candidate Account</Link>
              </li>
            </ul>
          </div>

          {/* Recruiter Services */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-200 mb-4">For Employers</h4>
            <ul className="space-y-2.5">
              <li>
                <Link to="/login" className="hover:text-white transition-colors">Recruiter Portal</Link>
              </li>
              <li>
                <span className="text-slate-500">Applicant Tracking System</span>
              </li>
              <li>
                <span className="text-slate-500">AI Resume Fit Scoring</span>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="pt-8 border-t border-navy-900/80 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p>© {new Date().getFullYear()} HireWise AI Platform. All rights reserved.</p>
          <div className="flex items-center gap-4 text-slate-400">
            <span>Spring Boot 4.x Backend Active</span>
            <span>•</span>
            <span>Tailwind & React Frontend</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
