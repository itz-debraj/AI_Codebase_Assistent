import React from 'react';
import { Loader2, CheckCircle2, AlertTriangle, Clock } from 'lucide-react';
import type { RepositoryStatus } from '../types';

interface StatusBadgeProps {
  status: RepositoryStatus;
  className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, className = '' }) => {
  switch (status) {
    case 'INDEXED':
      return (
        <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 ${className}`}>
          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
          Indexed
        </span>
      );
    case 'PROCESSING':
      return (
        <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-500/10 text-amber-400 border border-amber-500/20 animate-pulse ${className}`}>
          <Loader2 className="w-3.5 h-3.5 text-amber-400 animate-spin" />
          Processing
        </span>
      );
    case 'UPLOADED':
      return (
        <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-500/10 text-blue-400 border border-blue-500/20 ${className}`}>
          <Clock className="w-3.5 h-3.5 text-blue-400" />
          Uploaded
        </span>
      );
    case 'FAILED':
      return (
        <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-rose-500/10 text-rose-400 border border-rose-500/20 ${className}`}>
          <AlertTriangle className="w-3.5 h-3.5 text-rose-400" />
          Failed
        </span>
      );
    default:
      return null;
  }
};
