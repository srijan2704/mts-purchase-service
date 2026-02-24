package com.mts.mts_purchase_service.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource (seller, product, order, etc.) is not found.
 * Maps to HTTP 404 Not Found.
 *
 * Usage:
 *   throw new ResourceNotFoundException("Seller not found with id: " + id);
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
