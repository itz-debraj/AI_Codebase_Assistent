package com.example.ai_coding_assistent.controller;

import com.example.ai_coding_assistent.ai.rag.RagService;
import com.example.ai_coding_assistent.dto.ChatMessageDto;
import com.example.ai_coding_assistent.dto.ChatRequest;
import com.example.ai_coding_assistent.dto.ChatResponse;
import com.example.ai_coding_assistent.dto.ChatSessionDto;
import com.example.ai_coding_assistent.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for RAG-powered codebase chat and session history.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/repositories/{repositoryId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @PathVariable UUID repositoryId,
            @Valid @RequestBody ChatRequest request) {
        log.info("Received chat query for repository {}: '{}'", repositoryId, request.getMessage());
        ChatResponse response = ragService.chat(repositoryId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDto>> getSessions(@PathVariable UUID repositoryId) {
        return ResponseEntity.ok(chatService.getSessionsForRepository(repositoryId));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<List<ChatMessageDto>> getSessionMessages(
            @PathVariable UUID repositoryId,
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(chatService.getMessagesForSession(sessionId));
    }
}
