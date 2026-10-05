package com.example.ai_coding_assistent.service;

import com.example.ai_coding_assistent.config.AppProperties;
import com.example.ai_coding_assistent.dto.RepositoryDto;
import com.example.ai_coding_assistent.dto.RepositoryStatusDto;
import com.example.ai_coding_assistent.dto.RepositoryUploadResponse;
import com.example.ai_coding_assistent.entity.CodeRepository;
import com.example.ai_coding_assistent.entity.RepositoryStatus;
import com.example.ai_coding_assistent.exception.InvalidRepositoryException;
import com.example.ai_coding_assistent.exception.RepositoryNotFoundException;
import com.example.ai_coding_assistent.mapper.RepositoryMapper;
import com.example.ai_coding_assistent.repository.CodeRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Manages repository upload, validation, extraction, and metadata.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final CodeRepositoryRepository repositoryRepository;
    private final AppProperties appProperties;
    private final com.example.ai_coding_assistent.indexing.CodeIndexingService codeIndexingService;

    /**
     * Upload and validate a ZIP repository file.
     * The file is extracted and saved, then indexing is triggered asynchronously.
     */
    @Transactional
    public RepositoryUploadResponse uploadRepository(MultipartFile file, String repositoryName) {
        // Validate input
        validateUpload(file, repositoryName);

        // Create repository record
        CodeRepository repository = CodeRepository.builder()
                .name(repositoryName.trim())
                .originalFileName(file.getOriginalFilename())
                .status(RepositoryStatus.UPLOADED)
                .build();
        repository = repositoryRepository.save(repository);
        log.info("Repository created: id={}, name={}", repository.getId(), repository.getName());

        // Extract ZIP to storage directory
        Path extractionPath = extractZip(file, repository.getId());
        repository.setStoragePath(extractionPath.toString());
        log.info("Repository extracted: id={}, path={}", repository.getId(), extractionPath);

        // Trigger indexing pipeline asynchronously only AFTER transaction has committed
        final UUID repoId = repository.getId();
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            log.info("Transaction committed. Triggering async indexing for repo: {}", repoId);
                            codeIndexingService.indexRepositoryAsync(repoId);
                        }
                    }
            );
        } else {
            codeIndexingService.indexRepositoryAsync(repoId);
        }

        return RepositoryUploadResponse.builder()
                .id(repository.getId())
                .name(repository.getName())
                .message("Repository uploaded successfully. Indexing has started.")
                .build();
    }

    @Transactional(readOnly = true)
    public List<RepositoryDto> getAllRepositories() {
        return repositoryRepository.findAll().stream()
                .map(RepositoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RepositoryDto getRepositoryById(UUID id) {
        CodeRepository repo = findRepositoryOrThrow(id);
        return RepositoryMapper.toDto(repo);
    }

    @Transactional(readOnly = true)
    public RepositoryStatusDto getRepositoryStatus(UUID id) {
        CodeRepository repo = findRepositoryOrThrow(id);
        return RepositoryMapper.toStatusDto(repo);
    }

    public CodeRepository findRepositoryOrThrow(UUID id) {
        return repositoryRepository.findById(id)
                .orElseThrow(() -> new RepositoryNotFoundException(
                        "Repository not found with id: " + id));
    }

    // ==================== Validation ====================

    private void validateUpload(MultipartFile file, String repositoryName) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRepositoryException("Upload file is required and must not be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new InvalidRepositoryException("Only ZIP files are supported");
        }

        if (repositoryName == null || repositoryName.trim().isEmpty()) {
            throw new InvalidRepositoryException("Repository name is required");
        }

        long maxSizeBytes = (long) appProperties.getStorage().getMaxFileSizeMb() * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidRepositoryException(
                    "File size exceeds maximum allowed size of " +
                    appProperties.getStorage().getMaxFileSizeMb() + "MB");
        }
    }

    // ==================== ZIP Extraction ====================

    private Path extractZip(MultipartFile file, UUID repositoryId) {
        try {
            Path uploadDir = Paths.get(appProperties.getStorage().getUploadDir())
                    .toAbsolutePath().normalize();
            Path repositoryDir = uploadDir.resolve(repositoryId.toString());
            Files.createDirectories(repositoryDir);

            try (InputStream is = file.getInputStream();
                 ZipInputStream zis = new ZipInputStream(is)) {

                ZipEntry entry;
                int fileCount = 0;

                while ((entry = zis.getNextEntry()) != null) {
                    Path entryPath = repositoryDir.resolve(entry.getName()).normalize();

                    // Security: prevent ZIP path traversal
                    if (!entryPath.startsWith(repositoryDir)) {
                        throw new InvalidRepositoryException(
                                "ZIP entry contains path traversal: " + entry.getName());
                    }

                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                        fileCount++;
                    }

                    zis.closeEntry();
                }

                if (fileCount == 0) {
                    throw new InvalidRepositoryException("Repository archive is empty");
                }

                log.info("Extracted {} files for repository {}", fileCount, repositoryId);
            }

            return repositoryDir;

        } catch (InvalidRepositoryException e) {
            throw e;
        } catch (IOException e) {
            throw new InvalidRepositoryException("Failed to extract repository archive: " + e.getMessage());
        }
    }
}
