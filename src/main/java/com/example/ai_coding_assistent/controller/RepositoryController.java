package com.example.ai_coding_assistent.controller;

import com.example.ai_coding_assistent.dto.RepositoryDto;
import com.example.ai_coding_assistent.dto.RepositoryStatusDto;
import com.example.ai_coding_assistent.dto.RepositoryUploadResponse;
import com.example.ai_coding_assistent.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for repository management (upload, list, detail, status).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RepositoryUploadResponse> uploadRepository(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name) {
        
        String repoName = (name != null && !name.trim().isEmpty()) 
                ? name.trim() 
                : extractBaseName(file.getOriginalFilename());
        
        log.info("Received repository upload request: fileName={}, repoName={}", 
                file.getOriginalFilename(), repoName);
        
        RepositoryUploadResponse response = repositoryService.uploadRepository(file, repoName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RepositoryDto>> getAllRepositories() {
        return ResponseEntity.ok(repositoryService.getAllRepositories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepositoryDto> getRepositoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(repositoryService.getRepositoryById(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<RepositoryStatusDto> getRepositoryStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(repositoryService.getRepositoryStatus(id));
    }

    private String extractBaseName(String filename) {
        if (filename == null) return "untitled-repository";
        String clean = filename.replace("\\", "/");
        int lastSlash = clean.lastIndexOf('/');
        if (lastSlash >= 0) {
            clean = clean.substring(lastSlash + 1);
        }
        if (clean.toLowerCase().endsWith(".zip")) {
            clean = clean.substring(0, clean.length() - 4);
        }
        return clean.isEmpty() ? "untitled-repository" : clean;
    }
}
