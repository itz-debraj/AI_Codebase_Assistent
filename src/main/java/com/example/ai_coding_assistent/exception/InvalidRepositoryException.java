package com.example.ai_coding_assistent.exception;

/**
 * Thrown when an uploaded repository archive is invalid (wrong format, empty, path traversal, etc.).
 */
public class InvalidRepositoryException extends RuntimeException {
    public InvalidRepositoryException(String message) {
        super(message);
    }
}
