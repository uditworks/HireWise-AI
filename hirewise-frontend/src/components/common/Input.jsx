import React from 'react';

export default function Input({
  label,
  id,
  name,
  type = 'text',
  value,
  onChange,
  placeholder,
  error,
  helperText,
  required = false,
  disabled = false,
  icon,
  endAdornment,
  className = '',
  ...props
}) {
  const inputId = id || name;

  return (
    <div className={`flex flex-col gap-1.5 w-full ${className}`}>
      {label && (
        <label htmlFor={inputId} className="text-xs font-semibold text-slate-700 flex items-center justify-between">
          <span>
            {label} {required && <span className="text-red-500">*</span>}
          </span>
        </label>
      )}

      <div className="relative flex items-center">
        {icon && (
          <div className="absolute left-3.5 text-slate-400 pointer-events-none flex items-center justify-center">
            {icon}
          </div>
        )}

        <input
          id={inputId}
          name={name}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          disabled={disabled}
          required={required}
          className={`w-full rounded-xl border bg-white text-slate-900 text-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-1 disabled:bg-slate-100 disabled:cursor-not-allowed ${
            icon ? 'pl-10' : 'pl-3.5'
          } ${
            endAdornment ? 'pr-10' : 'pr-3.5'
          } py-2.5 ${
            error
              ? 'border-red-300 focus:border-red-500 focus:ring-red-200'
              : 'border-slate-300 focus:border-blue-600 focus:ring-blue-100'
          }`}
          {...props}
        />

        {endAdornment && (
          <div className="absolute right-3 text-slate-400 flex items-center justify-center">
            {endAdornment}
          </div>
        )}
      </div>

      {error ? (
        <p className="text-xs text-red-600 font-medium mt-0.5">{error}</p>
      ) : helperText ? (
        <p className="text-xs text-slate-500 mt-0.5">{helperText}</p>
      ) : null}
    </div>
  );
}
