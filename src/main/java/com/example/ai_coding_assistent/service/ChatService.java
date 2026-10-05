package com.example.ai_coding_assistent.service;

import com.example.ai_coding_assistent.dto.ChatMessageDto;
import com.example.ai_coding_assistent.dto.ChatSessionDto;
import com.example.ai_coding_assistent.dto.SourceReference;
import com.example.ai_coding_assistent.entity.ChatMessage;
import com.example.ai_coding_assistent.entity.ChatSession;
import com.example.ai_coding_assistent.entity.CodeRepository;
import com.example.ai_coding_assistent.exception.RepositoryNotFoundException;
import com.example.ai_coding_assistent.repository.ChatMessageRepository;
import com.example.ai_coding_assistent.repository.ChatSessionRepository;
import com.example.ai_coding_assistent.repository.CodeRepositoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service managing chat sessions and message histories.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final CodeRepositoryRepository repositoryRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatSession getOrCreateSession(UUID repositoryId, UUID sessionId, String prompt) {
        if (sessionId != null) {
            return sessionRepository.findById(sessionId)
                    .orElseGet(() -> createNewSession(repositoryId, prompt));
        }
        return createNewSession(repositoryId, prompt);
    }

    private ChatSession createNewSession(UUID repositoryId, String prompt) {
        CodeRepository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found with id: " + repositoryId));

        String title = (prompt != null && !prompt.trim().isEmpty())
                ? (prompt.length() > 60 ? prompt.substring(0, 57) + "..." : prompt)
                : "New Chat";

        ChatSession session = ChatSession.builder()
                .repository(repo)
                .title(title)
                .build();
        return sessionRepository.save(session);
    }

    @Transactional
    public ChatMessage saveMessage(ChatSession session, String role, String content, List<SourceReference> sources) {
        String sourcesJson = null;
        if (sources != null && !sources.isEmpty()) {
            try {
                sourcesJson = objectMapper.writeValueAsString(sources);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize source references: {}", e.getMessage());
            }
        }

        ChatMessage message = ChatMessage.builder()
                .repository(session.getRepository())
                .session(session)
                .role(role)
                .content(content)
                .sourcesJson(sourcesJson)
                .build();
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionDto> getSessionsForRepository(UUID repositoryId) {
        return sessionRepository.findByRepositoryIdOrderByCreatedAtDesc(repositoryId).stream()
                .map(s -> ChatSessionDto.builder()
                        .id(s.getId())
                        .repositoryId(s.getRepository().getId())
                        .title(s.getTitle())
                        .createdAt(s.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessagesForSession(UUID sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(m -> ChatMessageDto.builder()
                        .id(m.getId())
                        .sessionId(m.getSession().getId())
                        .role(m.getRole())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt())
                        .build())
                .toList();
    }
}
