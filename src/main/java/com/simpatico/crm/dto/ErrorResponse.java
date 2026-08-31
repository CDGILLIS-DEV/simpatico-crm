package com.simpatico.crm.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standard API error response payload returned to clients during exceptions.
 */
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationError> errors
) {
    /**
     * Represents a single field validation failure details.
     */
    public record ValidationError(String field, String message) {}
}
