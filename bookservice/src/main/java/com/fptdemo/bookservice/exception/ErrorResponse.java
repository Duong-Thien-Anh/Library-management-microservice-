package com.fptdemo.bookservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ERROR RESPONSE DTO
 * 
 * This class defines the structure of error responses sent to clients
 * 
 * When an error occurs, instead of sending a generic error,
 * we send a structured JSON response with detailed information
 * 
 * Example Error Response:
 * {
 *   "timestamp": "2026-02-18T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Book not found with ID: 123",
 *   "path": "/api/books/v1/123"
 * }
 * 
 * This helps:
 * - Frontend developers know exactly what went wrong
 * - Debugging becomes easier
 * - Provides consistent error format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private LocalDateTime timestamp;  // When error occurred
    private int status;                // HTTP status code (404, 400, 500, etc.)
    private String error;              // Error type (Not Found, Bad Request, etc.)
    private String message;            // Detailed error message
    private String path;               // URL path where error occurred
}
