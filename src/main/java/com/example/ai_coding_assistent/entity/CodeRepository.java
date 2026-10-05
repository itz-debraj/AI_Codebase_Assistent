package com.example.ai_coding_assistent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an uploaded code repository.
 */
@Entity
@Table(name = "repositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepositoryStatus status;

    /** Total number of source files discovered */
    @Column(nullable = false)
    @Builder.Default
    private int totalFiles = 0;

    /** Number of files that have been processed */
    @Column(nullable = false)
    @Builder.Default
    private int processedFiles = 0;

    /** Total number of code chunks created */
    @Column(nullable = false)
    @Builder.Default
    private int totalChunks = 0;

    /** Number of chunks that have been embedded */
    @Column(nullable = false)
    @Builder.Default
    private int processedChunks = 0;

    /** Path where the extracted repository is stored */
    private String storagePath;

    /** Optional error message if indexing failed */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
