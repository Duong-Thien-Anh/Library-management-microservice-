package com.fptdemo.bookservice.dto;

import com.fptdemo.bookservice.entity.Book;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BOOK REQUEST DTO (Data Transfer Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequestDTO {

    /**
     * BOOK TITLE
     * @NotBlank - Must have content (not just whitespace)
     * @Size - Must be between 1 and 200 characters
     */
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    /**
     * BOOK AUTHOR
     */
    @NotBlank(message = "Author is required")
    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    private String author;

    /**
     * ISBN NUMBER
     * @Pattern - Validates ISBN format (13 digits)
     */
    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", 
             message = "ISBN must be valid (10 or 13 digits)")
    private String isbn;

    /**
     * PUBLISHER
     */
    @Size(max = 100, message = "Publisher must not exceed 100 characters")
    private String publisher;

    /**
     * PUBLICATION YEAR
     * @Min - Cannot be before year 1000
     * @Max - Cannot be in the future
     */
    @Min(value = 1000, message = "Publication year must be after 1000")
    @Max(value = 2100, message = "Publication year cannot be in the future")
    private Integer publicationYear;

    /**
     * CATEGORY/GENRE
     */
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    /**
     * DESCRIPTION
     */
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    /**
     * TOTAL COPIES
     * @NotNull - Must provide a value
     * @Min - Must have at least 1 copy
     */
    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    /**
     * AVAILABLE COPIES
     * This should not exceed total copies
     */
    @NotNull(message = "Available copies is required")
    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    /**
     * BOOK STATUS
     */
    private Book.BookStatus status;

    /**
     * IMAGE URL
     */
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
}
