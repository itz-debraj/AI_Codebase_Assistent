package com.example.ai_coding_assistent.exception;

/**
 * Thrown when code indexing fails during processing.
 */
public class IndexingException extends RuntimeException {
    public IndexingException(String message) {
        super(message);
    }

    public IndexingException(String message, Throwable cause) {
        super(message, cause);
    }
}
