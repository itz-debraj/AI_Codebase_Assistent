package com.example.ai_coding_assistent.entity;

/**
 * Represents the processing status of a repository through the indexing pipeline.
 */
public enum RepositoryStatus {
    /** Repository ZIP has been uploaded and saved */
    UPLOADED,
    /** Repository is being processed (extraction, chunking, embedding) */
    PROCESSING,
    /** Repository has been fully indexed and is ready for queries */
    INDEXED,
    /** Repository indexing has failed */
    FAILED
}
