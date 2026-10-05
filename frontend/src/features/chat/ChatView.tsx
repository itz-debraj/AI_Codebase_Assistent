import React, { useState, useRef, useEffect } from 'react';
import {
  Send,
  Loader2,
  FolderGit2,
  FileCode,
  Search,
  Plus,
  MessageSquare,
  Sparkles,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import {
  useGetRepositoryByIdQuery,
  useGetRepositoryFilesQuery,
  useGetChatSessionsQuery,
  useGetSessionMessagesQuery,
  useSendChatMessageMutation,
} from '../api/apiSlice';
import type { ChatMessage } from '../../types';
import { ChatMessageItem } from './ChatMessageItem';
import { SourceViewerModal } from '../sourceViewer/SourceViewerModal';
import { StatusBadge } from '../../components/StatusBadge';

interface ChatViewProps {
  repoId: string;
  onBack: () => void;
}

const SUGGESTED_QUESTIONS = [
  'How is authentication implemented?',
  'Explain the overall project architecture',
  'Where and how are exceptions handled?',
  'What database entities and relations exist?',
  'How does the code indexing pipeline work?',
];

export const ChatView: React.FC<ChatViewProps> = ({ repoId, onBack }) => {
  const [activeSessionId, setActiveSessionId] = useState<string | undefined>(undefined);
  const [inputMessage, setInputMessage] = useState('');
  const [fileSearchQuery, setFileSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<'files' | 'sessions'>('files');
  const [sidebarOpen, setSidebarOpen] = useState(true);

  // Selected file or source for code viewer
  const [selectedFileForViewer, setSelectedFileForViewer] = useState<{
    fileId?: string;
    filePath?: string;
    startLine?: number;
    endLine?: number;
    snippet?: string;
  } | null>(null);

  // Local optimistic messages
  const [localMessages, setLocalMessages] = useState<ChatMessage[]>([]);

  // Backend queries
  const { data: repository } = useGetRepositoryByIdQuery(repoId);
  const { data: files = [] } = useGetRepositoryFilesQuery(repoId);
  const { data: sessions = [] } = useGetChatSessionsQuery(repoId);

  const { data: sessionHistory = [] } = useGetSessionMessagesQuery(
    { repoId, sessionId: activeSessionId! },
    { skip: !activeSessionId }
  );

  const [sendChatMessage, { isLoading: isSending }] = useSendChatMessageMutation();

  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Update messages when session history changes
  useEffect(() => {
    if (sessionHistory.length > 0) {
      setLocalMessages(sessionHistory);
    }
  }, [sessionHistory]);

  // Scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [localMessages, isSending]);

  const handleSendMessage = async (textToSend?: string) => {
    const text = (textToSend || inputMessage).trim();
    if (!text || isSending) return;

    setInputMessage('');

    // Add optimistic user message
    const tempUserMsg: ChatMessage = {
      id: 'temp-' + Date.now(),
      role: 'user',
      content: text,
      createdAt: new Date().toISOString(),
    };
    setLocalMessages((prev) => [...prev, tempUserMsg]);

    try {
      const response = await sendChatMessage({
        repoId,
        message: text,
        sessionId: activeSessionId,
      }).unwrap();

      if (!activeSessionId && response.sessionId) {
        setActiveSessionId(response.sessionId);
      }

      const assistantMsg: ChatMessage = {
        id: 'resp-' + Date.now(),
        sessionId: response.sessionId,
        role: 'assistant',
        content: response.answer,
        sources: response.sources,
        createdAt: new Date().toISOString(),
      };
      setLocalMessages((prev) => [...prev, assistantMsg]);
    } catch (err: any) {
      const errorMsg: ChatMessage = {
        id: 'err-' + Date.now(),
        role: 'assistant',
        content: `Error generating response: ${err?.data?.message || err?.message || 'Network error'}`,
        createdAt: new Date().toISOString(),
      };
      setLocalMessages((prev) => [...prev, errorMsg]);
    }
  };

  const handleStartNewSession = () => {
    setActiveSessionId(undefined);
    setLocalMessages([]);
  };

  const filteredFiles = files.filter((f) =>
    f.filePath.toLowerCase().includes(fileSearchQuery.toLowerCase())
  );

  return (
    <div className="flex h-[calc(100vh-3.5rem)] overflow-hidden bg-[#070b12]">
      {/* Left Sidebar */}
      <aside
        className={`${
          sidebarOpen ? 'w-80' : 'w-0'
        } transition-all duration-300 border-r border-slate-800/80 bg-slate-950/60 flex flex-col shrink-0 overflow-hidden relative`}
      >
        {/* Repo Header Card */}
        <div className="p-4 border-b border-slate-800">
          <div className="flex items-center justify-between mb-2">
            <button
              onClick={onBack}
              className="text-xs text-slate-400 hover:text-white flex items-center gap-1 transition-colors"
            >
              <ChevronLeft className="w-3.5 h-3.5" /> Back to Dashboard
            </button>
            {repository && <StatusBadge status={repository.status} />}
          </div>

          <h2 className="font-semibold text-slate-100 text-sm truncate flex items-center gap-2">
            <FolderGit2 className="w-4 h-4 text-blue-400 shrink-0" />
            {repository?.name || 'Repository'}
          </h2>

          <div className="grid grid-cols-2 gap-2 mt-3 pt-3 border-t border-slate-800/60 text-xs">
            <div className="bg-slate-900/60 p-2 rounded border border-slate-800/60">
              <span className="text-slate-500 block text-[10px]">Files</span>
              <span className="font-mono font-medium text-slate-200">
                {repository?.totalFiles || files.length}
              </span>
            </div>
            <div className="bg-slate-900/60 p-2 rounded border border-slate-800/60">
              <span className="text-slate-500 block text-[10px]">Chunks</span>
              <span className="font-mono font-medium text-slate-200">
                {repository?.totalChunks || 0}
              </span>
            </div>
          </div>
        </div>

        {/* Tab Switcher */}
        <div className="flex border-b border-slate-800 text-xs font-medium">
          <button
            onClick={() => setActiveTab('files')}
            className={`flex-1 py-2.5 flex items-center justify-center gap-1.5 transition-colors ${
              activeTab === 'files'
                ? 'text-blue-400 border-b-2 border-blue-500 bg-slate-900/40'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <FileCode className="w-3.5 h-3.5" />
            Files ({files.length})
          </button>
          <button
            onClick={() => setActiveTab('sessions')}
            className={`flex-1 py-2.5 flex items-center justify-center gap-1.5 transition-colors ${
              activeTab === 'sessions'
                ? 'text-blue-400 border-b-2 border-blue-500 bg-slate-900/40'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <MessageSquare className="w-3.5 h-3.5" />
            History ({sessions.length})
          </button>
        </div>

        {/* Tab Content */}
        <div className="flex-1 overflow-y-auto p-3">
          {activeTab === 'files' ? (
            <div className="space-y-2">
              <div className="relative">
                <Search className="w-3.5 h-3.5 text-slate-500 absolute left-2.5 top-2.5" />
                <input
                  type="text"
                  placeholder="Filter files..."
                  value={fileSearchQuery}
                  onChange={(e) => setFileSearchQuery(e.target.value)}
                  className="w-full pl-8 pr-3 py-1.5 bg-slate-900 border border-slate-800 rounded-lg text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="space-y-1 mt-2">
                {filteredFiles.map((file) => (
                  <button
                    key={file.id}
                    onClick={() =>
                      setSelectedFileForViewer({
                        fileId: file.id,
                        filePath: file.filePath,
                      })
                    }
                    className="w-full text-left px-2.5 py-1.5 rounded-md hover:bg-slate-900 text-xs text-slate-300 hover:text-white transition-colors flex items-center justify-between group"
                  >
                    <span className="truncate font-mono text-[11px] group-hover:text-blue-400">
                      {file.filePath}
                    </span>
                    <span className="text-[10px] text-slate-500 font-mono shrink-0 ml-1">
                      {file.chunkCount} ch
                    </span>
                  </button>
                ))}
                {filteredFiles.length === 0 && (
                  <p className="text-xs text-slate-500 text-center py-4">No files found</p>
                )}
              </div>
            </div>
          ) : (
            <div className="space-y-2">
              <button
                onClick={handleStartNewSession}
                className="w-full py-2 px-3 rounded-lg bg-blue-600/10 hover:bg-blue-600/20 text-blue-400 border border-blue-500/20 text-xs font-medium flex items-center justify-center gap-1.5 transition-colors"
              >
                <Plus className="w-3.5 h-3.5" /> New Chat Session
              </button>

              <div className="space-y-1 mt-3">
                {sessions.map((session) => (
                  <button
                    key={session.id}
                    onClick={() => setActiveSessionId(session.id)}
                    className={`w-full text-left p-2 rounded-lg text-xs transition-colors flex items-center gap-2 ${
                      activeSessionId === session.id
                        ? 'bg-slate-800 text-white font-medium border border-slate-700'
                        : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                    }`}
                  >
                    <MessageSquare className="w-3.5 h-3.5 shrink-0 text-slate-500" />
                    <span className="truncate">{session.title}</span>
                  </button>
                ))}
                {sessions.length === 0 && (
                  <p className="text-xs text-slate-500 text-center py-4">No previous sessions</p>
                )}
              </div>
            </div>
          )}
        </div>
      </aside>

      {/* Main Chat Pane */}
      <main className="flex-1 flex flex-col min-w-0 bg-[#090d16] relative">
        {/* Toggle Sidebar Button */}
        <button
          onClick={() => setSidebarOpen(!sidebarOpen)}
          className="absolute left-2 top-2 z-10 p-1.5 rounded-md bg-slate-900/80 hover:bg-slate-800 border border-slate-800 text-slate-400 hover:text-white transition-colors"
          title={sidebarOpen ? 'Collapse sidebar' : 'Expand sidebar'}
        >
          {sidebarOpen ? <ChevronLeft className="w-3.5 h-3.5" /> : <ChevronRight className="w-3.5 h-3.5" />}
        </button>

        {/* Message Stream */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 max-w-4xl mx-auto w-full">
          {localMessages.length === 0 && (
            <div className="h-full flex flex-col items-center justify-center text-center py-12 px-4">
              <div className="w-12 h-12 rounded-2xl bg-blue-600/10 text-blue-400 border border-blue-500/20 flex items-center justify-center mb-4 shadow-lg shadow-blue-500/10">
                <Sparkles className="w-6 h-6" />
              </div>
              <h3 className="text-base font-semibold text-slate-100 mb-1">
                Ask anything about {repository?.name || 'this codebase'}
              </h3>
              <p className="text-xs text-slate-400 max-w-md mb-6 leading-relaxed">
                DevPilot retrieves relevant source chunks using pgvector semantic search and synthesizes answers with file and line references.
              </p>

              {/* Suggested Questions */}
              <div className="w-full max-w-lg space-y-2">
                <span className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider block mb-2">
                  Suggested Questions
                </span>
                <div className="flex flex-wrap gap-2 justify-center">
                  {SUGGESTED_QUESTIONS.map((q, idx) => (
                    <button
                      key={idx}
                      onClick={() => handleSendMessage(q)}
                      className="text-xs px-3 py-1.5 rounded-lg bg-slate-900/90 hover:bg-slate-800 text-slate-300 hover:text-white border border-slate-800 transition-all text-left"
                    >
                      {q}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {localMessages.map((msg) => (
            <ChatMessageItem
              key={msg.id}
              message={msg}
              onSourceClick={(src) =>
                setSelectedFileForViewer({
                  filePath: src.filePath,
                  startLine: src.startLine,
                  endLine: src.endLine,
                  snippet: src.snippet,
                })
              }
            />
          ))}

          {isSending && (
            <div className="flex items-center gap-3 p-4 rounded-xl bg-slate-900/40 border border-slate-800/80 animate-pulse">
              <Loader2 className="w-5 h-5 text-blue-400 animate-spin" />
              <div className="text-xs text-slate-400">
                Searching codebase and generating grounded response...
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Input Bar */}
        <div className="p-4 border-t border-slate-800/80 bg-slate-950/80 backdrop-blur-md">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSendMessage();
            }}
            className="max-w-4xl mx-auto flex items-end gap-2"
          >
            <div className="flex-1 relative bg-slate-900 border border-slate-800 rounded-xl focus-within:border-blue-500 focus-within:ring-1 focus-within:ring-blue-500/20 transition-all">
              <textarea
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    handleSendMessage();
                  }
                }}
                rows={2}
                placeholder="Ask a technical question about this repository (Shift+Enter for newline)..."
                className="w-full bg-transparent px-3.5 py-2.5 text-xs text-slate-200 placeholder-slate-500 resize-none focus:outline-none"
              />
            </div>

            <button
              type="submit"
              disabled={!inputMessage.trim() || isSending}
              className="p-3 rounded-xl bg-blue-600 hover:bg-blue-500 disabled:opacity-40 disabled:hover:bg-blue-600 text-white shadow-lg shadow-blue-500/20 transition-all shrink-0"
              title="Send message"
            >
              {isSending ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Send className="w-4 h-4" />
              )}
            </button>
          </form>
        </div>
      </main>

      {/* Source Viewer Modal */}
      {selectedFileForViewer && (
        <SourceViewerModal
          repoId={repoId}
          fileId={selectedFileForViewer.fileId}
          filePath={selectedFileForViewer.filePath}
          startLine={selectedFileForViewer.startLine}
          endLine={selectedFileForViewer.endLine}
          snippet={selectedFileForViewer.snippet}
          onClose={() => setSelectedFileForViewer(null)}
        />
      )}
    </div>
  );
};
