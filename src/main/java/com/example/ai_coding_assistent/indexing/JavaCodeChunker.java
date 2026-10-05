package com.example.ai_coding_assistent.indexing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristic-based Java code chunker.
 * Decomposes Java source files into logical units (package/imports, classes, methods),
 * preserving class name, method name, and line numbers.
 */
@Slf4j
@Component
public class JavaCodeChunker {

    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "^\\s*(?:public|protected|private)?\\s*(?:static\\s+|final\\s+|abstract\\s+)*(?:class|interface|enum|record|@interface)\\s+([A-Za-z0-9_]+)",
            Pattern.MULTILINE
    );

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "^\\s*(?:(?:public|protected|private|static|final|synchronized|abstract|default)\\s+)+([A-Za-z0-9_<>\\[\\],\\?\\s]+?)\\s+([A-Za-z0-9_]+)\\s*\\(",
            Pattern.MULTILINE
    );

    public List<ChunkResult> chunk(String content, int maxChunkSize, int overlapSize) {
        if (content == null || content.trim().isEmpty()) {
            return List.of();
        }

        String[] lines = content.split("\r?\n");
        List<ChunkResult> chunks = new ArrayList<>();
        int chunkIndex = 0;

        // If the entire file is small, keep it as a single chunk
        if (content.length() <= maxChunkSize) {
            String className = detectPrimaryClass(content);
            chunks.add(ChunkResult.builder()
                    .chunkIndex(chunkIndex)
                    .content(content)
                    .language("java")
                    .className(className)
                    .methodName(null)
                    .startLine(1)
                    .endLine(lines.length)
                    .build());
            return chunks;
        }

        // Extract header (package + imports)
        int headerEndLine = 0;
        StringBuilder headerBuilder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.startsWith("package ") || trimmed.startsWith("import ") ||
                trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                headerBuilder.append(lines[i]).append("\n");
                headerEndLine = i + 1;
            } else if (!trimmed.isEmpty()) {
                break;
            }
        }

        String currentClass = detectPrimaryClass(content);

        // Heuristic line-by-line method & block scanning
        int currentStartLine = (headerEndLine > 0) ? headerEndLine + 1 : 1;
        StringBuilder currentChunkText = new StringBuilder();
        String currentMethod = null;
        int braceDepth = 0;
        int chunkStartLine = currentStartLine;

        for (int i = currentStartLine - 1; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            // Class detection
            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.find()) {
                currentClass = classMatcher.group(1);
            }

            // Method detection (only when at class level, typically braceDepth == 1)
            Matcher methodMatcher = METHOD_PATTERN.matcher(line);
            if (methodMatcher.find() && !line.trim().startsWith("if") && !line.trim().startsWith("for") && !line.trim().startsWith("while")) {
                // If we already have accumulated chunk text that reached a good size, flush it
                if (currentChunkText.length() > 300) {
                    chunks.add(ChunkResult.builder()
                            .chunkIndex(chunkIndex++)
                            .content(currentChunkText.toString().trim())
                            .language("java")
                            .className(currentClass)
                            .methodName(currentMethod)
                            .startLine(chunkStartLine)
                            .endLine(lineNumber - 1)
                            .build());
                    currentChunkText.setLength(0);
                    chunkStartLine = lineNumber;
                }
                currentMethod = methodMatcher.group(2);
            }

            currentChunkText.append(line).append("\n");

            // Count braces
            for (char c : line.toCharArray()) {
                if (c == '{') braceDepth++;
                else if (c == '}') braceDepth--;
            }

            // Flush chunk if size exceeds maxChunkSize, or if method ended (braceDepth == 1 and chunk is non-trivial)
            boolean methodBoundary = (braceDepth == 1 && currentMethod != null && currentChunkText.length() >= 400);
            boolean sizeExceeded = currentChunkText.length() >= maxChunkSize;

            if (methodBoundary || sizeExceeded) {
                chunks.add(ChunkResult.builder()
                        .chunkIndex(chunkIndex++)
                        .content(currentChunkText.toString().trim())
                        .language("java")
                        .className(currentClass)
                        .methodName(currentMethod)
                        .startLine(chunkStartLine)
                        .endLine(lineNumber)
                        .build());
                currentChunkText.setLength(0);
                chunkStartLine = lineNumber + 1;
                if (methodBoundary) {
                    currentMethod = null;
                }
            }
        }

        // Flush remaining lines
        if (currentChunkText.length() > 0 && !currentChunkText.toString().trim().isEmpty()) {
            chunks.add(ChunkResult.builder()
                    .chunkIndex(chunkIndex++)
                    .content(currentChunkText.toString().trim())
                    .language("java")
                    .className(currentClass)
                    .methodName(currentMethod)
                    .startLine(chunkStartLine)
                    .endLine(lines.length)
                    .build());
        }

        // If no chunks were created, fallback to generic single/line chunk
        if (chunks.isEmpty()) {
            chunks.add(ChunkResult.builder()
                    .chunkIndex(0)
                    .content(content)
                    .language("java")
                    .className(currentClass)
                    .methodName(null)
                    .startLine(1)
                    .endLine(lines.length)
                    .build());
        }

        return chunks;
    }

    private String detectPrimaryClass(String content) {
        Matcher matcher = CLASS_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
