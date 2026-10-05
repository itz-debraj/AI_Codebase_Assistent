import React from 'react';
import { FolderGit2, FileText, Layers, ArrowRight, Clock, AlertCircle } from 'lucide-react';
import type { Repository } from '../../types';
import { StatusBadge } from '../../components/StatusBadge';
import { useGetRepositoryStatusQuery } from '../api/apiSlice';

interface RepositoryCardProps {
  repository: Repository;
  onOpenAssistant: (repoId: string) => void;
}

export const RepositoryCard: React.FC<RepositoryCardProps> = ({ repository, onOpenAssistant }) => {
  // If the repository is currently PROCESSING or UPLOADED, poll status every 3 seconds
  const shouldPoll = repository.status === 'PROCESSING' || repository.status === 'UPLOADED';
  const { data: liveStatus } = useGetRepositoryStatusQuery(repository.id, {
    pollingInterval: shouldPoll ? 3000 : 0,
    skip: !shouldPoll,
  });

  const currentStatus = liveStatus?.status || repository.status;
  const currentTotalFiles = liveStatus?.totalFiles || repository.totalFiles;
  const currentProcessedFiles = liveStatus?.processedFiles || repository.processedFiles;
  const currentTotalChunks = liveStatus?.totalChunks || repository.totalChunks;

  const progressPercent = currentTotalFiles > 0
    ? Math.min(100, Math.round((currentProcessedFiles / currentTotalFiles) * 100))
    : 0;

  const isIndexed = currentStatus === 'INDEXED';
  const isProcessing = currentStatus === 'PROCESSING';

  const formattedDate = new Date(repository.updatedAt || repository.createdAt).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className="bg-slate-900/60 hover:bg-slate-900/90 border border-slate-800 hover:border-slate-700/80 rounded-2xl p-5 flex flex-col justify-between transition-all group shadow-sm hover:shadow-xl hover:shadow-black/40">
      <div>
        {/* Top Header */}
        <div className="flex items-start justify-between gap-3 mb-3">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <div className="w-9 h-9 rounded-xl bg-blue-600/10 text-blue-400 border border-blue-500/20 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform">
              <FolderGit2 className="w-5 h-5" />
            </div>
            <div className="truncate">
              <h3 className="text-sm font-semibold text-white truncate group-hover:text-blue-400 transition-colors">
                {repository.name}
              </h3>
              <span className="text-[11px] font-mono text-slate-500 truncate block">
                {repository.originalFileName}
              </span>
            </div>
          </div>
          <StatusBadge status={currentStatus} />
        </div>

        {/* Processing Progress Bar */}
        {isProcessing && (
          <div className="my-3 p-3 rounded-xl bg-amber-500/5 border border-amber-500/20">
            <div className="flex justify-between text-[11px] font-medium text-amber-400 mb-1.5">
              <span>Indexing codebase files...</span>
              <span>{progressPercent}%</span>
            </div>
            <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
              <div
                className="bg-amber-400 h-1.5 rounded-full transition-all duration-500 ease-out"
                style={{ width: `${progressPercent}%` }}
              />
            </div>
            <div className="flex justify-between text-[10px] text-slate-500 mt-1.5">
              <span>{currentProcessedFiles} of {currentTotalFiles} files</span>
              <span>{currentTotalChunks} chunks generated</span>
            </div>
          </div>
        )}

        {/* Error Notice */}
        {currentStatus === 'FAILED' && repository.errorMessage && (
          <div className="my-3 p-2.5 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span className="truncate">{repository.errorMessage}</span>
          </div>
        )}

        {/* Metadata Stats */}
        <div className="grid grid-cols-2 gap-2 my-4">
          <div className="bg-slate-950/60 p-2.5 rounded-xl border border-slate-800/60 flex items-center gap-2.5">
            <FileText className="w-4 h-4 text-slate-500" />
            <div>
              <span className="text-[10px] text-slate-500 block uppercase tracking-wider">Source Files</span>
              <span className="text-xs font-semibold text-slate-200 font-mono">{currentTotalFiles}</span>
            </div>
          </div>

          <div className="bg-slate-950/60 p-2.5 rounded-xl border border-slate-800/60 flex items-center gap-2.5">
            <Layers className="w-4 h-4 text-slate-500" />
            <div>
              <span className="text-[10px] text-slate-500 block uppercase tracking-wider">Code Chunks</span>
              <span className="text-xs font-semibold text-slate-200 font-mono">{currentTotalChunks}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Footer & CTA */}
      <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs">
        <span className="text-[11px] text-slate-500 flex items-center gap-1">
          <Clock className="w-3 h-3" />
          {formattedDate}
        </span>

        <button
          onClick={() => onOpenAssistant(repository.id)}
          disabled={!isIndexed}
          className={`px-3 py-1.5 rounded-lg font-medium flex items-center gap-1.5 transition-all text-xs ${
            isIndexed
              ? 'bg-blue-600 hover:bg-blue-500 text-white shadow-md shadow-blue-500/20'
              : 'bg-slate-800 text-slate-500 cursor-not-allowed'
          }`}
        >
          <span>AI Assistant</span>
          <ArrowRight className="w-3 h-3" />
        </button>
      </div>
    </div>
  );
};
