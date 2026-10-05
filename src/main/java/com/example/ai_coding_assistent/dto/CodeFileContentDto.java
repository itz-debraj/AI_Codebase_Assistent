package com.example.ai_coding_assistent.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeFileContentDto {
    private UUID id;
    private UUID repositoryId;
    private String filePath;
    private String fileName;
    private String extension;
    private String language;
    private long fileSize;
    private int chunkCount;
    private String content;
}
