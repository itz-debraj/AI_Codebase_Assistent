package com.example.ai_coding_assistent.indexing;

import com.example.ai_coding_assistent.ai.vector.VectorStoreService;
import com.example.ai_coding_assistent.entity.CodeChunk;
import com.example.ai_coding_assistent.entity.CodeFile;
import com.example.ai_coding_assistent.entity.CodeRepository;
import com.example.ai_coding_assistent.entity.RepositoryStatus;
import com.example.ai_coding_assistent.repository.CodeChunkRepository;
import com.example.ai_coding_assistent.repository.CodeFileRepository;
import com.example.ai_coding_assistent.repository.CodeRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service orchestrating the code indexing pipeline:
 * File discovery -> Filtering -> Chunking -> Vector embedding -> Storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeIndexingService {

    private final CodeRepositoryRepository repositoryRepository;
    private final CodeFileRepository codeFileRepository;
    private final CodeChunkRepository codeChunkRepository;
    private final FileFilterService fileFilterService;
    private final CodeChunkingService codeChunkingService;
    private final VectorStoreService vectorStoreService;

    @Async("taskExecutor")
    public void indexRepositoryAsync(UUID repositoryId) {
        log.info("Starting asynchronous indexing for repository id: {}", repositoryId);
        long startTime = System.currentTimeMillis();

        CodeRepository repository = null;
        for (int attempt = 1; attempt <= 5; attempt++) {
            repository = repositoryRepository.findById(repositoryId).orElse(null);
            if (repository != null) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (repository == null) {
            log.error("Repository not found for indexing after retries: {}", repositoryId);
            return;
        }

        try {
            repository.setStatus(RepositoryStatus.PROCESSING);
            repository.setErrorMessage(null);
            repositoryRepository.save(repository);

            Path storageDir = Paths.get(repository.getStoragePath());
            if (!Files.exists(storageDir)) {
                throw new IllegalStateException("Storage directory does not exist: " + storageDir);
            }

            // 1. Discover all candidate files
            List<Path> candidateFiles = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(storageDir)) {
                stream.forEach(path -> {
                    if (fileFilterService.shouldIndex(storageDir, path)) {
                        candidateFiles.add(path);
                    }
                });
            }

            repository.setTotalFiles(candidateFiles.size());
            repository.setProcessedFiles(0);
            repository.setTotalChunks(0);
            repository.setProcessedChunks(0);
            repositoryRepository.save(repository);

            log.info("Discovered {} indexable source files for repository {}", candidateFiles.size(), repositoryId);

            int totalChunkCount = 0;
            int processedFileCount = 0;

            for (Path filePath : candidateFiles) {
                Path relativePath = storageDir.relativize(filePath);
                String normalizedPath = relativePath.toString().replace('\\', '/');
                String fileName = filePath.getFileName().toString();
                String extension = fileFilterService.getFileExtension(fileName);
                String language = fileFilterService.detectLanguage(fileName);

                String content;
                try {
                    content = Files.readString(filePath, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("Failed to read file {} as UTF-8, trying ISO_8859_1: {}", normalizedPath, e.getMessage());
                    try {
                        content = Files.readString(filePath, StandardCharsets.ISO_8859_1);
                    } catch (IOException ex) {
                        log.error("Skipping unreadable file {}: {}", normalizedPath, ex.getMessage());
                        continue;
                    }
                }

                // Chunk the content
                List<ChunkResult> chunkResults = codeChunkingService.chunkCode(content, language);

                // Save CodeFile
                CodeFile codeFile = CodeFile.builder()
                        .repository(repository)
                        .filePath(normalizedPath)
                        .fileName(fileName)
                        .extension(extension)
                        .language(language)
                        .fileSize(Files.size(filePath))
                        .content(content)
                        .chunkCount(chunkResults.size())
                        .build();
                codeFile = codeFileRepository.save(codeFile);

                // Create and save CodeChunks
                List<CodeChunk> chunksToSave = new ArrayList<>();
                for (ChunkResult cr : chunkResults) {
                    CodeChunk chunk = CodeChunk.builder()
                            .repository(repository)
                            .file(codeFile)
                            .chunkIndex(cr.getChunkIndex())
                            .content(cr.getContent())
                            .language(cr.getLanguage())
                            .filePath(normalizedPath)
                            .fileName(fileName)
                            .className(cr.getClassName())
                            .methodName(cr.getMethodName())
                            .startLine(cr.getStartLine())
                            .endLine(cr.getEndLine())
                            .build();
                    chunksToSave.add(chunk);
                }

                List<CodeChunk> savedChunks = codeChunkRepository.saveAll(chunksToSave);
                totalChunkCount += savedChunks.size();
                processedFileCount++;

                // Store in Vector DB
                vectorStoreService.storeChunks(savedChunks);

                // Periodically update progress
                if (processedFileCount % 10 == 0 || processedFileCount == candidateFiles.size()) {
                    repository.setProcessedFiles(processedFileCount);
                    repository.setTotalChunks(totalChunkCount);
                    repository.setProcessedChunks(totalChunkCount);
                    repositoryRepository.save(repository);
                }
            }

            // Mark as INDEXED
            repository.setStatus(RepositoryStatus.INDEXED);
            repository.setProcessedFiles(processedFileCount);
            repository.setTotalChunks(totalChunkCount);
            repository.setProcessedChunks(totalChunkCount);
            repositoryRepository.save(repository);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Indexing completed for repository {} ({} files, {} chunks) in {} ms",
                    repositoryId, processedFileCount, totalChunkCount, duration);

        } catch (Exception e) {
            log.error("Indexing failed for repository {}: {}", repositoryId, e.getMessage(), e);
            repository.setStatus(RepositoryStatus.FAILED);
            repository.setErrorMessage(e.getMessage());
            repositoryRepository.save(repository);
        }
    }
}
