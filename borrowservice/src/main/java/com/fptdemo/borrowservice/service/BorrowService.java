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
        bookClient.decreaseStock(bookId, authHeader);

        // 4. Save borrow record
        BorrowRecord record = BorrowRecord.builder()
                .username(username)
                .bookId(bookId)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(req.getDurationDays()))
                .status(BorrowRecord.BorrowStatus.BORROWED)
                .build();

        borrowRepo.save(record);

        log.info("User '{}' borrowed book id={}", username, bookId);
        return toResponse(record, book.getTitle());
    }

    // ---------------------------------------------------------------
    // RETURN a book
    // ---------------------------------------------------------------
    @Transactional
    public BorrowResponse returnBook(String username, Long borrowRecordId, String authHeader) {

        BorrowRecord record = borrowRepo.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found with id: " + borrowRecordId));

        // Only the owner can return
        if (!record.getUsername().equals(username)) {
            throw new BorrowNotAllowedException("You are not allowed to return a book you did not borrow.");
        }

        if (record.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            throw new BorrowNotAllowedException("This book has already been returned.");
        }

        // Increase stock in book-service
        bookClient.increaseStock(record.getBookId(), authHeader);

        record.setReturnDate(LocalDate.now());
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        borrowRepo.save(record);

        log.info("User '{}' returned borrow record id={}", username, borrowRecordId);

        // Try to get book title for response (non-critical)
        String title = fetchBookTitle(record.getBookId(), authHeader);
        return toResponse(record, title);
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
    // GET single borrow record by ID (admin or owner)
    // ---------------------------------------------------------------
    public BorrowResponse getBorrowById(Long id, String authHeader) {
        BorrowRecord record = borrowRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found with id: " + id));
        String title = fetchBookTitle(record.getBookId(), authHeader);
        return toResponse(record, title);
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
    private BorrowResponse toResponse(BorrowRecord record, String bookTitle) {
        return BorrowResponse.builder()
                .id(record.getId())
                .username(record.getUsername())
                .bookId(record.getBookId())
                .bookTitle(bookTitle)
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
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

