package com.example.ai_coding_assistent.ai.rag;

import com.example.ai_coding_assistent.ai.vector.VectorStoreService;
import com.example.ai_coding_assistent.config.AppProperties;
import com.example.ai_coding_assistent.dto.ChatRequest;
import com.example.ai_coding_assistent.dto.ChatResponse;
import com.example.ai_coding_assistent.dto.SourceReference;
import com.example.ai_coding_assistent.entity.ChatSession;
import com.example.ai_coding_assistent.entity.CodeRepository;
import com.example.ai_coding_assistent.repository.CodeRepositoryRepository;
import com.example.ai_coding_assistent.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core RAG Orchestrator:
 * Semantic Search (Top-K) -> Context Construction -> LLM (Gemini) -> Grounded Answer with Source References.
 */
@Slf4j
@Service
public class RagService {

    private final VectorStoreService vectorStoreService;
    private final PromptTemplateService promptTemplateService;
    private final ChatService chatService;
    private final CodeRepositoryRepository repositoryRepository;
    private final AppProperties appProperties;
    private final Optional<ChatModel> chatModel;

    @Autowired
    public RagService(
            VectorStoreService vectorStoreService,
            PromptTemplateService promptTemplateService,
            ChatService chatService,
            CodeRepositoryRepository repositoryRepository,
            AppProperties appProperties,
            Optional<ChatModel> chatModel) {
        this.vectorStoreService = vectorStoreService;
        this.promptTemplateService = promptTemplateService;
        this.chatService = chatService;
        this.repositoryRepository = repositoryRepository;
        this.appProperties = appProperties;
        this.chatModel = chatModel;

        if (chatModel.isEmpty()) {
            log.warn("Spring AI ChatModel is not configured (e.g. GEMINI_API_KEY missing). Fallback answers will be generated from retrieved snippets.");
        }
    }

    public ChatResponse chat(UUID repositoryId, ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String question = request.getMessage().trim();

        // 1. Validate repository
        CodeRepository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with id: " + repositoryId));

        // 2. Retrieve or create chat session
        ChatSession session = chatService.getOrCreateSession(repositoryId, request.getSessionId(), question);

        // 3. Save User message to history
        chatService.saveMessage(session, "user", question, null);

        // 4. Vector similarity retrieval
        int topK = appProperties.getRag().getTopK();
        double similarityThreshold = appProperties.getRag().getSimilarityThreshold();
        List<Document> retrievedDocs = vectorStoreService.searchSimilar(repositoryId, question, topK, similarityThreshold);

        log.info("Retrieved {} relevant chunks for query: '{}' in repo: {}", retrievedDocs.size(), question, repo.getName());

        // 5. Extract source references
        List<SourceReference> sources = extractSources(retrievedDocs);

        // 6. Generate answer via LLM
        String answer;
        if (chatModel.isPresent()) {
            try {
                String systemPrompt = PromptTemplateService.SYSTEM_PROMPT;
                String userPromptWithContext = promptTemplateService.buildUserPromptWithContext(question, retrievedDocs);

                Prompt prompt = new Prompt(List.of(
                        new SystemMessage(systemPrompt),
                        new UserMessage(userPromptWithContext)
                ));

                log.debug("Invoking Spring AI ChatModel for repo {}...", repositoryId);
                var aiResponse = chatModel.get().call(prompt);
                answer = aiResponse.getResult().getOutput().getText();
            } catch (Exception e) {
                log.error("LLM call failed: {}", e.getMessage(), e);
                answer = buildFallbackAnswer(question, retrievedDocs, "LLM generation encountered an error: " + e.getMessage());
            }
        } else {
            answer = buildFallbackAnswer(question, retrievedDocs, "GEMINI_API_KEY is not configured in the application environment.");
        }

        // 7. Save Assistant message to history
        chatService.saveMessage(session, "assistant", answer, sources);

        long duration = System.currentTimeMillis() - startTime;
        log.info("RAG chat completed in {} ms for session {}", duration, session.getId());

        return ChatResponse.builder()
                .sessionId(session.getId())
                .answer(answer)
                .sources(sources)
                .build();
    }

    private List<SourceReference> extractSources(List<Document> documents) {
        List<SourceReference> sources = new ArrayList<>();
        if (documents == null) return sources;

        Set<String> seenKeys = new HashSet<>();

        for (Document doc : documents) {
            Map<String, Object> meta = doc.getMetadata();
            String filePath = (String) meta.getOrDefault("filePath", "unknown");
            String fileName = (String) meta.getOrDefault("fileName", filePath);
            String className = (String) meta.get("className");
            String methodName = (String) meta.get("methodName");
            Integer startLine = meta.get("startLine") instanceof Number n ? n.intValue() : null;
            Integer endLine = meta.get("endLine") instanceof Number n ? n.intValue() : null;

            UUID chunkId = null;
            try {
                if (doc.getId() != null) {
                    chunkId = UUID.fromString(doc.getId());
                }
            } catch (Exception ignored) {}

            String key = filePath + ":" + startLine + ":" + endLine;
            if (seenKeys.add(key)) {
                String text = doc.getText();
                String snippet = text.length() > 250 ? text.substring(0, 247) + "..." : text;

                sources.add(SourceReference.builder()
                        .chunkId(chunkId)
                        .filePath(filePath)
                        .fileName(fileName)
                        .className(className)
                        .methodName(methodName)
                        .startLine(startLine)
                        .endLine(endLine)
                        .relevanceScore(doc.getScore())
                        .snippet(snippet)
                        .build());
            }
        }
        return sources;
    }

    private String buildFallbackAnswer(String question, List<Document> retrievedDocs, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("> **Notice:** ").append(reason).append("\n\n");

        if (retrievedDocs == null || retrievedDocs.isEmpty()) {
            sb.append("No matching code chunks were found for your query: \"").append(question).append("\".\n");
            sb.append("Ensure the repository has completed indexing and that relevant code files exist.");
        } else {
            sb.append("Here are the most relevant code chunks retrieved from your repository for **\"").append(question).append("\"**:\n\n");
            for (int i = 0; i < retrievedDocs.size(); i++) {
                Document doc = retrievedDocs.get(i);
                String filePath = (String) doc.getMetadata().getOrDefault("filePath", "unknown");
                String lang = (String) doc.getMetadata().getOrDefault("language", "");
                sb.append("### ").append(i + 1).append(". `").append(filePath).append("`\n");
                sb.append("```").append(lang).append("\n");
                sb.append(doc.getText()).append("\n");
                sb.append("```\n\n");
            }
        }
        return sb.toString();
    }
}
