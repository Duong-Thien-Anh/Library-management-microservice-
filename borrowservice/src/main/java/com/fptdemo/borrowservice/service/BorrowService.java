package com.fptdemo.borrowservice.service;

import com.fptdemo.borrowservice.client.BookClient;
import com.fptdemo.borrowservice.dto.BookDto;
import com.fptdemo.borrowservice.dto.BorrowRequest;
import com.fptdemo.borrowservice.dto.BorrowResponse;
import com.fptdemo.borrowservice.entity.BorrowRecord;
import com.fptdemo.borrowservice.exception.BorrowNotAllowedException;
import com.fptdemo.borrowservice.exception.ResourceNotFoundException;
import com.fptdemo.borrowservice.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordRepository borrowRepo;
    private final BookClient bookClient;

    // ---------------------------------------------------------------
    // BORROW a book
    // ---------------------------------------------------------------
    @Transactional
    public BorrowResponse borrowBook(String username, BorrowRequest req, String authHeader) {

        Long bookId = req.getBookId();

        // 1. Check book exists and is available (call book-service via Feign)
        BookDto book;
        try {
            book = bookClient.getBookById(bookId, authHeader);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Book with id " + bookId + " not found in book-service.");
        }

        if (!book.isAvailable()) {
            throw new BorrowNotAllowedException("Book '" + book.getTitle() + "' is currently not available.");
        }

        // 2. Prevent the same user from borrowing the same book twice
        if (borrowRepo.existsByUsernameAndBookIdAndStatus(username, bookId, BorrowRecord.BorrowStatus.BORROWED)) {
            throw new BorrowNotAllowedException("You already have this book borrowed.");
        }

        // 3. Decrease stock in book-service
        bookClient.updateBookAvailability(bookId, false, authHeader );

        // 4. Save borrow records
        BorrowRecord records = BorrowRecord.builder()
                .username(username)
                .bookId(bookId)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(req.getDurationDays()))
                .status(BorrowRecord.BorrowStatus.BORROWED)
                .build();

        borrowRepo.save(records);

        log.info("User '{}' borrowed book id={}", username, bookId);
        return toResponse(records, book.getTitle());
    }

    // ---------------------------------------------------------------
    // RETURN a book
    // ---------------------------------------------------------------
    @Transactional
    public BorrowResponse returnBook(String username, Long borrowRecordId, String authHeader) {

        BorrowRecord records = borrowRepo.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow records not found with id: " + borrowRecordId));

        // Only the owner can return
        if (!records.getUsername().equals(username)) {
            throw new BorrowNotAllowedException("You are not allowed to return a book you did not borrow.");
        }

        if (records.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            throw new BorrowNotAllowedException("This book has already been returned.");
        }

        // Increase stock in book-service
        bookClient.updateBookAvailability(records.getBookId(), true, authHeader);

        records.setReturnDate(LocalDate.now());
        records.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        borrowRepo.save(records);

        log.info("User '{}' returned borrow records id={}", username, borrowRecordId);

        // Try to get book title for response (non-critical)
        String title = fetchBookTitle(records.getBookId(), authHeader);
        return toResponse(records, title);
    }

    // ---------------------------------------------------------------
    // GET all borrow records for the current user
    // ---------------------------------------------------------------
    public List<BorrowResponse> getMyBorrows(String username, String authHeader) {
        return borrowRepo.findByUsername(username).stream()
                .map(r -> toResponse(r, fetchBookTitle(r.getBookId(), authHeader)))
                .toList();
    }

    // ---------------------------------------------------------------
    // GET single borrow records by ID (admin or owner)
    // ---------------------------------------------------------------
    public BorrowResponse getBorrowById(Long id, String authHeader) {
        BorrowRecord records = borrowRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow records not found with id: " + id));
        String title = fetchBookTitle(records.getBookId(), authHeader);
        return toResponse(records, title);
    }

    // ---------------------------------------------------------------
    // GET all borrow records (admin use)
    // ---------------------------------------------------------------
    public List<BorrowResponse> getAllBorrows(String authHeader) {
        return borrowRepo.findAll().stream()
                .map(r -> toResponse(r, fetchBookTitle(r.getBookId(), authHeader)))
                .toList();
    }

    // ---------------------------------------------------------------
    // Helper: map entity → response DTO
    // ---------------------------------------------------------------
    private BorrowResponse toResponse(BorrowRecord records, String bookTitle) {
        return BorrowResponse.builder()
                .id(records.getId())
                .username(records.getUsername())
                .bookId(records.getBookId())
                .bookTitle(bookTitle)
                .borrowDate(records.getBorrowDate())
                .dueDate(records.getDueDate())
                .returnDate(records.getReturnDate())
                .status(records.getStatus())
                .build();
    }

    // Helper: fetch title from book-service (returns "Unknown" on failure)
    private String fetchBookTitle(Long bookId, String authHeader) {
        try {
            return bookClient.getBookById(bookId, authHeader).getTitle();
        } catch (Exception e) {
            log.warn("Could not fetch book title for id={}: {}", bookId, e.getMessage());
            return "Unknown";
        }
    }
}

