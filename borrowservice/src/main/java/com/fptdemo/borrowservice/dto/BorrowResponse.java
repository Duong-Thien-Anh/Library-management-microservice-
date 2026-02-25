package com.fptdemo.borrowservice.dto;

import com.fptdemo.borrowservice.entity.BorrowRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response returned to the client for any borrow-record operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowResponse {

    private Long id;
    private String username;
    private Long bookId;
    private String bookTitle;   // fetched from book-service via Feign
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowRecord.BorrowStatus status;
}

