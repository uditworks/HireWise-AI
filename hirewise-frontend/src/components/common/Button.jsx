import React from 'react';
import { Loader2 } from 'lucide-react';

export default function Button({
  children,
  type = 'button',
  variant = 'primary',
  size = 'md',
  disabled = false,
  loading = false,
  icon,
  className = '',
  onClick,
  ...props
}) {
  const baseStyles = 'inline-flex items-center justify-center font-semibold rounded-xl transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed select-none';

  const variantStyles = {
    primary: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500 shadow-sm shadow-blue-600/20 active:scale-[0.98]',
    navy: 'bg-navy-900 text-white hover:bg-navy-800 focus:ring-navy-900 shadow-sm shadow-navy-950/20 active:scale-[0.98]',
    secondary: 'bg-white text-slate-700 border border-slate-300 hover:bg-slate-50 focus:ring-slate-400 active:scale-[0.98]',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500 shadow-sm shadow-red-600/20 active:scale-[0.98]',
    outline: 'bg-transparent text-blue-600 border border-blue-600 hover:bg-blue-50 focus:ring-blue-500 active:scale-[0.98]',
    ghost: 'bg-transparent text-slate-600 hover:text-navy-950 hover:bg-slate-100 focus:ring-slate-400',
  };

  const sizeStyles = {
    sm: 'px-3 py-1.5 text-xs gap-1.5',
    md: 'px-4 py-2.5 text-sm gap-2',
    lg: 'px-6 py-3.5 text-base gap-2.5',
  };

  return (
    <button
      type={type}
      disabled={disabled || loading}
      onClick={onClick}
      className={`${baseStyles} ${variantStyles[variant] || variantStyles.primary} ${sizeStyles[size] || sizeStyles.md} ${className}`}
      {...props}
    >
      {loading ? (
        <>
          <Loader2 className="w-4 h-4 animate-spin text-current" />
          <span>{children}</span>
        </>
      ) : (
        <>
          {icon && <span className="flex-shrink-0">{icon}</span>}
          <span>{children}</span>
        </>
      )}
    </button>
  );
}
