package com.example.ai_coding_assistent.mapper;

import com.example.ai_coding_assistent.dto.RepositoryDto;
import com.example.ai_coding_assistent.dto.RepositoryStatusDto;
import com.example.ai_coding_assistent.entity.CodeRepository;

/**
 * Maps between CodeRepository entity and DTOs.
 * Uses static methods — no framework dependency needed for simple mapping.
 */
public final class RepositoryMapper {

    private RepositoryMapper() {
        // utility class
    }

    public static RepositoryDto toDto(CodeRepository entity) {
        return RepositoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .originalFileName(entity.getOriginalFileName())
                .status(entity.getStatus())
                .totalFiles(entity.getTotalFiles())
                .processedFiles(entity.getProcessedFiles())
                .totalChunks(entity.getTotalChunks())
                .processedChunks(entity.getProcessedChunks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static RepositoryStatusDto toStatusDto(CodeRepository entity) {
        return RepositoryStatusDto.builder()
                .repositoryId(entity.getId())
                .status(entity.getStatus())
                .totalFiles(entity.getTotalFiles())
                .processedFiles(entity.getProcessedFiles())
                .totalChunks(entity.getTotalChunks())
                .processedChunks(entity.getProcessedChunks())
                .build();
    }
}
