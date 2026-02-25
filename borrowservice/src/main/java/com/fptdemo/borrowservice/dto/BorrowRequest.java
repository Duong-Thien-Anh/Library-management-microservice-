package com.fptdemo.borrowservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body when a user wants to borrow a book.
 */
@Data
public class BorrowRequest {

    @NotNull(message = "bookId must not be null")
    private Long bookId;

    /** How many days the user wants to borrow the book (default 14) */
    private int durationDays = 14;
}

