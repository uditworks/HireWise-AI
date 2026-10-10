import React from 'react';
import { Loader2 } from 'lucide-react';

export default function LoadingSpinner({
  size = 'md',
  message = 'Loading...',
  fullScreen = false,
  className = ''
}) {
  const sizeMap = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };

  const spinner = (
    <div className={`flex flex-col items-center justify-center gap-3 p-6 text-slate-500 ${className}`}>
      <Loader2 className={`${sizeMap[size] || sizeMap.md} animate-spin text-blue-600`} />
      {message && <p className="text-xs sm:text-sm font-medium text-slate-600">{message}</p>}
    </div>
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 bg-white/80 backdrop-blur-sm z-50 flex items-center justify-center">
        {spinner}
      </div>
    );
  }

  return spinner;
}
