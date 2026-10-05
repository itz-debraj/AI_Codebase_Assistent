package com.example.ai_coding_assistent.ai.rag;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service providing prompt templates and context construction for the RAG pipeline.
 */
@Service
public class PromptTemplateService {

    public static final String SYSTEM_PROMPT = """
            You are DevPilot, an expert AI Codebase Assistant.
            Your role is to help developers understand, navigate, and analyze the uploaded codebase.
            
            GUIDELINES:
            1. Ground your answers primarily in the retrieved repository code and context provided below.
            2. Mention exact file paths (e.g. `src/main/java/...`), class names, and method names when explaining code.
            3. Do NOT invent, assume, or hallucinate files, methods, or configurations that are not present in the context.
            4. If the retrieved context does not contain enough information to answer the question accurately, clearly state:
               "Based on the indexed codebase, I don't have enough context to answer this completely." and explain what is missing.
            5. Provide clean, well-formatted Markdown with code blocks indicating the appropriate programming language syntax.
            6. Distinguish clearly between verified facts from the code vs architectural suggestions/best practices.
            7. Keep explanations concise, technically accurate, and developer-focused.
            """;

    public String buildUserPromptWithContext(String userQuestion, List<Document> retrievedDocuments) {
        StringBuilder sb = new StringBuilder();

        if (retrievedDocuments == null || retrievedDocuments.isEmpty()) {
            sb.append("=== NO RETRIEVED CONTEXT AVAILABLE ===\n");
            sb.append("Note: No relevant code chunks were found in the repository vector index.\n\n");
        } else {
            sb.append("=== RETRIEVED REPOSITORY CONTEXT ===\n\n");
            for (int i = 0; i < retrievedDocuments.size(); i++) {
                Document doc = retrievedDocuments.get(i);
                String filePath = (String) doc.getMetadata().getOrDefault("filePath", "unknown");
                String className = (String) doc.getMetadata().get("className");
                String methodName = (String) doc.getMetadata().get("methodName");
                Object startLine = doc.getMetadata().get("startLine");
                Object endLine = doc.getMetadata().get("endLine");

                sb.append(String.format("--- [Snippet %d] %s", i + 1, filePath));
                if (className != null) sb.append(" | Class: ").append(className);
                if (methodName != null) sb.append(" | Method: ").append(methodName);
                if (startLine != null && endLine != null) {
                    sb.append(String.format(" (Lines %s-%s)", startLine, endLine));
                }
                sb.append(" ---\n");
                sb.append(doc.getText()).append("\n\n");
            }
            sb.append("=== END OF CONTEXT ===\n\n");
        }

        sb.append("USER QUESTION:\n").append(userQuestion).append("\n\n");
        sb.append("Please provide a thorough, accurate answer referencing the code snippets above where relevant.");

        return sb.toString();
    }
}
