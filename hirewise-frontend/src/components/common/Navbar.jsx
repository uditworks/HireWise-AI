import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Sparkles, Menu, X, LogOut, Briefcase, FileText, LayoutDashboard, PlusCircle, Search } from 'lucide-react';
import Badge from './Badge';

export default function Navbar() {
  const { user, isAuthenticated, isSeeker, isRecruiter, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
    setMobileMenuOpen(false);
  };

  const isActive = (path) => location.pathname === path;

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-3 group" onClick={() => setMobileMenuOpen(false)}>
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-navy-900 via-blue-800 to-blue-600 flex items-center justify-center text-white shadow-md shadow-blue-900/20 group-hover:scale-105 transition-transform">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xl font-bold tracking-tight text-navy-950 flex items-center gap-1.5">
              HireWise <span className="text-blue-600 font-extrabold">AI</span>
            </span>
            <span className="block text-[10px] uppercase tracking-wider font-semibold text-slate-400 -mt-1">
              Intelligent Portal
            </span>
          </div>
        </Link>

        {/* Desktop Navigation Links */}
        <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-slate-600">
          <Link
            to="/jobs"
            className={`flex items-center gap-1.5 py-1 transition-colors ${
              isActive('/jobs') ? 'text-blue-600 font-semibold border-b-2 border-blue-600' : 'hover:text-blue-600'
            }`}
          >
            <Search className="w-4 h-4" />
            <span>Browse Jobs</span>
          </Link>

          {/* Job Seeker Navigation */}
          {isAuthenticated && isSeeker && (
            <>
              <Link
                to="/seeker/dashboard"
                className={`flex items-center gap-1.5 py-1 transition-colors ${
                  isActive('/seeker/dashboard') ? 'text-blue-600 font-semibold border-b-2 border-blue-600' : 'hover:text-blue-600'
                }`}
              >
                <LayoutDashboard className="w-4 h-4" />
                <span>Dashboard</span>
              </Link>
              <Link
                to="/seeker/applications"
                className={`flex items-center gap-1.5 py-1 transition-colors ${
                  isActive('/seeker/applications') ? 'text-blue-600 font-semibold border-b-2 border-blue-600' : 'hover:text-blue-600'
                }`}
              >
                <Briefcase className="w-4 h-4" />
                <span>My Applications</span>
              </Link>
              <Link
                to="/seeker/resumes"
                className={`flex items-center gap-1.5 py-1 transition-colors ${
                  isActive('/seeker/resumes') ? 'text-blue-600 font-semibold border-b-2 border-blue-600' : 'hover:text-blue-600'
                }`}
              >
                <FileText className="w-4 h-4" />
                <span>Resumes & AI</span>
              </Link>
            </>
          )}

          {/* Recruiter Navigation */}
          {isAuthenticated && isRecruiter && (
            <>
              <Link
                to="/recruiter/dashboard"
                className={`flex items-center gap-1.5 py-1 transition-colors ${
                  isActive('/recruiter/dashboard') ? 'text-blue-600 font-semibold border-b-2 border-blue-600' : 'hover:text-blue-600'
                }`}
              >
                <LayoutDashboard className="w-4 h-4" />
                <span>Recruiter Hub</span>
              </Link>
              <Link
                to="/recruiter/jobs/create"
                className={`flex items-center gap-1.5 py-1 transition-colors ${
                  isActive('/recruiter/jobs/create') ? 'text-blue-600 font-semibold border-b-2 border-blue-600' : 'hover:text-blue-600'
                }`}
              >
                <PlusCircle className="w-4 h-4" />
                <span>Post Job</span>
              </Link>
            </>
          )}
        </nav>

        {/* Desktop User Profile / Auth Action Buttons */}
        <div className="hidden md:flex items-center gap-3">
          {isAuthenticated ? (
            <div className="flex items-center gap-3 bg-slate-50 border border-slate-200/80 py-1.5 px-3 rounded-xl">
              <div className="w-8 h-8 rounded-lg bg-navy-900 text-white flex items-center justify-center font-bold text-xs">
                {user?.fullName?.charAt(0) || 'U'}
              </div>
              <div className="text-left leading-tight">
                <p className="text-xs font-bold text-navy-950 truncate max-w-[130px]">{user?.fullName}</p>
                <Badge status={user?.role} size="sm" />
              </div>
              <button
                onClick={handleLogout}
                className="ml-2 text-slate-400 hover:text-red-600 p-1 rounded-md transition-colors"
                title="Log Out"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="px-4 py-2 text-sm font-semibold text-slate-700 hover:text-navy-900 hover:bg-slate-100 rounded-xl transition-all"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                className="px-4 py-2 text-sm font-semibold text-white bg-navy-900 hover:bg-navy-800 rounded-xl shadow-sm shadow-navy-950/20 transition-all"
              >
                Get Started
              </Link>
            </div>
          )}
        </div>

        {/* Mobile Hamburger Toggle Button */}
        <div className="md:hidden flex items-center">
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 rounded-lg text-slate-600 hover:text-navy-900 hover:bg-slate-100 focus:outline-none"
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-slate-200 bg-white px-4 pt-3 pb-5 space-y-3 shadow-lg animate-in slide-in-from-top-1">
          {isAuthenticated && (
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-lg bg-navy-900 text-white flex items-center justify-center font-bold text-sm">
                  {user?.fullName?.charAt(0) || 'U'}
                </div>
                <div>
                  <p className="text-sm font-bold text-slate-900">{user?.fullName}</p>
                  <p className="text-xs text-slate-500">{user?.email}</p>
                </div>
              </div>
              <Badge status={user?.role} size="sm" />
            </div>
          )}

          <div className="flex flex-col gap-1 text-sm font-medium text-slate-700">
            <Link
              to="/jobs"
              className="px-3 py-2 rounded-lg hover:bg-slate-50"
              onClick={() => setMobileMenuOpen(false)}
            >
              Browse Jobs
            </Link>

            {isAuthenticated && isSeeker && (
              <>
                <Link
                  to="/seeker/dashboard"
                  className="px-3 py-2 rounded-lg hover:bg-slate-50"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Seeker Dashboard
                </Link>
                <Link
                  to="/seeker/applications"
                  className="px-3 py-2 rounded-lg hover:bg-slate-50"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  My Applications
                </Link>
                <Link
                  to="/seeker/resumes"
                  className="px-3 py-2 rounded-lg hover:bg-slate-50"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Resumes & AI Matching
                </Link>
              </>
            )}

            {isAuthenticated && isRecruiter && (
              <>
                <Link
                  to="/recruiter/dashboard"
                  className="px-3 py-2 rounded-lg hover:bg-slate-50"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Recruiter Hub
                </Link>
                <Link
                  to="/recruiter/jobs/create"
                  className="px-3 py-2 rounded-lg hover:bg-slate-50"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Post a New Job
                </Link>
              </>
            )}
          </div>

          <div className="pt-2 border-t border-slate-100 flex flex-col gap-2">
            {isAuthenticated ? (
              <button
                onClick={handleLogout}
                className="w-full flex items-center justify-center gap-2 py-2.5 text-sm font-semibold text-red-600 bg-red-50 hover:bg-red-100 rounded-xl transition-colors"
              >
                <LogOut className="w-4 h-4" />
                <span>Sign Out</span>
              </button>
            ) : (
              <>
                <Link
                  to="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="w-full text-center py-2.5 text-sm font-semibold text-slate-700 bg-slate-100 rounded-xl"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  onClick={() => setMobileMenuOpen(false)}
                  className="w-full text-center py-2.5 text-sm font-semibold text-white bg-navy-900 rounded-xl shadow-sm"
                >
                  Create Account
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
