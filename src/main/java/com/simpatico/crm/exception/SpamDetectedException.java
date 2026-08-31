package com.simpatico.crm.exception;

/**
 * Exception thrown when a honeypot check fails or spam patterns are identified in public submissions.
 */
public class SpamDetectedException extends RuntimeException {
    public SpamDetectedException(String message) {
        super(message);
    }
}
