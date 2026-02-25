package com.fptdemo.bookservice.exception;

/**
 * CUSTOM EXCEPTION: Book Not Found
 * 
 * EXCEPTION HANDLING EXPLANATION:
 * - Exceptions are special classes that represent errors
 * - When something goes wrong, we "throw" an exception
 * - Spring catches the exception and returns appropriate error response
 * 
 * This exception is thrown when:
 * - Trying to get a book that doesn't exist
 * - Trying to update a book that doesn't exist
 * - Trying to delete a book that doesn't exist
 * 
 * RuntimeException means:
 * - Unchecked exception (don't need to declare in method signature)
 * - Spring will automatically handle it
 */
public class BookNotFoundException extends RuntimeException {
    
    /**
     * Constructor with message
     * 
     * @param message - Error message describing what wasn't found
     * 
     * Example usage:
     * throw new BookNotFoundException("Book not found with ID: 123");
     */
    public BookNotFoundException(String message) {
        super(message);  // Pass message to parent RuntimeException class
    }
}
