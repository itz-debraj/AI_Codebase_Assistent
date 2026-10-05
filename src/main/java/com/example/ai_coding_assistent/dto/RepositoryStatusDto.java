package com.example.ai_coding_assistent.dto;

import com.example.ai_coding_assistent.entity.RepositoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for repository indexing status response.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryStatusDto {
    private UUID repositoryId;
    private RepositoryStatus status;
    private int totalFiles;
    private int processedFiles;
    private int totalChunks;
    private int processedChunks;
}
