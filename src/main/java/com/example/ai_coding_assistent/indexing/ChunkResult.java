package com.example.ai_coding_assistent.indexing;

import lombok.Builder;
import lombok.Getter;

/**
 * Intermediate data structure holding extracted chunk information.
 */
@Getter
@Builder
public class ChunkResult {
    private final int chunkIndex;
    private final String content;
    private final String language;
    private final String className;
    private final String methodName;
    private final int startLine;
    private final int endLine;
}
