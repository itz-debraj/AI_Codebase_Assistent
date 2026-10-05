package com.example.ai_coding_assistent.exception;

/**
 * Thrown when the AI/LLM service encounters an error.
 */
public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
