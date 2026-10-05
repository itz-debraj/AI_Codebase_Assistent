import React, { useState } from 'react';
import { User, Cpu, Copy, Check, BookOpen } from 'lucide-react';
import type { ChatMessage, SourceReference } from '../../types';
import { SourceReferenceChip } from './SourceReferenceChip';

interface ChatMessageItemProps {
  message: ChatMessage;
  onSourceClick: (source: SourceReference) => void;
}

export const ChatMessageItem: React.FC<ChatMessageItemProps> = ({ message, onSourceClick }) => {
  const isUser = message.role === 'user';
  const [copied, setCopied] = useState(false);

  const handleCopyMessage = () => {
    navigator.clipboard.writeText(message.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Render markdown-like sections (code blocks and text)
  const renderFormattedContent = (text: string) => {
    const parts = text.split(/(```[\s\S]*?```)/g);

    return parts.map((part, index) => {
      if (part.startsWith('```') && part.endsWith('```')) {
        const firstLineEnd = part.indexOf('\n');
        const language = firstLineEnd > 3 ? part.substring(3, firstLineEnd).trim() : '';
        const code = firstLineEnd !== -1 ? part.substring(firstLineEnd + 1, part.length - 3) : part.substring(3, part.length - 3);

        return (
          <div key={index} className="my-3 rounded-lg overflow-hidden border border-slate-800 bg-[#070b12]">
            <div className="flex items-center justify-between px-3 py-1.5 bg-slate-900/80 border-b border-slate-800 text-xs font-mono text-slate-400">
              <span>{language || 'code'}</span>
              <button
                onClick={() => navigator.clipboard.writeText(code)}
                className="hover:text-white transition-colors flex items-center gap-1"
                title="Copy code block"
              >
                <Copy className="w-3 h-3" />
                <span>Copy</span>
              </button>
            </div>
            <pre className="p-3 text-xs font-mono text-slate-200 overflow-x-auto leading-relaxed">
              <code>{code}</code>
            </pre>
          </div>
        );
      }

      // Regular markdown paragraphs
      return (
        <div key={index} className="whitespace-pre-wrap leading-relaxed text-sm text-slate-200">
          {part.split('\n\n').map((paragraph, pIdx) => (
            <p key={pIdx} className="mb-2 last:mb-0">
              {paragraph}
            </p>
          ))}
        </div>
      );
    });
  };

  return (
    <div className={`flex gap-3 p-4 rounded-xl ${isUser ? 'bg-blue-600/10 border border-blue-500/20' : 'bg-slate-900/40 border border-slate-800/80'}`}>
      <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${isUser ? 'bg-blue-600 text-white' : 'bg-slate-800 text-cyan-400 border border-slate-700'}`}>
        {isUser ? <User className="w-4 h-4" /> : <Cpu className="w-4 h-4" />}
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-1.5">
          <span className="text-xs font-semibold text-slate-300">
            {isUser ? 'You' : 'DevPilot Assistant'}
          </span>
          <button
            onClick={handleCopyMessage}
            className="p-1 rounded text-slate-500 hover:text-slate-300 transition-colors"
            title="Copy message"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
          </button>
        </div>

        <div className="text-sm">
          {renderFormattedContent(message.content)}
        </div>

        {/* Source References */}
        {message.sources && message.sources.length > 0 && (
          <div className="mt-4 pt-3 border-t border-slate-800">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-400 mb-2">
              <BookOpen className="w-3.5 h-3.5 text-blue-400" />
              <span>Referenced Sources ({message.sources.length})</span>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {message.sources.map((src, idx) => (
                <SourceReferenceChip
                  key={idx}
                  source={src}
                  onClick={() => onSourceClick(src)}
                />
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
