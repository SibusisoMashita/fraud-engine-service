package com.fraudengine.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fraudengine.exception.JwtExceptions.ExpiredTokenException;
import com.fraudengine.exception.JwtExceptions.InvalidTokenException;
import com.fraudengine.exception.JwtExceptions.MissingSecretException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Object> buildResponse(
            HttpStatus status,
            String message,
            Map<String, Object> details
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        if (details != null && !details.isEmpty()) {
            body.put("details", details);
        }

        return new ResponseEntity<>(body, status);
    }

    // ────────────────────────────────────────────────────────────────
    //  @Valid DTO Validation Errors (400)
    // ────────────────────────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.info(
                "event=validation_failed fields={} exception={} message={}",
                fieldErrors.keySet(),
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                Map.of("fields", fieldErrors)
        );
    }

    // ────────────────────────────────────────────────────────────────
    //  @Validated Method Parameter Errors (400)
    // ────────────────────────────────────────────────────────────────
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {

        log.info(
                "event=constraint_violation exception={} message={}",
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Constraint violation",
                Map.of("violation", ex.getMessage())
        );
    }

    // ────────────────────────────────────────────────────────────────
    //  Not Found / Illegal arguments (404)
    // ────────────────────────────────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleNotFound(IllegalArgumentException ex) {

        log.warn(
                "event=entity_not_found exception={} message={}",
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                null
        );
    }

    // ────────────────────────────────────────────────────────────────
    //  Business Logic Conflicts (409)
    // ────────────────────────────────────────────────────────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleConflict(IllegalStateException ex) {

        log.warn(
                "event=business_conflict exception={} message={}",
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                null
        );
    }

    // JWT related exceptions (401)
    @ExceptionHandler({MissingSecretException.class, InvalidTokenException.class, ExpiredTokenException.class})
    public ResponseEntity<Object> handleJwt(RuntimeException ex) {
        log.warn(
                "event=jwt_error exception={} message={}",
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                null
        );
    }

    // ────────────────────────────────────────────────────────────────
    //  Catch-all fallback for unexpected errors (500)
    // ────────────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneral(Exception ex) {

        log.error(
                "event=unexpected_exception exception={} message={}",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                Map.of("exception", ex.getClass().getSimpleName())
        );
    }
}
