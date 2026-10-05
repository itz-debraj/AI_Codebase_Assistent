package com.example.ai_coding_assistent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response returned after a successful repository upload.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryUploadResponse {
    private UUID id;
    private String name;
    private String message;
}
