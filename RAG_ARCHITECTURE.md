# DevPilot RAG Architecture & Data Flow

This document details the complete end-to-end architecture of DevPilot, covering the **Code Indexing Pipeline**, **Vector Storage Engine (pgvector)**, and the **RAG Retrieval & Generation Flow**.

---

## 1. High-Level System Architecture

```mermaid
graph TB
    subgraph Client["Frontend (React 19 + TypeScript + Tailwind)"]
        UI[User Interface & Chat View]
        UploadModal[Repository Upload Modal]
        SourceViewer[Source Code Modal]
    end

    subgraph Backend["Spring Boot 4.x Application"]
        RepoCtrl[RepositoryController]
        ChatCtrl[ChatController]
        IndexSvc[CodeIndexingService]
        FilterSvc[FileFilterService]
        ChunkSvc[CodeChunkingService]
        JavaChunker[JavaCodeChunker]
        GenericChunker[GenericCodeChunker]
        VectorSvc[VectorStoreService]
        RagSvc[RagService]
        PromptSvc[PromptTemplateService]
        EmbedModel[GeminiEmbeddingModel<br/>(com.google.genai.Client)]
        ChatModel[Spring AI ChatModel<br/>(GoogleGenAiChatModel)]
    end

    subgraph Storage["Persistence & Vector Database (PostgreSQL 16)"]
        Postgres[Relational Tables<br/>repositories, code_files, code_chunks, chat_messages]
        PgVector[(pgvector Extension<br/>vector_store table + HNSW Cosine Index)]
    end

    subgraph Gemini["Google Gemini Developer API"]
        EmbedAPI["Embedding API<br/>(gemini-embedding-001 / 768 dims)"]
        ChatAPI["LLM Generation API<br/>(gemini-3.8-flash)"]
    end

    %% Upload & Indexing Connections
    UploadModal -->|POST /upload| RepoCtrl
    RepoCtrl --> IndexSvc
    IndexSvc --> FilterSvc
    IndexSvc --> ChunkSvc
    ChunkSvc --> JavaChunker
    ChunkSvc --> GenericChunker
    IndexSvc --> Postgres
    IndexSvc --> VectorSvc
    VectorSvc --> EmbedModel
    EmbedModel --> EmbedAPI
    VectorSvc --> PgVector

    %% Chat & RAG Connections
    UI -->|POST /chat| ChatCtrl
    ChatCtrl --> RagSvc
    RagSvc --> VectorSvc
    VectorSvc --> PgVector
    RagSvc --> PromptSvc
    RagSvc --> ChatModel
    ChatModel --> ChatAPI
    RagSvc --> Postgres
    RagSvc --> UI
    SourceViewer -.->|GET /files/{id}| Postgres
```

---

## 2. Code Indexing Pipeline Flow

The indexing process converts raw codebase zip archives into structured relational records and searchable semantic vectors.

```mermaid
sequenceDiagram
    autonumber
    actor User as Developer
    participant UI as Frontend Dashboard
    participant RC as RepositoryController
    participant RS as RepositoryService
    participant IS as CodeIndexingService
    participant FF as FileFilterService
    participant CS as CodeChunkingService
    participant DB as PostgreSQL (Relational)
    participant VS as VectorStoreService / PgVector
    participant GM as Google Gemini Embedding API

    User->>UI: Upload repository ZIP archive
    UI->>RC: POST /api/v1/repositories/upload (multipart/form-data)
    RC->>RS: createAndExtractRepository(zipFile, repoName)
    RS->>RS: Unpack ZIP into ./uploads/{repoId}/
    RS->>DB: INSERT into repositories (status='PROCESSING')
    RS-->>RC: Return RepositoryDTO
    RC-->>UI: 202 Accepted (Polling begins)

    Note over RS,IS: Transaction commits before async task triggers

    RS->>IS: indexRepositoryAsync(repoId, extractDir)
    IS->>FF: scanAndFilterFiles(extractDir)
    Note over FF: Ignores build, target, node_modules, .git, binary files
    FF-->>IS: List of supported source files (.java, .ts, .py, etc.)

    loop For each valid source file
        IS->>CS: chunkFile(file, relativePath)
        alt Java Source (.java)
            CS->>CS: JavaCodeChunker (Class/Method & structural boundary chunking)
        else Generic Source (.ts, .py, .go, etc.)
            CS->>CS: GenericCodeChunker (Sliding window with 2000 chars & 200 overlap)
        end
        CS-->>IS: List of CodeChunk entities
        IS->>DB: INSERT into code_files & code_chunks
    end

    IS->>VS: storeChunks(allCodeChunks)
    VS->>GM: Embed text batches via gemini-embedding-001 (768 dimensions)
    GM-->>VS: List of 768-dim float arrays
    VS->>DB: INSERT into vector_store (id, content, metadata, embedding)
    Note over DB: HNSW index updates vector embeddings

    IS->>DB: UPDATE repositories SET status='INDEXED'
    UI->>RC: GET /api/v1/repositories/{id}/status (polling)
    RC-->>UI: Status: INDEXED (Ready for chat)
```

