package com.fptdemo.bookservice.exception;

/**
 * CUSTOM EXCEPTION: Duplicate ISBN
 * 
 * This exception is thrown when:
 * - Trying to create a book with an ISBN that already exists
 * - Trying to update a book's ISBN to one that already exists
 * 
 * Why? Because ISBN should be unique for each book
 */
public class DuplicateIsbnException extends RuntimeException {
    
    /**
     * Constructor with message
     * 
     * @param message - Error message
     * 
     * Example usage:
     * throw new DuplicateIsbnException("Book with ISBN 9780132350884 already exists");
     */
    public DuplicateIsbnException(String message) {
        super(message);
    }
}
