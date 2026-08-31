package com.simpatico.crm.exception;

/**
 * Exception thrown when a requested resource cannot be found in the database.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
