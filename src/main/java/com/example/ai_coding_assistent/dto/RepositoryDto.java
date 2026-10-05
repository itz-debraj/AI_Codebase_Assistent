package com.example.ai_coding_assistent.dto;

import com.example.ai_coding_assistent.entity.RepositoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for repository information returned to the frontend.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDto {
    private UUID id;
    private String name;
    private String originalFileName;
    private RepositoryStatus status;
    private int totalFiles;
    private int processedFiles;
    private int totalChunks;
    private int processedChunks;
    private Instant createdAt;
    private Instant updatedAt;
}
