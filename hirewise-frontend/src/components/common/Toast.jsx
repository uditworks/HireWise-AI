import React from 'react';
import { CheckCircle2, AlertCircle, Info, AlertTriangle, X } from 'lucide-react';

export default function Toast({ toast, onClose }) {
  const { id, type, message } = toast;

  const typeConfig = {
    success: {
      bg: 'bg-emerald-50 border-emerald-200 text-emerald-800',
      icon: <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />,
    },
    error: {
      bg: 'bg-red-50 border-red-200 text-red-800',
      icon: <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0" />,
    },
    warning: {
      bg: 'bg-amber-50 border-amber-200 text-amber-800',
      icon: <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0" />,
    },
    info: {
      bg: 'bg-blue-50 border-blue-200 text-blue-800',
      icon: <Info className="w-5 h-5 text-blue-600 flex-shrink-0" />,
    },
  };

  const config = typeConfig[type] || typeConfig.info;

  return (
    <div className={`flex items-start gap-3 p-4 rounded-xl border shadow-lg max-w-md w-full transition-all transform animate-in slide-in-from-top-2 ${config.bg}`}>
      {config.icon}
      <p className="text-sm font-medium flex-1 pt-0.5 leading-snug">{message}</p>
      <button
        onClick={() => onClose(id)}
        className="text-slate-400 hover:text-slate-600 p-1 rounded-md transition-colors"
        aria-label="Close notification"
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  );
}
