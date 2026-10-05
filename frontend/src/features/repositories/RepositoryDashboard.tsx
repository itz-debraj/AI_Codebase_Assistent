import React, { useState } from 'react';
import {
  Upload,
  Search,
  FolderGit2,
  FileCode,
  Layers,
  RefreshCw,
  Plus,
} from 'lucide-react';
import { useGetRepositoriesQuery } from '../api/apiSlice';
import { RepositoryCard } from './RepositoryCard';
import { RepositoryUploadModal } from './RepositoryUploadModal';

interface RepositoryDashboardProps {
  onOpenAssistant: (repoId: string) => void;
}

export const RepositoryDashboard: React.FC<RepositoryDashboardProps> = ({ onOpenAssistant }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [uploadModalOpen, setUploadModalOpen] = useState(false);

  const { data: repositories = [], isLoading, refetch } = useGetRepositoriesQuery();

  const filteredRepositories = repositories.filter((repo) =>
    repo.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    repo.originalFileName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const totalFiles = repositories.reduce((sum, r) => sum + (r.totalFiles || 0), 0);
  const totalChunks = repositories.reduce((sum, r) => sum + (r.totalChunks || 0), 0);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-800">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            Codebase Repositories
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Upload and manage software repositories for retrieval-augmented question answering
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => refetch()}
            className="p-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-400 hover:text-white border border-slate-800 transition-colors"
            title="Refresh repositories"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
          <button
            onClick={() => setUploadModalOpen(true)}
            className="px-3.5 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-medium text-xs flex items-center gap-2 shadow-lg shadow-blue-500/20 transition-all hover:scale-[1.02]"
          >
            <Plus className="w-4 h-4" />
            Upload Repository
          </button>
        </div>
      </div>

      {/* Stats Counters */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 my-6">
        <div className="p-4 rounded-2xl bg-slate-900/50 border border-slate-800 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-blue-600/10 text-blue-400 flex items-center justify-center">
            <FolderGit2 className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] text-slate-500 uppercase tracking-wider block">Repositories</span>
            <span className="text-lg font-bold text-white font-mono">{repositories.length}</span>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-900/50 border border-slate-800 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-600/10 text-indigo-400 flex items-center justify-center">
            <FileCode className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] text-slate-500 uppercase tracking-wider block">Indexed Files</span>
            <span className="text-lg font-bold text-white font-mono">{totalFiles}</span>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-900/50 border border-slate-800 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-cyan-600/10 text-cyan-400 flex items-center justify-center">
            <Layers className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[11px] text-slate-500 uppercase tracking-wider block">Vector Chunks</span>
            <span className="text-lg font-bold text-white font-mono">{totalChunks}</span>
          </div>
        </div>
      </div>

      {/* Search Input */}
      <div className="relative mb-6">
        <Search className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
        <input
          type="text"
          placeholder="Search repositories by name or archive..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full pl-9 pr-4 py-2.5 bg-slate-900/60 border border-slate-800 rounded-xl text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/20"
        />
      </div>

      {/* Repository Cards Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-48 rounded-2xl bg-slate-900/40 border border-slate-800/80 animate-pulse" />
          ))}
        </div>
      ) : filteredRepositories.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredRepositories.map((repo) => (
            <RepositoryCard
              key={repo.id}
              repository={repo}
              onOpenAssistant={onOpenAssistant}
            />
          ))}
        </div>
      ) : (
        <div className="border border-dashed border-slate-800 rounded-2xl p-12 text-center bg-slate-950/30">
          <div className="w-12 h-12 rounded-2xl bg-blue-600/10 text-blue-400 border border-blue-500/20 flex items-center justify-center mx-auto mb-4">
            <Upload className="w-6 h-6" />
          </div>
          <h3 className="text-sm font-semibold text-slate-200 mb-1">
            {searchQuery ? 'No matching repositories found' : 'No repositories uploaded yet'}
          </h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mb-4">
            {searchQuery
              ? 'Try adjusting your search query to find the repository.'
              : 'Upload a ZIP file of your codebase to index its source code and start asking questions.'}
          </p>
          {!searchQuery && (
            <button
              onClick={() => setUploadModalOpen(true)}
              className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-medium text-xs inline-flex items-center gap-2 shadow-lg shadow-blue-500/20 transition-all"
            >
              <Upload className="w-4 h-4" />
              Upload First Repository
            </button>
          )}
        </div>
      )}

      {/* Upload Modal */}
      <RepositoryUploadModal
        isOpen={uploadModalOpen}
        onClose={() => setUploadModalOpen(false)}
        onSuccess={() => {
          refetch();
        }}
      />
    </div>
  );
};
