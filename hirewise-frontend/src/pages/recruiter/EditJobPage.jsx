import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { jobsApi } from '../../api/jobsApi';
import { useToast } from '../../context/ToastContext';
import { EMPLOYMENT_TYPES, EMPLOYMENT_TYPE_LABELS } from '../../utils/constants';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  ArrowLeft,
  Briefcase,
  MapPin,
  DollarSign,
  Sparkles,
  X,
  AlertCircle,
  Save
} from 'lucide-react';

export default function EditJobPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    location: '',
    employmentType: 'FULL_TIME',
    salaryRange: '',
    status: 'PUBLISHED'
  });

  const [initialStatus, setInitialStatus] = useState('PUBLISHED');
  const [skills, setSkills] = useState([]);
  const [skillInput, setSkillInput] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchJob = async () => {
      setLoading(true);
      setFormError('');
      try {
        const data = await jobsApi.getJobById(id);
        setFormData({
          title: data.title || '',
          description: data.description || '',
          location: data.location || '',
          employmentType: data.employmentType || 'FULL_TIME',
          salaryRange: data.salaryRange || '',
          status: data.status || 'PUBLISHED'
        });
        setInitialStatus(data.status || 'PUBLISHED');
        setSkills(data.requiredSkills ? Array.from(data.requiredSkills) : []);
      } catch (err) {
        setFormError(err.message || 'Could not load job details for editing.');
      } finally {
        setLoading(false);
      }
    };

    fetchJob();
  }, [id]);

  const handleAddSkill = (e) => {
    if (e) e.preventDefault();
    const trimmed = skillInput.trim();
    if (trimmed && !skills.includes(trimmed)) {
      setSkills((prev) => [...prev, trimmed]);
      setSkillInput('');
      if (fieldErrors.skills) {
        setFieldErrors((prev) => ({ ...prev, skills: '' }));
      }
    }
  };

  const handleRemoveSkill = (skillToRemove) => {
    setSkills((prev) => prev.filter((s) => s !== skillToRemove));
  };

  const handleKeyDownSkill = (e) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      handleAddSkill();
    }
  };

  const validate = () => {
    const errors = {};

    if (!formData.title.trim()) {
      errors.title = 'Job title is required';
    } else if (formData.title.trim().length < 3 || formData.title.trim().length > 150) {
      errors.title = 'Title must be between 3 and 150 characters';
    }

    if (!formData.description.trim()) {
      errors.description = 'Job description is required';
    } else if (formData.description.trim().length < 10) {
      errors.description = 'Description must be at least 10 characters';
    }

    if (!formData.location.trim()) {
      errors.location = 'Location is required';
    }

    if (!formData.employmentType) {
      errors.employmentType = 'Employment type is required';
    }

    if (skills.length === 0) {
      errors.skills = 'At least one required skill must be specified';
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
      // 1. Update job details
      const updatePayload = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        location: formData.location.trim(),
        employmentType: formData.employmentType,
        salaryRange: formData.salaryRange.trim() || undefined,
        requiredSkills: skills
      };

      await jobsApi.updateJob(id, updatePayload);

      // 2. If status changed, update status via PATCH
      if (formData.status !== initialStatus) {
        await jobsApi.updateJobStatus(id, formData.status);
      }

      showSuccess(`Job posting "${formData.title}" updated successfully!`);
      navigate('/recruiter/dashboard');
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to update job posting.';
      setFormError(msg);
      showError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex-1 bg-slate-50 py-8 sm:py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
        {/* Back Link */}
        <div>
          <Link
            to="/recruiter/dashboard"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-blue-600 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Recruiter Hub</span>
          </Link>
        </div>

        {/* Page Title */}
        <div className="space-y-1">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-50 border border-indigo-200/80 text-indigo-700 text-xs font-semibold uppercase tracking-wider mb-1">
            <Briefcase className="w-3.5 h-3.5 text-indigo-600" />
            <span>Job Editor</span>
          </div>
          <h1 className="text-3xl font-extrabold text-navy-950 tracking-tight">
            Edit Job Posting #{id}
          </h1>
          <p className="text-sm text-slate-500">
            Update job details, adjust required skills, or change posting publication status.
          </p>
        </div>

        {/* Loading / Error States */}
        {loading ? (
          <div className="bg-white rounded-2xl border border-slate-200 p-16 flex flex-col items-center justify-center text-center space-y-3 shadow-sm">
            <LoadingSpinner size="lg" />
            <p className="text-sm font-medium text-slate-600">Loading job details...</p>
          </div>
        ) : formError && !formData.title ? (
          <div className="bg-white rounded-2xl border border-red-200 p-12 text-center space-y-4 shadow-sm">
            <AlertCircle className="w-10 h-10 text-red-600 mx-auto" />
            <div className="space-y-1">
              <h3 className="text-lg font-bold text-navy-950">Job Not Found</h3>
              <p className="text-xs text-slate-600">{formError}</p>
            </div>
            <Link to="/recruiter/dashboard">
              <Button variant="navy" size="sm">
                <span>Return to Recruiter Hub</span>
              </Button>
            </Link>
          </div>
        ) : (
          /* Edit Form Card */
          <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-sm">
            <form onSubmit={handleSubmit} className="space-y-6" noValidate>
              {formError && (
                <div className="p-4 rounded-xl bg-red-50 border border-red-200 flex items-start gap-3 text-red-800 text-sm">
                  <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                  <p className="text-xs text-red-700">{formError}</p>
                </div>
              )}

              {/* Status Selector */}
              <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                  <label htmlFor="status" className="text-xs font-bold text-navy-950 block">
                    Posting Status Lifecycle
                  </label>
                  <p className="text-[11px] text-slate-500">
                    Control visibility in the public candidates catalog.
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <select
                    id="status"
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                    className="rounded-xl border border-slate-300 bg-white text-slate-900 text-xs font-semibold px-3 py-2 transition-all focus:outline-none focus:ring-2 focus:ring-blue-100"
                  >
                    <option value="PUBLISHED">🟢 PUBLISHED (Active)</option>
                    <option value="DRAFT">⚪ DRAFT (Unpublished)</option>
                    <option value="CLOSED">🔴 CLOSED (Archived)</option>
                  </select>
                  <Badge status={formData.status} size="sm" />
                </div>
              </div>

              {/* Position Title */}
              <Input
                label="Position Title"
                id="title"
                name="title"
                placeholder="e.g. Senior Backend Engineer"
                value={formData.title}
                onChange={handleChange}
                error={fieldErrors.title}
                required
                icon={<Briefcase className="w-4 h-4 text-slate-400" />}
              />

              {/* Location & Employment Type Row */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Input
                  label="Location"
                  id="location"
                  name="location"
                  placeholder="e.g. Remote, Austin, TX"
                  value={formData.location}
                  onChange={handleChange}
                  error={fieldErrors.location}
                  required
                  icon={<MapPin className="w-4 h-4 text-slate-400" />}
                />

                <div className="flex flex-col gap-1.5 w-full">
                  <label
                    htmlFor="employmentType"
                    className="text-xs font-semibold text-slate-700 flex items-center justify-between"
                  >
                    <span>
                      Employment Type <span className="text-red-500">*</span>
                    </span>
                  </label>
                  <select
                    id="employmentType"
                    name="employmentType"
                    value={formData.employmentType}
                    onChange={handleChange}
                    className="w-full rounded-xl border border-slate-300 bg-white text-slate-900 text-sm px-3.5 py-2.5 transition-all focus:outline-none focus:ring-2 focus:ring-blue-100 focus:border-blue-600"
                  >
                    {Object.entries(EMPLOYMENT_TYPES).map(([key, value]) => (
                      <option key={key} value={value}>
                        {EMPLOYMENT_TYPE_LABELS[key] || value}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Salary Range */}
              <Input
                label="Salary Range (Optional)"
                id="salaryRange"
                name="salaryRange"
                placeholder="e.g. $130,000 - $160,000"
                value={formData.salaryRange}
                onChange={handleChange}
                icon={<DollarSign className="w-4 h-4 text-slate-400" />}
              />

              {/* Required Skills Chip Input */}
              <div className="space-y-2">
                <label className="text-xs font-semibold text-slate-700 block">
                  Required Technical Skills <span className="text-red-500">*</span>
                </label>

                <div className="flex gap-2">
                  <Input
                    id="skill-input"
                    name="skillInput"
                    placeholder="Add a required skill..."
                    value={skillInput}
                    onChange={(e) => setSkillInput(e.target.value)}
                    onKeyDown={handleKeyDownSkill}
                    icon={<Sparkles className="w-4 h-4 text-slate-400" />}
                    className="flex-1"
                  />
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={handleAddSkill}
                    disabled={!skillInput.trim()}
                  >
                    <span>Add</span>
                  </Button>
                </div>

                {fieldErrors.skills && (
                  <p className="text-xs text-red-600 font-medium">{fieldErrors.skills}</p>
                )}

                <div className="flex flex-wrap gap-2 pt-1">
                  {skills.map((skill) => (
                    <span
                      key={skill}
                      className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-blue-50 text-blue-800 text-xs font-semibold border border-blue-200"
                    >
                      <span>{skill}</span>
                      <button
                        type="button"
                        onClick={() => handleRemoveSkill(skill)}
                        className="text-blue-400 hover:text-red-600 focus:outline-none"
                      >
                        <X className="w-3.5 h-3.5" />
                      </button>
                    </span>
                  ))}
                </div>
              </div>

              {/* Job Description */}
              <div className="space-y-1.5">
                <label htmlFor="description" className="text-xs font-semibold text-slate-700 block">
                  Job Description & Qualifications <span className="text-red-500">*</span>
                </label>
                <textarea
                  id="description"
                  name="description"
                  rows={6}
                  value={formData.description}
                  onChange={handleChange}
                  className={`w-full rounded-xl border bg-white text-slate-900 text-sm p-3.5 transition-all focus:outline-none focus:ring-2 focus:ring-offset-1 resize-y ${
                    fieldErrors.description
                      ? 'border-red-300 focus:border-red-500 focus:ring-red-200'
                      : 'border-slate-300 focus:border-blue-600 focus:ring-blue-100'
                  }`}
                />
                {fieldErrors.description && (
                  <p className="text-xs text-red-600 font-medium">{fieldErrors.description}</p>
                )}
              </div>

              {/* Action Buttons */}
              <div className="pt-4 flex items-center justify-end gap-3 border-t border-slate-100">
                <Link to="/recruiter/dashboard">
                  <Button type="button" variant="outline" disabled={submitting}>
                    Cancel
                  </Button>
                </Link>
                <Button type="submit" variant="navy" loading={submitting}>
                  <Save className="w-4 h-4" />
                  <span>Save Changes</span>
                </Button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}
