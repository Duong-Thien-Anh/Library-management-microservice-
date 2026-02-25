package com.fptdemo.borrowservice.controller;

import com.fptdemo.borrowservice.dto.BorrowRequest;
import com.fptdemo.borrowservice.dto.BorrowResponse;
import com.fptdemo.borrowservice.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for borrow operations.
 *
 * Base URL: /api/borrows
 *
 * @AuthenticationPrincipal – Spring Security injects the username
 *   that was set by JwtAuthFilter (the JWT "sub" claim).
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /**
     * POST /api/borrows
     * Borrow a book. Body: { "bookId": 1, "durationDays": 14 }
     */
    @PostMapping
    public ResponseEntity<BorrowResponse> borrowBook(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody BorrowRequest request,
            @RequestHeader("Authorization") String authHeader) {

        BorrowResponse response = borrowService.borrowBook(username, request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/borrows/{id}/return
     * Return a borrowed book.
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowResponse> returnBook(
            @AuthenticationPrincipal String username,
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        BorrowResponse response = borrowService.returnBook(username, id, authHeader);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/borrows/my
     * Get all borrow records for the logged-in user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<BorrowResponse>> getMyBorrows(
            @AuthenticationPrincipal String username,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(borrowService.getMyBorrows(username, authHeader));
    }

    /**
     * GET /api/borrows/{id}
     * Get a single borrow record by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BorrowResponse> getBorrowById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(borrowService.getBorrowById(id, authHeader));
    }

    /**
     * GET /api/borrows
     * Get ALL borrow records (admin use).
     */
    @GetMapping
    public ResponseEntity<List<BorrowResponse>> getAllBorrows(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(borrowService.getAllBorrows(authHeader));
    }

//    @GetMapping("/health")
//    public ResponseEntity<String> healthCheck() {
//        return ResponseEntity.ok("Borrow Service is running! 📚");
//    }

}

