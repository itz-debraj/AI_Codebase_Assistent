package com.example.ai_coding_assistent.ai.vector;

import com.example.ai_coding_assistent.config.AppProperties;
import com.example.ai_coding_assistent.entity.CodeChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service managing pgvector vector storage and semantic similarity search via Spring AI.
 */
@Slf4j
@Service
public class VectorStoreService {

    private final Optional<VectorStore> vectorStore;
    private final AppProperties appProperties;

    @Autowired
    public VectorStoreService(Optional<VectorStore> vectorStore, AppProperties appProperties) {
        this.vectorStore = vectorStore;
        this.appProperties = appProperties;
        if (vectorStore.isEmpty()) {
            log.warn("Spring AI VectorStore is not configured. Semantic search will return empty results until pgvector/database is connected.");
        }
    }

    public boolean isAvailable() {
        return vectorStore.isPresent();
    }

    /**
     * Store a list of code chunks into the vector store.
     */
    public void storeChunks(List<CodeChunk> chunks) {
        if (vectorStore.isEmpty() || chunks.isEmpty()) {
            return;
        }

        List<Document> documents = new ArrayList<>();
        for (CodeChunk chunk : chunks) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("repositoryId", chunk.getRepository().getId().toString());
            metadata.put("fileId", chunk.getFile().getId().toString());
            metadata.put("filePath", chunk.getFilePath());
            metadata.put("fileName", chunk.getFileName());
            metadata.put("language", chunk.getLanguage() != null ? chunk.getLanguage() : "unknown");
            metadata.put("chunkIndex", chunk.getChunkIndex());
            if (chunk.getClassName() != null) metadata.put("className", chunk.getClassName());
            if (chunk.getMethodName() != null) metadata.put("methodName", chunk.getMethodName());
            if (chunk.getStartLine() != null) metadata.put("startLine", chunk.getStartLine());
            if (chunk.getEndLine() != null) metadata.put("endLine", chunk.getEndLine());

            Document doc = Document.builder()
                    .id(chunk.getId().toString())
                    .text(chunk.getContent())
                    .metadata(metadata)
                    .build();
            documents.add(doc);
        }

        try {
            log.info("Saving {} documents to pgvector store", documents.size());
            vectorStore.get().add(documents);
        } catch (Exception e) {
            log.error("Failed to add documents to vector store: {}", e.getMessage(), e);
        }
    }

    /**
     * Search for code chunks semantically similar to the given query for a specific repository.
     */
    public List<Document> searchSimilar(UUID repositoryId, String query, int topK, double similarityThreshold) {
        if (vectorStore.isEmpty() || query == null || query.trim().isEmpty()) {
            return List.of();
        }

        try {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK > 0 ? topK : appProperties.getRag().getTopK())
                    .similarityThreshold(similarityThreshold > 0 ? similarityThreshold : appProperties.getRag().getSimilarityThreshold())
                    .filterExpression(b.eq("repositoryId", repositoryId.toString()).build())
                    .build();

            log.debug("Executing vector similarity search: query='{}', repoId={}, topK={}", query, repositoryId, request.getTopK());
            return vectorStore.get().similaritySearch(request);
        } catch (Exception e) {
            log.error("Vector search failed for repo {}: {}", repositoryId, e.getMessage(), e);
            return List.of();
        }
    }
}
