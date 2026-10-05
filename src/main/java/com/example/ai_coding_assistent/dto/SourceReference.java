package com.example.ai_coding_assistent.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceReference {
    private String filePath;
    private String fileName;
    private String className;
    private String methodName;
    private UUID chunkId;
    private Integer startLine;
    private Integer endLine;
    private Double relevanceScore;
    private String snippet;
}
