package com.example.ai_coding_assistent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a source file within an uploaded repository.
 */
@Entity
@Table(name = "code_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private CodeRepository repository;

    /** Relative file path within the repository */
    @Column(nullable = false)
    private String filePath;

    /** Just the file name (e.g., "UserService.java") */
    @Column(nullable = false)
    private String fileName;

    /** File extension (e.g., ".java") */
    @Column(nullable = false)
    private String extension;

    /** Detected programming language */
    private String language;

    /** File size in bytes */
    private long fileSize;

    /** Full file content stored for source viewing */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Number of chunks generated from this file */
    @Column(nullable = false)
    @Builder.Default
    private int chunkCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
