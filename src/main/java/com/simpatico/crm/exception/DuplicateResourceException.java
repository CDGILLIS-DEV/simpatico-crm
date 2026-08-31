package com.simpatico.crm.exception;

/**
 * Exception thrown when a resource creation or update collides with existing data (e.g. duplicate email).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
