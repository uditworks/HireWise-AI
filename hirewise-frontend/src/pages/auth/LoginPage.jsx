import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { ROLES } from '../../utils/constants';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import { Mail, Lock, Eye, EyeOff, Sparkles, AlertCircle, CheckCircle2, ArrowRight } from 'lucide-react';

export default function LoginPage() {
  const { login, isAuthenticated, isSeeker, isRecruiter, loading: authLoading } = useAuth();
  const { showSuccess, showError } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [formData, setFormData] = useState({
    email: location.state?.registeredEmail || '',
    password: ''
  });
  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Success notice if redirected from successful registration
  const registrationMessage = location.state?.message || '';

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      const redirectPath = location.state?.from?.pathname || 
        (isRecruiter ? '/recruiter/dashboard' : '/seeker/dashboard');
      navigate(redirectPath, { replace: true });
    }
  }, [isAuthenticated, isRecruiter, isSeeker, navigate, location]);

  const validate = () => {
    const errors = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!formData.email.trim()) {
      errors.email = 'Email address is required';
    } else if (!emailRegex.test(formData.email.trim())) {
      errors.email = 'Please enter a valid email address';
    }

    if (!formData.password) {
      errors.password = 'Password is required';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear field-specific error on edit
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: '' }));
    }
    if (formError) {
      setFormError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    if (!validate()) {
      return;
    }

    setSubmitting(true);
    try {
      const userProfile = await login({
        email: formData.email.trim(),
        password: formData.password
      });

      showSuccess(`Welcome back, ${userProfile.fullName || 'User'}!`);

      // Determine redirect path
      const targetPath = location.state?.from?.pathname ||
        (userProfile.role === ROLES.RECRUITER ? '/recruiter/dashboard' : '/seeker/dashboard');

      navigate(targetPath, { replace: true });
    } catch (err) {
      // Determine user-friendly error message
      let errorMsg = 'Invalid email or password. Please try again.';
      
      if (err.response) {
        if (err.response.status === 401) {
          errorMsg = 'Invalid email or password. Please check your credentials.';
        } else if (err.response.status === 403) {
          errorMsg = 'Access denied. Your account does not have permission.';
        } else if (err.response.data?.message) {
          errorMsg = err.response.data.message;
        }
      } else if (err.request) {
        errorMsg = 'Cannot connect to backend server. Please verify the Spring Boot service is running.';
      } else if (err.message) {
        errorMsg = err.message;
      }

      setFormError(errorMsg);
      showError(errorMsg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-slate-50">
      <div className="w-full max-w-md space-y-6">
        {/* Header Branding */}
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-gradient-to-tr from-navy-900 to-blue-600 text-white shadow-lg shadow-blue-900/20 mb-2">
            <Sparkles className="w-6 h-6" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-navy-950 tracking-tight">
            Welcome Back
          </h1>
          <p className="text-sm text-slate-500">
            Sign in to access your HireWise AI dashboard
          </p>
        </div>

        {/* Registration Success Banner */}
        {registrationMessage && (
          <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 flex items-start gap-3 text-emerald-800 text-sm">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold">Registration Successful</p>
              <p className="text-xs text-emerald-700 mt-0.5">{registrationMessage}</p>
            </div>
          </div>
        )}

        {/* Login Error Banner */}
        {formError && (
          <div className="p-4 rounded-xl bg-red-50 border border-red-200 flex items-start gap-3 text-red-800 text-sm animate-in fade-in">
            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <p className="font-semibold text-xs uppercase tracking-wider text-red-600">Authentication Failed</p>
              <p className="text-xs text-red-700 mt-0.5">{formError}</p>
            </div>
          </div>
        )}

        {/* Login Form Card */}
        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/80 shadow-sm">
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            {/* Email Field */}
            <Input
              label="Email Address"
              id="email"
              name="email"
              type="email"
              placeholder="you@example.com"
              value={formData.email}
              onChange={handleChange}
              error={fieldErrors.email}
              icon={<Mail className="w-4 h-4 text-slate-400" />}
              autoComplete="email"
              required
            />

            {/* Password Field */}
            <Input
              label="Password"
              id="password"
              name="password"
              type={showPassword ? 'text' : 'password'}
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              error={fieldErrors.password}
              icon={<Lock className="w-4 h-4 text-slate-400" />}
              autoComplete="current-password"
              required
              endAdornment={
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="p-1 text-slate-400 hover:text-slate-600 focus:outline-none"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  tabIndex="-1"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              }
            />

            {/* Submit Button */}
            <Button
              type="submit"
              variant="navy"
              size="lg"
              className="w-full mt-2"
              loading={submitting || authLoading}
            >
              <span>Sign In</span>
              <ArrowRight className="w-4 h-4" />
            </Button>
          </form>

          {/* Footer Card Link */}
          <div className="mt-6 pt-5 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-600">
              Don't have an account?{' '}
              <Link
                to="/register"
                className="font-semibold text-blue-600 hover:text-blue-700 hover:underline inline-flex items-center gap-1"
              >
                Create Account
              </Link>
            </p>
          </div>
        </div>

        {/* Security / Privacy Note */}
        <p className="text-center text-[11px] text-slate-400">
          Protected with JWT Bearer Authentication & Spring Security
        </p>
      </div>
    </div>
  );
}
