package com.fptdemo.borrowservice.exception;

/**
 * Thrown when the requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

