package com.example.ai_coding_assistent.exception;

/**
 * Thrown when a repository is not found by its ID.
 */
public class RepositoryNotFoundException extends RuntimeException {
    public RepositoryNotFoundException(String message) {
        super(message);
    }
}
