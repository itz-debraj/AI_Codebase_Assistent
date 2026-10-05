export type RepositoryStatus = 'UPLOADED' | 'PROCESSING' | 'INDEXED' | 'FAILED';

export interface Repository {
  id: string;
  name: string;
  originalFileName: string;
  status: RepositoryStatus;
  totalFiles: number;
  processedFiles: number;
  totalChunks: number;
  processedChunks: number;
  errorMessage?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RepositoryStatusResponse {
  id: string;
  name: string;
  status: RepositoryStatus;
  totalFiles: number;
  processedFiles: number;
  totalChunks: number;
  processedChunks: number;
  errorMessage?: string;
}

export interface RepositoryUploadResponse {
  id: string;
  name: string;
  message: string;
}

export interface SourceReference {
  filePath: string;
  fileName: string;
  className?: string;
  methodName?: string;
  chunkId?: string;
  startLine?: number;
  endLine?: number;
  relevanceScore?: number;
  snippet?: string;
}

export interface ChatMessage {
  id: string;
  sessionId?: string;
  role: 'user' | 'assistant';
  content: string;
  sources?: SourceReference[];
  createdAt: string;
}

export interface ChatSession {
  id: string;
  repositoryId: string;
  title: string;
  createdAt: string;
}

export interface ChatResponse {
  sessionId: string;
  answer: string;
  sources: SourceReference[];
}

export interface CodeFile {
  id: string;
  repositoryId: string;
  filePath: string;
  fileName: string;
  extension: string;
  language: string;
  fileSize: number;
  chunkCount: number;
}

export interface CodeFileContent extends CodeFile {
  content: string;
}
