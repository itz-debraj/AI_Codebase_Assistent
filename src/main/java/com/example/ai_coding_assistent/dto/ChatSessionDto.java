package com.example.ai_coding_assistent.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionDto {
    private UUID id;
    private UUID repositoryId;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;
}
