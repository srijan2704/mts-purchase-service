package com.mts.mts_purchase_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when trying to create a resource that already exists.
 * e.g. Seller with the same name, duplicate variant label for a product.
 * Maps to HTTP 409 Conflict.
 *
 * Usage:
 *   throw new DuplicateResourceException("Seller with name '" + name + "' already exists");
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
