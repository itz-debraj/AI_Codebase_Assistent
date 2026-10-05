package com.example.ai_coding_assistent.indexing;

import com.example.ai_coding_assistent.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * High-level chunking service that delegates to language-specific or generic chunkers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeChunkingService {

    private final AppProperties appProperties;
    private final JavaCodeChunker javaCodeChunker;
    private final GenericCodeChunker genericCodeChunker;

    public List<ChunkResult> chunkCode(String content, String language) {
        int maxChunkSize = appProperties.getChunking().getMaxChunkSize();
        int overlapSize = appProperties.getChunking().getOverlapSize();

        if ("java".equalsIgnoreCase(language)) {
            return javaCodeChunker.chunk(content, maxChunkSize, overlapSize);
        } else {
            return genericCodeChunker.chunk(content, language, maxChunkSize, overlapSize);
        }
    }
}
