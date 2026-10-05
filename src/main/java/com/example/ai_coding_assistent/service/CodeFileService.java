package com.example.ai_coding_assistent.service;

import com.example.ai_coding_assistent.dto.CodeFileContentDto;
import com.example.ai_coding_assistent.dto.CodeFileDto;
import com.example.ai_coding_assistent.entity.CodeFile;
import com.example.ai_coding_assistent.exception.RepositoryNotFoundException;
import com.example.ai_coding_assistent.repository.CodeFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service providing file exploration and source viewing for repositories.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeFileService {

    private final CodeFileRepository codeFileRepository;

    @Transactional(readOnly = true)
    public List<CodeFileDto> getFilesByRepository(UUID repositoryId) {
        return codeFileRepository.findByRepositoryId(repositoryId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CodeFileContentDto getFileContent(UUID fileId) {
        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new RepositoryNotFoundException("File not found with id: " + fileId));

        return CodeFileContentDto.builder()
                .id(file.getId())
                .repositoryId(file.getRepository().getId())
                .filePath(file.getFilePath())
                .fileName(file.getFileName())
                .extension(file.getExtension())
                .language(file.getLanguage())
                .fileSize(file.getFileSize())
                .chunkCount(file.getChunkCount())
                .content(file.getContent())
                .build();
    }

    private CodeFileDto toDto(CodeFile file) {
        return CodeFileDto.builder()
                .id(file.getId())
                .repositoryId(file.getRepository().getId())
                .filePath(file.getFilePath())
                .fileName(file.getFileName())
                .extension(file.getExtension())
                .language(file.getLanguage())
                .fileSize(file.getFileSize())
                .chunkCount(file.getChunkCount())
                .build();
    }
}
