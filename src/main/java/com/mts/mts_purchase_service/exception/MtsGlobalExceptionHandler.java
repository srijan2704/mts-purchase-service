package com.mts.mts_purchase_service.exception;

import com.mts.mts_purchase_service.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler — catches exceptions thrown by any controller
 * and returns a consistent ApiResponseDTO error payload instead of
 * Spring's default error page or stack trace.
 */
@RestControllerAdvice
public class MtsGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MtsGlobalExceptionHandler.class);

    // ── 404: Resource not found ───────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(
            ResourceNotFoundException ex) {
        log.info("Resource not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponseDTO.error(ex.getMessage()));
    }

    // ── 404: Invalid URL / endpoint not mapped ───────────────────
    // Handles requests such as /api/invalid-path where no controller
    // mapping exists, and returns the same API response contract.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNoResourceFound(
            NoResourceFoundException ex) {
        String message = "No endpoint found for URL: " + ex.getResourcePath();
        log.info("Invalid URL requested: method={}, path={}", ex.getHttpMethod(), ex.getResourcePath());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(message));
    }

    // ── 409: Duplicate resource ───────────────────────────────────
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicate(
            DuplicateResourceException ex) {
        log.info("Duplicate resource conflict: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponseDTO.error(ex.getMessage()));
    }

    // ── 401: Unauthorized access/session errors ────────────────
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUnauthorized(
            UnauthorizedException ex) {
        log.info("Unauthorized request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDTO.error(ex.getMessage()));
    }

    // ── 401/400: Missing request headers ────────────────────────
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMissingHeader(
            MissingRequestHeaderException ex) {
        if ("Authorization".equalsIgnoreCase(ex.getHeaderName())) {
            log.info("Unauthorized request: missing Authorization header");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.error("Missing or invalid Authorization header"));
        }
        log.info("Bad request: missing header {}", ex.getHeaderName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error("Missing required request header: " + ex.getHeaderName()));
    }

    // ── 400: Bean validation failures (@Valid) ────────────────────
    // Returns a map of field → error message so the UI can highlight
    // exactly which form field failed.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        log.info("Validation failed for request - {} field error(s): {}", fieldErrors.size(), fieldErrors);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Validation failed");
        response.put("errors",  fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ── 400: JSON parse / request payload type mismatch ──────────
    // Handles invalid payload shapes, wrong data types, or null passed
    // for primitive fields (for example "active": null for boolean).
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUnreadablePayload(
            HttpMessageNotReadableException ex) {
        String rawMessage = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        String message = "Invalid request payload. Please check JSON field names and data types.";
        if (rawMessage != null && rawMessage.contains("SellerDTO[\"active\"]")) {
            message = "Invalid value for 'active'. Use true/false or remove the field.";
        }

        log.info("Invalid JSON payload: {}", rawMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(message));
    }

    // ── 400: Business rule / input validation failures ──────────
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(RuntimeException ex) {
        log.info("Bad request validation failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(ex.getMessage()));
    }

    // ── 500: Any unexpected exception ────────────────────────────
    // Note: Using log.error() here (not info) — unexpected exceptions
    // should always be logged at ERROR level with full stack trace.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponseDTO.error("An unexpected error occurred. Please try again."));
    }
}
