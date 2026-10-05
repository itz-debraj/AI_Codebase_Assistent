package com.example.ai_coding_assistent.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private UUID sessionId;
    private String answer;
    private List<SourceReference> sources;
}
