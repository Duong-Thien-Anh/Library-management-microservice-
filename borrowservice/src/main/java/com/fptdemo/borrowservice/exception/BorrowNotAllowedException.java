package com.fptdemo.borrowservice.exception;

/**
 * Thrown when business logic prevents the borrow action
 * (e.g., book not available, already borrowed, etc.).
 */
public class BorrowNotAllowedException extends RuntimeException {
    public BorrowNotAllowedException(String message) {
        super(message);
    }
}

