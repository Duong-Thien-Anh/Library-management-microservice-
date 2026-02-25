package com.fptdemo.borrowservice.client;

import com.fptdemo.borrowservice.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client that communicates with book-service.
 * "bookservice" must match the spring.application.name of that service.
 */
@FeignClient(name = "bookservice")
public interface BookClient {

    /**
     * Get a single book by ID.
     * We forward the original JWT token so book-service security can pass through.
     */
    @GetMapping("/api/books/{id}")
    BookDto getBookById(@PathVariable("id") Long id,
                        @RequestHeader("Authorization") String authHeader);

    /**
     * Decrease book stock by 1 (called when borrowing).
     */
    @PutMapping("/api/books/{id}/decrease-stock")
    void decreaseStock(@PathVariable("id") Long id,
                       @RequestHeader("Authorization") String authHeader);

    /**
     * Increase book stock by 1 (called when returning).
     */
    @PutMapping("/api/books/{id}/increase-stock")
    void increaseStock(@PathVariable("id") Long id,
                       @RequestHeader("Authorization") String authHeader);
}

