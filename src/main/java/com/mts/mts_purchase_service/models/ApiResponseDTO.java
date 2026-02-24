package com.mts.mts_purchase_service.models;


import java.time.LocalDateTime;

/**
 * Generic API response wrapper for all endpoints.
 *
 * Every response from the API follows this consistent structure:
 * {
 *   "success": true,
 *   "message": "Seller created successfully",
 *   "data": { ... },
 *   "timestamp": "2025-02-21T10:30:00"
 * }
 *
 * Usage:
 *   ApiResponseDTO.success("message", dataObject)
 *   ApiResponseDTO.error("Something went wrong")
 */
public class ApiResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // ── Private constructor — use static factory methods ──────────

    private ApiResponseDTO(boolean success, String message, T data) {
        this.success   = success;
        this.message   = message;
        this.data      = data;
        this.timestamp = LocalDateTime.now();
    }

    // ── Static factory methods ────────────────────────────────────

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>(true, message, data);
    }

    public static <T> ApiResponseDTO<T> error(String message) {
        return new ApiResponseDTO<>(false, message, null);
    }

    // ── Getters ───────────────────────────────────────────────────

    public boolean isSuccess()             { return success; }
    public String getMessage()             { return message; }
    public T getData()                     { return data; }
    public LocalDateTime getTimestamp()    { return timestamp; }
}
