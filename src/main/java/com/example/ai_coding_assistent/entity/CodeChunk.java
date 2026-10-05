package com.example.ai_coding_assistent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a chunk of source code extracted from a file.
 * Each chunk has an associated embedding vector stored in pgvector.
 */
@Entity
@Table(name = "code_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private CodeRepository repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private CodeFile file;

    /** Sequential index of this chunk within the file */
    @Column(nullable = false)
    private int chunkIndex;

    /** The actual source code content of this chunk */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** Detected programming language */
    private String language;

    /** Relative file path within the repository */
    @Column(nullable = false)
    private String filePath;

    /** File name */
    @Column(nullable = false)
    private String fileName;

    /** Detected class name (if applicable) */
    private String className;

    /** Detected method name (if applicable) */
    private String methodName;

    /** Start line in the original file */
    private Integer startLine;

    /** End line in the original file */
    private Integer endLine;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
