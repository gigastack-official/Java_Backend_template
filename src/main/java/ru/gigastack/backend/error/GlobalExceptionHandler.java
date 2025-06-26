package ru.gigastack.backend.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* 2.1 - валидация DTO  */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        var violations = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErrorResponse.FieldViolation(
                        f.getField(), f.getDefaultMessage()))
                .toList();

        var body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                violations
        );
        return ResponseEntity.badRequest().body(body);
    }

    /* 2.2 - бизнес-исключения  */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatus(
            ResponseStatusException ex,
            HttpServletRequest request) {

        var status  = ex.getStatusCode();             // HttpStatusCode
        var code    = status.value();                 // int
        var reason  = (status instanceof HttpStatus hs)
                ? hs.getReasonPhrase()          // SAFE cast
                : "Error";

        var body = new ErrorResponse(
                code, reason,
                ex.getReason(),
                request.getRequestURI()
        );
        return ResponseEntity.status(code).body(body);
    }

    /* 2.3 - всё остальное (NPE, SQL) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception", ex);

        var body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}