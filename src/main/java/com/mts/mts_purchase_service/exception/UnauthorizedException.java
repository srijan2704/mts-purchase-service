package com.mts.mts_purchase_service.exception;

/**
 * Runtime exception for unauthorized access attempts.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
