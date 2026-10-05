import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type {
  Repository,
  RepositoryStatusResponse,
  RepositoryUploadResponse,
  ChatResponse,
  ChatSession,
  ChatMessage,
  CodeFile,
  CodeFileContent,
} from '../../types';

export const apiSlice = createApi({
  reducerPath: 'api',
  baseQuery: fetchBaseQuery({ baseUrl: '/api/v1' }),
  tagTypes: ['Repositories', 'RepositoryStatus', 'Files', 'ChatSessions', 'ChatMessages'],
  endpoints: (builder) => ({
    // Repositories
    getRepositories: builder.query<Repository[], void>({
      query: () => '/repositories',
      providesTags: ['Repositories'],
    }),

    getRepositoryById: builder.query<Repository, string>({
      query: (id) => `/repositories/${id}`,
      providesTags: (_res, _err, id) => [{ type: 'Repositories', id }],
    }),

    getRepositoryStatus: builder.query<RepositoryStatusResponse, string>({
      query: (id) => `/repositories/${id}/status`,
      providesTags: (_res, _err, id) => [{ type: 'RepositoryStatus', id }],
    }),

    uploadRepository: builder.mutation<RepositoryUploadResponse, FormData>({
      query: (formData) => ({
        url: '/repositories/upload',
        method: 'POST',
        body: formData,
      }),
      invalidatesTags: ['Repositories'],
    }),

    // Files
    getRepositoryFiles: builder.query<CodeFile[], string>({
      query: (repoId) => `/repositories/${repoId}/files`,
      providesTags: (_res, _err, repoId) => [{ type: 'Files', id: repoId }],
    }),

    getFileContent: builder.query<CodeFileContent, { repoId: string; fileId: string }>({
      query: ({ repoId, fileId }) => `/repositories/${repoId}/files/${fileId}`,
    }),

    // Chat
    sendChatMessage: builder.mutation<ChatResponse, { repoId: string; message: string; sessionId?: string }>({
      query: ({ repoId, message, sessionId }) => ({
        url: `/repositories/${repoId}/chat`,
        method: 'POST',
        body: { message, sessionId },
      }),
      invalidatesTags: (_res, _err, { repoId, sessionId }) => [
        { type: 'ChatSessions', id: repoId },
        ...(sessionId ? [{ type: 'ChatMessages' as const, id: sessionId }] : []),
      ],
    }),

    getChatSessions: builder.query<ChatSession[], string>({
      query: (repoId) => `/repositories/${repoId}/chat/sessions`,
      providesTags: (_res, _err, repoId) => [{ type: 'ChatSessions', id: repoId }],
    }),

    getSessionMessages: builder.query<ChatMessage[], { repoId: string; sessionId: string }>({
      query: ({ repoId, sessionId }) => `/repositories/${repoId}/chat/sessions/${sessionId}`,
      providesTags: (_res, _err, { sessionId }) => [{ type: 'ChatMessages', id: sessionId }],
    }),
  }),
});

export const {
  useGetRepositoriesQuery,
  useGetRepositoryByIdQuery,
  useGetRepositoryStatusQuery,
  useUploadRepositoryMutation,
  useGetRepositoryFilesQuery,
  useGetFileContentQuery,
  useSendChatMessageMutation,
  useGetChatSessionsQuery,
  useGetSessionMessagesQuery,
} = apiSlice;
