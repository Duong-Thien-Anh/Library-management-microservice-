package com.fptdemo.borrowservice.client;

import com.fptdemo.borrowservice.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client that communicates with book-service.
 * "bookservice" must match the spring.application.name of that service.
 */
@FeignClient(name = "BOOK-SERVICE" , path = "/api/books/v1")
public interface BookClient {
    /**
     * Get a single book by ID.
     * We forward the original JWT token so book-service security can pass through.
     */
    @GetMapping("/{id}")
    BookDto getBookById(@PathVariable("id") Long id,
                        @RequestHeader("Authorization") String authHeader);

    @PatchMapping("/{id}/availability")
    void updateBookAvailability(
            @PathVariable("id") Long id,
            @RequestParam("increment") boolean increment,
            @RequestHeader("Authorization") String authHeader);
}

