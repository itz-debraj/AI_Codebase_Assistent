package com.example.ai_coding_assistent.ai.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromptTemplateServiceTest {

    private final PromptTemplateService promptTemplateService = new PromptTemplateService();

    @Test
    void shouldBuildPromptWithContextSnippets() {
        Document doc = Document.builder()
                .text("public class AuthController { ... }")
                .metadata(Map.of(
                        "filePath", "src/main/java/AuthController.java",
                        "className", "AuthController",
                        "startLine", 10,
                        "endLine", 35
                ))
                .build();

        String prompt = promptTemplateService.buildUserPromptWithContext(
                "Where is authentication defined?",
                List.of(doc)
        );

        assertTrue(prompt.contains("RETRIEVED REPOSITORY CONTEXT"));
        assertTrue(prompt.contains("src/main/java/AuthController.java"));
        assertTrue(prompt.contains("Where is authentication defined?"));
        assertTrue(prompt.contains("Lines 10-35"));
    }

    @Test
    void shouldHandleEmptyRetrievedDocuments() {
        String prompt = promptTemplateService.buildUserPromptWithContext(
                "How does caching work?",
                List.of()
        );

        assertTrue(prompt.contains("NO RETRIEVED CONTEXT AVAILABLE"));
        assertTrue(prompt.contains("How does caching work?"));
    }
}
