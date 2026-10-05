import React, { useState, useRef } from 'react';
import { Upload, X, FileArchive, AlertCircle, Loader2 } from 'lucide-react';
import { useUploadRepositoryMutation } from '../api/apiSlice';

interface RepositoryUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (repoId: string) => void;
}

export const RepositoryUploadModal: React.FC<RepositoryUploadModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [file, setFile] = useState<File | null>(null);
  const [repoName, setRepoName] = useState('');
  const [dragActive, setDragActive] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploadRepository, { isLoading }] = useUploadRepositoryMutation();

  if (!isOpen) return null;

  const handleFileSelect = (selectedFile: File) => {
    if (!selectedFile.name.toLowerCase().endsWith('.zip')) {
      setErrorMessage('Only ZIP archives (.zip) are supported.');
      return;
    }
    setFile(selectedFile);
    setErrorMessage(null);

    // Auto-fill repository name if blank
    if (!repoName) {
      const cleanName = selectedFile.name.replace(/\.zip$/i, '');
      setRepoName(cleanName);
    }
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelect(e.dataTransfer.files[0]);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) {
      setErrorMessage('Please select a ZIP file to upload.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    if (repoName.trim()) {
      formData.append('name', repoName.trim());
    }

    try {
      const response = await uploadRepository(formData).unwrap();
      onSuccess(response.id);
      onClose();
    } catch (err: any) {
      setErrorMessage(err?.data?.message || err?.message || 'Failed to upload repository.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-950 border border-slate-800 rounded-2xl w-full max-w-lg shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/50">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-blue-600/10 text-blue-400 flex items-center justify-center">
              <Upload className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-white">Upload Repository</h3>
              <p className="text-xs text-slate-400">Import a codebase ZIP for indexing and RAG</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {errorMessage && (
            <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Drag & Drop Zone */}
          <div
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-xl p-6 text-center cursor-pointer transition-all ${
              dragActive
                ? 'border-blue-500 bg-blue-500/5'
                : file
                ? 'border-emerald-500/40 bg-emerald-500/5'
                : 'border-slate-800 hover:border-slate-700 bg-slate-900/40'
            }`}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".zip"
              className="hidden"
              onChange={(e) => {
                if (e.target.files && e.target.files[0]) {
                  handleFileSelect(e.target.files[0]);
                }
              }}
            />

            {file ? (
              <div className="flex flex-col items-center">
                <FileArchive className="w-10 h-10 text-emerald-400 mb-2" />
                <span className="text-sm font-medium text-slate-200">{file.name}</span>
                <span className="text-xs text-slate-500 mt-1">
                  {(file.size / (1024 * 1024)).toFixed(2)} MB • Click or drag to change
                </span>
              </div>
            ) : (
              <div className="flex flex-col items-center">
                <Upload className="w-10 h-10 text-slate-500 mb-2" />
                <span className="text-sm font-medium text-slate-300">
                  Drop your repository ZIP here, or <span className="text-blue-400">browse</span>
                </span>
                <span className="text-xs text-slate-500 mt-1">
                  Max file size: 50MB (Standard Maven/Gradle/Node/Python projects)
                </span>
              </div>
            )}
          </div>

          {/* Repository Name Input */}
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Repository Name
            </label>
            <input
              type="text"
              value={repoName}
              onChange={(e) => setRepoName(e.target.value)}
              placeholder="e.g. spring-petclinic"
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-800 rounded-lg text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/20"
            />
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-lg text-xs font-medium text-slate-400 hover:text-white hover:bg-slate-900 border border-transparent transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!file || isLoading}
              className="px-4 py-2 rounded-lg text-xs font-medium bg-blue-600 hover:bg-blue-500 disabled:opacity-40 text-white flex items-center gap-1.5 transition-all shadow-lg shadow-blue-500/20"
            >
              {isLoading ? (
                <>
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  Uploading & Starting Index...
                </>
              ) : (
                'Upload & Index'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
