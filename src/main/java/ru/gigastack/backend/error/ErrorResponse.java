package ru.gigastack.backend.error;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int     status,
        String  error,
        String  message,
        String  path,
        List<FieldViolation> violations
) {
    /* 4-парам. (без списка ошибок) */
    public ErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, List.of());
    }

    /* 5-парам. (со списком violations) */
    public ErrorResponse(int status, String error, String message, String path,
                         List<FieldViolation> violations) {
        this(Instant.now(), status, error, message, path, violations);
    }

    public record FieldViolation(String field, String message) {}
}