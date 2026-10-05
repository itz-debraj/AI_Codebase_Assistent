package com.example.ai_coding_assistent.ai.embedding;

import com.example.ai_coding_assistent.exception.AiServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for generating text embeddings using Spring AI.
 */
@Slf4j
@Service
public class EmbeddingService {

    private final Optional<EmbeddingModel> embeddingModel;

    @Autowired
    public EmbeddingService(Optional<EmbeddingModel> embeddingModel) {
        this.embeddingModel = embeddingModel;
        if (embeddingModel.isEmpty()) {
            log.warn("Spring AI EmbeddingModel is not configured (e.g. GEMINI_API_KEY missing). Vector embeddings will be simulated or skipped.");
        }
    }

    /**
     * Check if an active embedding model is available.
     */
    public boolean isAvailable() {
        return embeddingModel.isPresent();
    }

    /**
     * Generate an embedding vector for a single text chunk.
     */
    public float[] embed(String text) {
        if (embeddingModel.isEmpty()) {
            log.debug("EmbeddingModel unavailable, returning empty vector");
            return new float[0];
        }
        try {
            return embeddingModel.get().embed(text);
        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Generate embeddings for a batch of texts.
     */
    public List<float[]> embedBatch(List<String> texts) {
        if (embeddingModel.isEmpty()) {
            return List.of();
        }
        try {
            return embeddingModel.get().embed(texts);
        } catch (Exception e) {
            log.error("Failed to generate batch embeddings: {}", e.getMessage(), e);
            throw new AiServiceException("Failed to generate batch embeddings: " + e.getMessage(), e);
        }
    }
}
