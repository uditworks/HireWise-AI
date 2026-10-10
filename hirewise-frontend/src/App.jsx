import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import Navbar from './components/common/Navbar';
import Footer from './components/common/Footer';
import ProtectedRoute from './components/common/ProtectedRoute';

import ErrorBoundary from './components/common/ErrorBoundary';

// Pages
import HomePage from './pages/public/HomePage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import JobsPage from './pages/jobs/JobsPage';
import JobDetailsPage from './pages/jobs/JobDetailsPage';
import SeekerDashboardPage from './pages/seeker/SeekerDashboardPage';
import MyApplicationsPage from './pages/seeker/MyApplicationsPage';
import MyResumesPage from './pages/seeker/MyResumesPage';
import ResumeAnalysisPage from './pages/seeker/ResumeAnalysisPage';
import ResumeMatchPage from './pages/seeker/ResumeMatchPage';
import RecruiterDashboardPage from './pages/recruiter/RecruiterDashboardPage';
import CreateJobPage from './pages/recruiter/CreateJobPage';
import EditJobPage from './pages/recruiter/EditJobPage';
import JobApplicantsPage from './pages/recruiter/JobApplicantsPage';

export default function App() {
  return (
    <ErrorBoundary>
      <Router>
        <AuthProvider>
          <ToastProvider>
            <div className="min-h-screen flex flex-col bg-slate-50 text-slate-800">
            {/* Main Navigation Bar */}
            <Navbar />

            {/* Application Viewport */}
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<HomePage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/jobs" element={<JobsPage />} />
              <Route path="/jobs/:id" element={<JobDetailsPage />} />

              {/* Protected Job Seeker Routes */}
              <Route
                path="/seeker/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['JOB_SEEKER']}>
                    <SeekerDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seeker/applications"
                element={
                  <ProtectedRoute allowedRoles={['JOB_SEEKER']}>
                    <MyApplicationsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seeker/resumes"
                element={
                  <ProtectedRoute allowedRoles={['JOB_SEEKER']}>
                    <MyResumesPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seeker/resumes/:id/analyze"
                element={
                  <ProtectedRoute allowedRoles={['JOB_SEEKER']}>
                    <ResumeAnalysisPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seeker/resumes/:id/match"
                element={
                  <ProtectedRoute allowedRoles={['JOB_SEEKER']}>
                    <ResumeMatchPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seeker/resumes/match"
                element={
                  <ProtectedRoute allowedRoles={['JOB_SEEKER']}>
                    <ResumeMatchPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/seeker/*"
                element={
                  <ProtectedRoute allowedRoles={['JOB_SEEKER']}>
                    <SeekerDashboardPage />
                  </ProtectedRoute>
                }
              />

              {/* Protected Recruiter Routes */}
              <Route
                path="/recruiter/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['RECRUITER']}>
                    <RecruiterDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/recruiter/jobs/create"
                element={
                  <ProtectedRoute allowedRoles={['RECRUITER']}>
                    <CreateJobPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/recruiter/jobs/:id/edit"
                element={
                  <ProtectedRoute allowedRoles={['RECRUITER']}>
                    <EditJobPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/recruiter/jobs/:jobId/applicants"
                element={
                  <ProtectedRoute allowedRoles={['RECRUITER']}>
                    <JobApplicantsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/recruiter/*"
                element={
                  <ProtectedRoute allowedRoles={['RECRUITER']}>
                    <RecruiterDashboardPage />
                  </ProtectedRoute>
                }
              />

              {/* Catch-all Wildcard Route */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>

            {/* Global Footer */}
            <Footer />
          </div>
        </ToastProvider>
      </AuthProvider>
    </Router>
  </ErrorBoundary>
  );
}
