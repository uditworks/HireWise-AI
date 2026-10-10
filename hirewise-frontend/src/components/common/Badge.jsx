import React from 'react';

export default function Badge({ status, label, variant, size = 'md' }) {
  const getVariant = () => {
    if (variant) return variant;

    switch (status) {
      // Job Statuses
      case 'PUBLISHED':
        return 'emerald';
      case 'DRAFT':
        return 'slate';
      case 'CLOSED':
        return 'red';

      // Application Statuses
      case 'SUBMITTED':
        return 'blue';
      case 'UNDER_REVIEW':
        return 'amber';
      case 'SHORTLISTED':
        return 'purple';
      case 'ACCEPTED':
        return 'emerald';
      case 'REJECTED':
        return 'red';
      case 'WITHDRAWN':
        return 'slate';

      // Resume Processing Statuses
      case 'PARSED':
        return 'emerald';
      case 'UPLOADED':
        return 'blue';
      case 'FAILED':
        return 'red';

      // Employment Types
      case 'FULL_TIME':
        return 'blue';
      case 'PART_TIME':
        return 'purple';
      case 'CONTRACT':
        return 'amber';
      case 'INTERNSHIP':
        return 'emerald';
      case 'FREELANCE':
        return 'slate';

      // Roles
      case 'JOB_SEEKER':
        return 'blue';
      case 'RECRUITER':
        return 'purple';

      default:
        return 'slate';
    }
  };

  const currentVariant = getVariant();

  const variantStyles = {
    emerald: 'bg-emerald-50 text-emerald-700 border-emerald-200/80',
    blue: 'bg-blue-50 text-blue-700 border-blue-200/80',
    purple: 'bg-purple-50 text-purple-700 border-purple-200/80',
    amber: 'bg-amber-50 text-amber-700 border-amber-200/80',
    red: 'bg-red-50 text-red-700 border-red-200/80',
    slate: 'bg-slate-100 text-slate-700 border-slate-200',
  };

  const sizeStyles = {
    sm: 'px-2 py-0.5 text-[11px]',
    md: 'px-2.5 py-1 text-xs',
    lg: 'px-3.5 py-1.5 text-sm',
  };

  const displayLabel = label || (status ? status.replace(/_/g, ' ') : '');

  return (
    <span
      className={`inline-flex items-center font-semibold rounded-full border capitalize tracking-wide ${
        variantStyles[currentVariant] || variantStyles.slate
      } ${sizeStyles[size] || sizeStyles.md}`}
    >
      {displayLabel}
    </span>
  );
}
