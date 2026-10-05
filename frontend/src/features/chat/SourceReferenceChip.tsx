import React from 'react';
import { FileCode, ChevronRight } from 'lucide-react';
import type { SourceReference } from '../../types';

interface SourceReferenceChipProps {
  source: SourceReference;
  onClick: () => void;
}

export const SourceReferenceChip: React.FC<SourceReferenceChipProps> = ({ source, onClick }) => {
  const lineInfo = source.startLine && source.endLine ? `L${source.startLine}-${source.endLine}` : null;
  const scorePercent = source.relevanceScore != null ? `${Math.round(source.relevanceScore * 100)}%` : null;

  return (
    <button
      onClick={onClick}
      className="group flex items-center gap-2 px-2.5 py-1.5 rounded-lg bg-slate-900/90 hover:bg-slate-800 border border-slate-800 hover:border-blue-500/40 text-left transition-all text-xs"
    >
      <div className="w-5 h-5 rounded bg-blue-500/10 text-blue-400 flex items-center justify-center shrink-0">
        <FileCode className="w-3.5 h-3.5" />
      </div>

      <div className="flex flex-col min-w-0">
        <div className="flex items-center gap-1.5">
          <span className="font-mono text-slate-200 font-medium truncate max-w-[180px] group-hover:text-blue-400">
            {source.fileName}
          </span>
          {lineInfo && (
            <span className="font-mono text-[10px] text-slate-500 bg-slate-800/80 px-1 rounded">
              {lineInfo}
            </span>
          )}
        </div>

        {(source.className || source.methodName) && (
          <span className="text-[11px] text-slate-400 truncate max-w-[200px]">
            {source.className}{source.className && source.methodName ? '::' : ''}{source.methodName}
          </span>
        )}
      </div>

      {scorePercent && (
        <span className="ml-auto text-[10px] text-emerald-400 font-mono bg-emerald-500/10 border border-emerald-500/20 px-1.5 py-0.5 rounded">
          {scorePercent}
        </span>
      )}

      <ChevronRight className="w-3.5 h-3.5 text-slate-600 group-hover:text-slate-400 group-hover:translate-x-0.5 transition-transform" />
    </button>
  );
};
