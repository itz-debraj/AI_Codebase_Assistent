package com.example.ai_coding_assistent.ai.config;

import com.google.genai.Client;
import com.google.genai.types.ContentEmbedding;
import com.google.genai.types.EmbedContentConfig;
import com.google.genai.types.EmbedContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * AI configuration providing an EmbeddingModel bean backed by the Google GenAI SDK,
 * with fallback to zero-vectors for local testing when no API key is configured.
 */
@Slf4j
@Configuration
public class AiConfig {

    public static final int DEFAULT_VECTOR_DIMENSION = 768;

    @Value("${spring.ai.google.genai.api-key:${GEMINI_API_KEY:}}")
    private String apiKey;

    @Value("${spring.ai.google.genai.embedding.options.model:gemini-embedding-001}")
    private String embeddingModelName;

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        if (!StringUtils.hasText(apiKey) || "placeholder-key-for-local-dev".equalsIgnoreCase(apiKey.trim())) {
            log.warn("Gemini API key is not configured or placeholder. Initializing fallback embedding model with dimension {}.", DEFAULT_VECTOR_DIMENSION);
            return new FallbackEmbeddingModel();
        }

        log.info("Initializing GeminiEmbeddingModel using model: {} and dimension: {}", embeddingModelName, DEFAULT_VECTOR_DIMENSION);
        return new GeminiEmbeddingModel(apiKey.trim(), embeddingModelName, DEFAULT_VECTOR_DIMENSION);
    }

    public static class GeminiEmbeddingModel implements EmbeddingModel {

        private final Client client;
        private final String modelName;
        private final int dimension;
        private final EmbedContentConfig embedConfig;

        public GeminiEmbeddingModel(String apiKey, String modelName, int dimension) {
            this.client = Client.builder().apiKey(apiKey).build();
            this.modelName = modelName;
            this.dimension = dimension;
            this.embedConfig = EmbedContentConfig.builder()
                    .outputDimensionality(dimension)
                    .build();
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<String> instructions = request.getInstructions();
            List<float[]> vectors = embed(instructions);
            List<Embedding> embeddings = new ArrayList<>(vectors.size());
            for (int i = 0; i < vectors.size(); i++) {
                embeddings.add(new Embedding(vectors.get(i), i));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            return embed(document.getText());
        }

        @Override
        public float[] embed(String text) {
            List<float[]> results = embed(List.of(text));
            return results.isEmpty() ? new float[dimension] : results.get(0);
        }

        @Override
        public List<float[]> embed(List<String> texts) {
            List<float[]> results = new ArrayList<>();
            if (texts == null || texts.isEmpty()) {
                return results;
            }

            try {
                EmbedContentResponse response = client.models.embedContent(modelName, texts, embedConfig);
                if (response.embeddings().isPresent()) {
                    for (ContentEmbedding ce : response.embeddings().get()) {
                        if (ce.values().isPresent()) {
                            List<Float> values = ce.values().get();
                            float[] arr = new float[values.size()];
                            for (int i = 0; i < values.size(); i++) {
                                arr[i] = values.get(i);
                            }
                            results.add(arr);
                        } else {
                            results.add(new float[dimension]);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to generate embeddings from Gemini API for {} texts: {}", texts.size(), e.getMessage());
                // Fallback to zero vectors so the pipeline does not completely crash
                for (int i = 0; i < texts.size(); i++) {
                    results.add(new float[dimension]);
                }
            }

            // Ensure output size matches input size
            while (results.size() < texts.size()) {
                results.add(new float[dimension]);
            }

            return results;
        }

        @Override
        public int dimensions() {
            return dimension;
        }
    }

    public static class FallbackEmbeddingModel implements EmbeddingModel {

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<Embedding> embeddings = new ArrayList<>();
            for (int i = 0; i < request.getInstructions().size(); i++) {
                embeddings.add(new Embedding(new float[DEFAULT_VECTOR_DIMENSION], i));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            return new float[DEFAULT_VECTOR_DIMENSION];
        }

        @Override
        public float[] embed(String text) {
            return new float[DEFAULT_VECTOR_DIMENSION];
        }

        @Override
        public List<float[]> embed(List<String> texts) {
            List<float[]> list = new ArrayList<>(texts.size());
            for (int i = 0; i < texts.size(); i++) {
                list.add(new float[DEFAULT_VECTOR_DIMENSION]);
            }
            return list;
        }

        @Override
        public int dimensions() {
            return DEFAULT_VECTOR_DIMENSION;
        }
    }
}
