package com.example.ai_coding_assistent.indexing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fallback code chunker for non-Java languages (Python, TypeScript, Go, YAML, Markdown, etc.).
 * Uses line-based sliding windows with overlap, attempting to preserve symbol names (functions/classes).
 */
@Slf4j
@Component
public class GenericCodeChunker {

    private static final Pattern GENERIC_DEF_PATTERN = Pattern.compile(
            "^\\s*(?:def|class|function|const|let|var|type|interface|func)\\s+([A-Za-z0-9_]+)",
            Pattern.MULTILINE
    );

    public List<ChunkResult> chunk(String content, String language, int maxChunkSize, int overlapSize) {
        if (content == null || content.trim().isEmpty()) {
            return List.of();
        }

        String[] lines = content.split("\r?\n");
        List<ChunkResult> chunks = new ArrayList<>();

        if (content.length() <= maxChunkSize) {
            chunks.add(ChunkResult.builder()
                    .chunkIndex(0)
                    .content(content)
                    .language(language)
                    .className(detectSymbol(content))
                    .methodName(null)
                    .startLine(1)
                    .endLine(lines.length)
                    .build());
            return chunks;
        }

        int chunkIndex = 0;
        int currentLineIdx = 0;

        while (currentLineIdx < lines.length) {
            StringBuilder chunkBuilder = new StringBuilder();
            int startLine = currentLineIdx + 1;
            String currentSymbol = null;

            int endLineIdx = currentLineIdx;
            while (endLineIdx < lines.length) {
                String line = lines[endLineIdx];
                if (currentSymbol == null) {
                    Matcher m = GENERIC_DEF_PATTERN.matcher(line);
                    if (m.find()) {
                        currentSymbol = m.group(1);
                    }
                }

                if (chunkBuilder.length() + line.length() + 1 > maxChunkSize && chunkBuilder.length() > 0) {
                    break;
                }

                chunkBuilder.append(line).append("\n");
                endLineIdx++;
            }

            int endLine = endLineIdx;
            String chunkContent = chunkBuilder.toString().trim();

            if (!chunkContent.isEmpty()) {
                chunks.add(ChunkResult.builder()
                        .chunkIndex(chunkIndex++)
                        .content(chunkContent)
                        .language(language)
                        .className(currentSymbol)
                        .methodName(null)
                        .startLine(startLine)
                        .endLine(endLine)
                        .build());
            }

            // Advance with overlap
            int linesInChunk = endLineIdx - currentLineIdx;
            int overlapLines = Math.max(1, (int) Math.round((double) overlapSize / Math.max(1, chunkContent.length() / Math.max(1, linesInChunk))));
            if (linesInChunk <= overlapLines) {
                currentLineIdx = endLineIdx;
            } else {
                currentLineIdx = endLineIdx - overlapLines;
            }

            if (currentLineIdx <= startLine - 1) {
                currentLineIdx = endLineIdx;
            }
        }

        return chunks;
    }

    private String detectSymbol(String content) {
        Matcher m = GENERIC_DEF_PATTERN.matcher(content);
        return m.find() ? m.group(1) : null;
    }
}
