import { useState } from 'react';
import { X, Copy, Check, FileCode } from 'lucide-react';
import { useGetFileContentQuery } from '../api/apiSlice';

interface SourceViewerModalProps {
  repoId: string;
  fileId?: string;
  filePath?: string;
  startLine?: number;
  endLine?: number;
  snippet?: string;
  onClose: () => void;
}

export const SourceViewerModal: React.FC<SourceViewerModalProps> = ({
  repoId,
  fileId,
  filePath,
  startLine,
  endLine,
  snippet,
  onClose,
}) => {
  const [copied, setCopied] = useState(false);

  // If fileId is present, query full content from backend
  const { data: fileData, isLoading } = useGetFileContentQuery(
    { repoId, fileId: fileId! },
    { skip: !fileId }
  );

  const displayCode = fileData?.content || snippet || '// Content loading or unavailable';
  const displayPath = fileData?.filePath || filePath || 'Source Code';
  const displayLang = fileData?.language || 'text';

  const lines = displayCode.split('\n');

  const handleCopy = () => {
    navigator.clipboard.writeText(displayCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-950 border border-slate-800 rounded-xl w-full max-w-4xl max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-slate-800 bg-slate-900/60">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <FileCode className="w-4 h-4 text-blue-400 shrink-0" />
            <div className="flex items-center gap-2 truncate">
              <span className="font-mono text-sm font-medium text-slate-200 truncate">{displayPath}</span>
              {startLine && endLine && (
                <span className="text-xs px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 font-mono shrink-0">
                  L{startLine}-{endLine}
                </span>
              )}
              <span className="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-400 font-mono shrink-0">
                {displayLang}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleCopy}
              className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors flex items-center gap-1.5 text-xs"
              title="Copy code"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? 'Copied' : 'Copy'}</span>
            </button>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
              title="Close viewer"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Code Content */}
        <div className="flex-1 overflow-auto p-4 bg-[#080c14] font-mono text-xs leading-relaxed">
          {isLoading ? (
            <div className="h-64 flex items-center justify-center text-slate-500">
              Loading source file...
            </div>
          ) : (
            <table className="w-full border-collapse">
              <tbody>
                {lines.map((line, idx) => {
                  const lineNum = (startLine ? startLine + idx : idx + 1);
                  const isHighlighted = startLine && endLine && lineNum >= startLine && lineNum <= endLine;
                  return (
                    <tr
                      key={idx}
                      className={isHighlighted ? 'bg-blue-950/40 text-blue-100' : 'hover:bg-slate-900/40'}
                    >
                      <td className="w-12 pr-4 text-right select-none text-slate-600 font-mono border-r border-slate-800/80">
                        {lineNum}
                      </td>
                      <td className="pl-4 whitespace-pre text-slate-300 font-mono">
                        {line || ' '}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};
