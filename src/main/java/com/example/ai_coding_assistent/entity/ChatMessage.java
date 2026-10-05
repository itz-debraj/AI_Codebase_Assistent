package com.example.ai_coding_assistent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single message in a chat session (user or assistant).
 */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private CodeRepository repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    /** Role: "user" or "assistant" */
    @Column(nullable = false)
    private String role;

    /** Message content (user question or AI answer) */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** JSON string of source references (for assistant messages) */
    @Column(columnDefinition = "TEXT")
    private String sourcesJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
