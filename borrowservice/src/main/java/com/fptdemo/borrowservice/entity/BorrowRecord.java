package com.fptdemo.borrowservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Represents one borrow record: a user borrows a book.
 */
@Entity
@Table(name = "borrow_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username taken from the JWT token */
    @Column(nullable = false)
    private String username;

    /** ID of the book (from book-service) */
    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private LocalDate borrowDate;

    private LocalDate dueDate;

    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status;

    public enum BorrowStatus {
        BORROWED,   // currently borrowed
        RETURNED,   // returned on time
        OVERDUE     // not returned and past due date
    }
}

