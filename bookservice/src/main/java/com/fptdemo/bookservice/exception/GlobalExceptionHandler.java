package com.fptdemo.bookservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GLOBAL EXCEPTION HANDLER
 * 
 * WHAT IS THIS?
 * - This class catches ALL exceptions thrown in the application
 * - Converts exceptions to user-friendly error responses
 * - Returns proper HTTP status codes
 * 
 * HOW IT WORKS:
 * 1. Controller/Service throws an exception
 * 2. Spring catches the exception
 * 3. Spring looks for matching @ExceptionHandler method
 * 4. Handler method creates ErrorResponse
 * 5. Spring sends ErrorResponse as JSON to client
 * 
 * @RestControllerAdvice: Makes this a global exception handler
 * - Works for ALL controllers in the application
 * - Automatically converts response to JSON
 * 
 * @ExceptionHandler: Marks method to handle specific exception type
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * HANDLE BOOK NOT FOUND EXCEPTION
     * 
     * When: BookNotFoundException is thrown
     * Returns: 404 NOT FOUND
     * 
     * @param ex - The exception that was thrown
     * @param request - The HTTP request that caused the error
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFoundException(
            BookNotFoundException ex, 
            WebRequest request) {
        
        log.error("Book not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),  // 404
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * HANDLE DUPLICATE ISBN EXCEPTION
     * 
     * When: DuplicateIsbnException is thrown
     * Returns: 409 CONFLICT
     * 
     * 409 CONFLICT means: Resource already exists
     */
    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateIsbnException(
            DuplicateIsbnException ex, 
            WebRequest request) {
        
        log.error("Duplicate ISBN: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),  // 409
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * HANDLE VALIDATION ERRORS
     * 
     * When: Request body fails @Valid validation
     * Returns: 400 BAD REQUEST
     * 
     * Example: If required field is missing or value is invalid
     * 
     * @param ex - Contains all validation errors
     * @return Map of field names and error messages
     * 
     * Example Response:
     * {
     *   "title": "Title is required",
     *   "author": "Author must be between 1 and 100 characters",
     *   "publicationYear": "Publication year cannot be in the future"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        log.error("Validation failed: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        
        // Extract each field error
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE ACCESS DENIED (Authorization Errors)
     * 
     * When: User tries to access endpoint without proper role
     * Returns: 403 FORBIDDEN
     * 
     * Example: USER trying to delete a book (only ADMIN can)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, 
            WebRequest request) {
        
        log.error("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),  // 403
                "Forbidden",
                "You don't have permission to access this resource",
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * HANDLE ILLEGAL STATE EXCEPTION
     * 
     * When: Business logic error (e.g., no copies available)
     * Returns: 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, 
            WebRequest request) {
        
        log.error("Illegal state: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),  // 400
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE ALL OTHER EXCEPTIONS
     * 
     * This is a catch-all handler for any exception not handled above
     * Returns: 500 INTERNAL SERVER ERROR
     * 
     * This prevents exposing internal error details to clients
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, 
            WebRequest request) {
        
        log.error("Unexpected error occurred: ", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 500
                "Internal Server Error",
                "An unexpected error occurred. Please contact support.",
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
