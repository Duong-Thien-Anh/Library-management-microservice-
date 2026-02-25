package com.fptdemo.borrowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal representation of a Book returned from book-service.
 * Only the fields we actually need are mapped here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private boolean available;   // true = stock > 0
}

