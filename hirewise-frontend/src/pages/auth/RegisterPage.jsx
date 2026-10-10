import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Badge from '../../components/common/Badge';
import {
  User,
  Mail,
  Lock,
  Eye,
  EyeOff,
  Sparkles,
  AlertCircle,
  CheckCircle2,
  XCircle,
  ArrowRight,
  ShieldCheck
} from 'lucide-react';

export default function RegisterPage() {
  const { register, isAuthenticated, isSeeker, isRecruiter, loading: authLoading } = useAuth();
  const { showSuccess, showError } = useToast();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate(isRecruiter ? '/recruiter/dashboard' : '/seeker/dashboard', { replace: true });
    }
  }, [isAuthenticated, isRecruiter, isSeeker, navigate]);

  // Real-time password criteria evaluation
  const passwordCriteria = {
    length: formData.password.length >= 8 && formData.password.length <= 64,
    upper: /[A-Z]/.test(formData.password),
    lower: /[a-z]/.test(formData.password),
    number: /\d/.test(formData.password),
    special: /[@$!%*?&#^()_+\-=[\]{};':"\\|,.<>/?]/.test(formData.password)
  };

  const isPasswordValid = Object.values(passwordCriteria).every(Boolean);
  const doPasswordsMatch = formData.password && formData.password === formData.confirmPassword;

  const validate = () => {
    const errors = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!formData.fullName.trim()) {
      errors.fullName = 'Full name is required';
    } else if (formData.fullName.trim().length < 2) {
      errors.fullName = 'Full name must be at least 2 characters';
    }

    if (!formData.email.trim()) {
      errors.email = 'Email address is required';
    } else if (!emailRegex.test(formData.email.trim())) {
      errors.email = 'Please enter a valid email address';
    }

    if (!formData.password) {
      errors.password = 'Password is required';
    } else if (!isPasswordValid) {
      errors.password = 'Password does not meet all the security criteria below';
    }

    if (!formData.confirmPassword) {
      errors.confirmPassword = 'Confirmation password is required';
    } else if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
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
      // Public registration payload exclusively creates JOB_SEEKER accounts
      await register({
        fullName: formData.fullName.trim(),
        email: formData.email.trim().toLowerCase(),
        password: formData.password
      });

      showSuccess('Account created successfully! Please sign in to continue.');

      // Redirect to login page with registered email prefilled
      navigate('/login', {
        state: {
          registeredEmail: formData.email.trim().toLowerCase(),
          message: 'Your Job Seeker account has been created. Please sign in below.'
        }
      });
    } catch (err) {
      let errorMsg = 'Registration failed. Please check the form and try again.';

      if (err.response) {
        if (err.response.status === 409 || err.response.data?.message?.includes('already exists')) {
          errorMsg = `An account with email '${formData.email.trim()}' already exists. Please sign in instead.`;
        } else if (err.response.data?.message) {
          errorMsg = err.response.data.message;
        } else if (err.response.data?.errors) {
          // If backend returns field validation errors map
          const messages = Object.values(err.response.data.errors).join(', ');
          errorMsg = messages || errorMsg;
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
      <div className="w-full max-w-lg space-y-6">
        {/* Header Branding */}
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-gradient-to-tr from-navy-900 to-blue-600 text-white shadow-lg shadow-blue-900/20 mb-2">
            <Sparkles className="w-6 h-6" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-navy-950 tracking-tight">
            Create Your Account
          </h1>
          <p className="text-sm text-slate-500 flex items-center justify-center gap-2">
            <span>Join HireWise AI</span>
            <span>•</span>
            <Badge status="JOB_SEEKER" label="Job Seeker Account" size="sm" />
          </p>
        </div>

        {/* Global Registration Error Banner */}
        {formError && (
          <div className="p-4 rounded-xl bg-red-50 border border-red-200 flex items-start gap-3 text-red-800 text-sm animate-in fade-in">
            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <p className="font-semibold text-xs uppercase tracking-wider text-red-600">Registration Error</p>
              <p className="text-xs text-red-700 mt-0.5">{formError}</p>
            </div>
          </div>
        )}

        {/* Registration Form Card */}
        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/80 shadow-sm">
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            {/* Full Name */}
            <Input
              label="Full Name"
              id="fullName"
              name="fullName"
              type="text"
              placeholder="e.g. Jane Doe"
              value={formData.fullName}
              onChange={handleChange}
              error={fieldErrors.fullName}
              icon={<User className="w-4 h-4 text-slate-400" />}
              autoComplete="name"
              required
            />

            {/* Email Address */}
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

            {/* Password */}
            <Input
              label="Password"
              id="password"
              name="password"
              type={showPassword ? 'text' : 'password'}
              placeholder="Min. 8 characters with mix of cases, numbers & symbols"
              value={formData.password}
              onChange={handleChange}
              error={fieldErrors.password}
              icon={<Lock className="w-4 h-4 text-slate-400" />}
              autoComplete="new-password"
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

            {/* Live Password Strength / Requirements Checklist */}
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/80 text-xs space-y-1.5">
              <p className="font-semibold text-slate-700 flex items-center gap-1.5 mb-1 text-[11px] uppercase tracking-wider">
                <ShieldCheck className="w-3.5 h-3.5 text-blue-600" />
                Password Requirements
              </p>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-1.5">
                <div className={`flex items-center gap-1.5 ${passwordCriteria.length ? 'text-emerald-700 font-medium' : 'text-slate-500'}`}>
                  {passwordCriteria.length ? <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> : <XCircle className="w-3.5 h-3.5 text-slate-400" />}
                  <span>8 to 64 characters</span>
                </div>
                <div className={`flex items-center gap-1.5 ${passwordCriteria.upper ? 'text-emerald-700 font-medium' : 'text-slate-500'}`}>
                  {passwordCriteria.upper ? <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> : <XCircle className="w-3.5 h-3.5 text-slate-400" />}
                  <span>At least 1 uppercase (A-Z)</span>
                </div>
                <div className={`flex items-center gap-1.5 ${passwordCriteria.lower ? 'text-emerald-700 font-medium' : 'text-slate-500'}`}>
                  {passwordCriteria.lower ? <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> : <XCircle className="w-3.5 h-3.5 text-slate-400" />}
                  <span>At least 1 lowercase (a-z)</span>
                </div>
                <div className={`flex items-center gap-1.5 ${passwordCriteria.number ? 'text-emerald-700 font-medium' : 'text-slate-500'}`}>
                  {passwordCriteria.number ? <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> : <XCircle className="w-3.5 h-3.5 text-slate-400" />}
                  <span>At least 1 number (0-9)</span>
                </div>
                <div className={`flex items-center gap-1.5 sm:col-span-2 ${passwordCriteria.special ? 'text-emerald-700 font-medium' : 'text-slate-500'}`}>
                  {passwordCriteria.special ? <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> : <XCircle className="w-3.5 h-3.5 text-slate-400" />}
                  <span>At least 1 special character (!@#$%^&*...)</span>
                </div>
              </div>
            </div>

            {/* Confirm Password */}
            <Input
              label="Confirm Password"
              id="confirmPassword"
              name="confirmPassword"
              type={showConfirmPassword ? 'text' : 'password'}
              placeholder="Re-enter your password"
              value={formData.confirmPassword}
              onChange={handleChange}
              error={fieldErrors.confirmPassword}
              icon={<Lock className="w-4 h-4 text-slate-400" />}
              autoComplete="new-password"
              required
              endAdornment={
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="p-1 text-slate-400 hover:text-slate-600 focus:outline-none"
                  aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                  tabIndex="-1"
                >
                  {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              }
            />

            {/* Password match indicator */}
            {formData.confirmPassword && (
              <p className={`text-xs flex items-center gap-1 ${doPasswordsMatch ? 'text-emerald-600 font-medium' : 'text-red-500'}`}>
                {doPasswordsMatch ? (
                  <>
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> Passwords match
                  </>
                ) : (
                  <>
                    <XCircle className="w-3.5 h-3.5 text-red-500" /> Passwords do not match
                  </>
                )}
              </p>
            )}

            {/* Account Type Notice */}
            <div className="p-3 rounded-xl bg-blue-50/70 border border-blue-100 text-xs text-blue-900 leading-relaxed">
              <p className="font-semibold mb-0.5">Account Role Note:</p>
              <p className="text-blue-800 text-[11px]">
                Public registration creates a <strong>Job Seeker</strong> profile. Recruiter accounts are provisioned administratively.
              </p>
            </div>

            {/* Submit Button */}
            <Button
              type="submit"
              variant="navy"
              size="lg"
              className="w-full mt-2"
              loading={submitting || authLoading}
            >
              <span>Create Account</span>
              <ArrowRight className="w-4 h-4" />
            </Button>
          </form>

          {/* Footer Card Link */}
          <div className="mt-6 pt-5 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-600">
              Already have an account?{' '}
              <Link
                to="/login"
                className="font-semibold text-blue-600 hover:text-blue-700 hover:underline inline-flex items-center gap-1"
              >
                Sign In
              </Link>
            </p>
          </div>
        </div>

        {/* Security / Privacy Note */}
        <p className="text-center text-[11px] text-slate-400">
          Your credentials are securely hashed using BCrypt and never stored in plain text.
        </p>
      </div>
    </div>
  );
}