---

## 3. Vector Storage Architecture (pgvector)

Vector representations are stored and indexed inside PostgreSQL using the `pgvector` extension.

```mermaid
classDiagram
    class vector_store {
        +UUID id PK
        +text content
        +jsonb metadata
        +vector(768) embedding
    }

    class MetadataJSON {
        +UUID repositoryId
        +UUID fileId
        +string filePath
        +string fileName
        +string language
        +string className
        +string methodName
        +int startLine
        +int endLine
        +int chunkIndex
    }

    class code_chunks {
        +UUID id PK
        +UUID repository_id FK
        +UUID file_id FK
        +int chunk_index
        +text content
        +string language
        +string file_path
        +string class_name
        +string method_name
        +int start_line
        +int end_line
    }

    vector_store ..> MetadataJSON : contains in metadata column
    code_chunks -- vector_store : matches 1-to-1 chunk content
```

### Search Metric & Indexing
- **Distance Metric**: Cosine Distance
  $$\text{Cosine Distance} = 1 - \frac{\mathbf{u} \cdot \mathbf{v}}{\|\mathbf{u}\|_2 \|\mathbf{v}\|_2}$$
- **Index Type**: `HNSW` (Hierarchical Navigable Small World) for sub-millisecond approximate nearest neighbor (ANN) retrieval.
- **Repository Isolation**: All queries enforce metadata isolation:
  ```sql
  WHERE metadata->>'repositoryId' = :repoId
  ```

---

## 4. RAG Query Flow (Retrieval-Augmented Generation)

When a developer asks a question about the repository, DevPilot retrieves relevant code context before prompting the LLM.

```mermaid
sequenceDiagram
    autonumber
    actor Dev as Developer
    participant UI as Frontend Chat Interface
    participant CC as ChatController
    participant RS as RagService
    participant VS as VectorStoreService
    participant GM_E as Gemini Embedding API
    participant PG as PostgreSQL (pgvector)
    participant PS as PromptTemplateService
    participant GM_C as Gemini Chat API (gemini-3.8-flash)
    participant DB as ChatMessageRepository

    Dev->>UI: Types query: "what is there in App.java"
    UI->>CC: POST /api/v1/repositories/{repoId}/chat (query, sessionId)
    CC->>RS: processChat(repoId, sessionId, query)
    
    RS->>VS: similaritySearch(query, repoId, topK=8, threshold=0.1)
    VS->>GM_E: Embed user query string
    GM_E-->>VS: Query vector [float[768]]
    
    VS->>PG: Cosine similarity search on vector_store WHERE repoId = :repoId
    PG-->>VS: Top 8 Document chunks with similarity scores
    VS-->>RS: Filtered list of relevant chunks (e.g. 2 chunks above threshold)

    RS->>PS: buildPrompt(repoName, query, retrievedChunks, conversationHistory)
    Note over PS: Formats context blocks with file paths,<br/>method names, line numbers, and strict grounding instructions
    PS-->>RS: Complete assembled Prompt

    RS->>GM_C: Invoke ChatModel with assembled prompt
    GM_C-->>RS: Grounded LLM Response text
    
    RS->>RS: Extract & format SourceReferenceDTO list
    RS->>DB: Save User & Assistant messages to chat_messages
    RS-->>CC: ChatResponseDTO (answer, sources, sessionId)
    CC-->>UI: JSON response
    UI-->>Dev: Displays answer with clickable source reference chips
```

---

## 5. Summary Table: Component Responsibilities

| Stage | Class / Service | Key Technology | Primary Output / Responsibility |
|---|---|---|---|
| **Filter** | `FileFilterService` | PathMatcher / MIME types | Excludes noise (`target`, `node_modules`, `.git`, binaries). |
| **Chunk** | `JavaCodeChunker` | Regex / AST heuristics | Extracts clean method/class chunks with line numbers. |
| **Chunk** | `GenericCodeChunker` | Sliding Window | 2,000 character windows with 200 character overlap. |
| **Embed** | `GeminiEmbeddingModel` | Google GenAI SDK | Produces 768-dimensional float arrays (`gemini-embedding-001`). |
| **Store** | `PgVectorStore` | PostgreSQL + `pgvector` | Persists vectors and metadata with HNSW cosine index. |
| **Search** | `VectorStoreService` | Spring AI `SearchRequest` | Fetches Top-K nearest code chunks for a specific `repoId`. |
| **Prompt** | `PromptTemplateService` | Structured String Templates | Enforces code grounding and prevents LLM hallucination. |
| **Answer** | `RagService` | `GoogleGenAiChatModel` | Invokes `gemini-3.8-flash` and constructs source citation chips. |
