package com.example.ai_coding_assistent.controller;

import com.example.ai_coding_assistent.dto.CodeFileContentDto;
import com.example.ai_coding_assistent.dto.CodeFileDto;
import com.example.ai_coding_assistent.service.CodeFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for retrieving repository files and viewing source code.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/repositories/{repositoryId}/files")
@RequiredArgsConstructor
public class CodeFileController {

    private final CodeFileService codeFileService;

    @GetMapping
    public ResponseEntity<List<CodeFileDto>> getFiles(@PathVariable UUID repositoryId) {
        return ResponseEntity.ok(codeFileService.getFilesByRepository(repositoryId));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<CodeFileContentDto> getFileContent(
            @PathVariable UUID repositoryId,
            @PathVariable UUID fileId) {
        return ResponseEntity.ok(codeFileService.getFileContent(fileId));
    }
}
