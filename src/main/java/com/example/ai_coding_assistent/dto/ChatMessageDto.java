package com.example.ai_coding_assistent.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private UUID id;
    private UUID sessionId;
    private String role;
    private String content;
    private Instant createdAt;
}
