package com.example.ai_coding_assistent.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Standardized API error response returned for all exceptions.
 */
@Getter
@Builder
@AllArgsConstructor
public class ApiError {
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
}
