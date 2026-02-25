package com.fptdemo.bookservice.dto;

import java.time.LocalDateTime;

import com.fptdemo.bookservice.entity.Book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BOOK RESPONSE DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDTO {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private Integer publicationYear;
    private String category;
    private String description;
    private Integer totalCopies;
    private Integer availableCopies;
    private Book.BookStatus status;
    private String imageUrl;
    
    // Metadata fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed field - tells if book can be borrowed
    private Boolean isAvailable;

    /**
     * CONVENIENCE CONSTRUCTOR
     * Converts Book entity to BookResponseDTO
     * 
     * @param book - The Book entity to convert
     * 
     * This constructor is used in the service layer to convert
     * database entities to DTOs before sending to client.
     */
    public BookResponseDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.isbn = book.getIsbn();
        this.publisher = book.getPublisher();
        this.publicationYear = book.getPublicationYear();
        this.category = book.getCategory();
        this.description = book.getDescription();
        this.totalCopies = book.getTotalCopies();
        this.availableCopies = book.getAvailableCopies();
        this.status = book.getStatus();
        this.imageUrl = book.getImageUrl();
        this.createdAt = book.getCreatedAt();
        this.updatedAt = book.getUpdatedAt();
        // Computed field: Book is available if there are copies and status is AVAILABLE
        this.isAvailable = book.getAvailableCopies() != null 
                        && book.getAvailableCopies() > 0 
                        && book.getStatus() == Book.BookStatus.AVAILABLE;
    }
}
