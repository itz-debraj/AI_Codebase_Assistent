import React from 'react';
import { Terminal, Cpu, FolderGit2 } from 'lucide-react';

interface NavbarProps {
  currentView: 'dashboard' | 'chat';
  onNavigateHome: () => void;
  activeRepoName?: string;
}

export const Navbar: React.FC<NavbarProps> = ({ currentView, onNavigateHome, activeRepoName }) => {
  return (
    <header className="h-14 border-b border-slate-800 bg-slate-950/80 backdrop-blur-md px-4 flex items-center justify-between sticky top-0 z-30">
      <div className="flex items-center gap-3">
        <button
          onClick={onNavigateHome}
          className="flex items-center gap-2.5 text-left group hover:opacity-90 transition-opacity focus:outline-none"
        >
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-600 via-indigo-600 to-cyan-500 flex items-center justify-center shadow-lg shadow-blue-500/20 group-hover:scale-105 transition-transform">
            <Cpu className="w-4 h-4 text-white" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-bold text-sm tracking-tight text-white">DevPilot</span>
              <span className="text-[10px] uppercase font-semibold tracking-wider px-1.5 py-0.2 bg-blue-500/10 text-blue-400 border border-blue-500/20 rounded">
                RAG v1.0
              </span>
            </div>
            <p className="text-[11px] text-slate-400">AI Codebase Assistant</p>
          </div>
        </button>

        {currentView === 'chat' && activeRepoName && (
          <div className="flex items-center gap-2 ml-4 pl-4 border-l border-slate-800 text-xs">
            <span className="text-slate-500">Repository:</span>
            <span className="font-mono font-medium text-slate-200 bg-slate-900 px-2 py-0.5 rounded border border-slate-800 flex items-center gap-1.5">
              <FolderGit2 className="w-3.5 h-3.5 text-blue-400" />
              {activeRepoName}
            </span>
          </div>
        )}
      </div>

      <div className="flex items-center gap-3">
        {currentView === 'chat' && (
          <button
            onClick={onNavigateHome}
            className="text-xs px-3 py-1.5 rounded-md bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-800 transition-colors flex items-center gap-1.5"
          >
            <Terminal className="w-3.5 h-3.5 text-slate-400" />
            All Repositories
          </button>
        )}
        <div className="flex items-center gap-1.5 text-[11px] text-slate-400 bg-slate-900/60 border border-slate-800/80 px-2.5 py-1 rounded-full">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
          <span>Spring AI + pgvector</span>
        </div>
      </div>
    </header>
  );
};
