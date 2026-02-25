package com.fptdemo.bookservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * BOOK ENTITY CLASS
 * 
 * This class represents the "books" table in the MySQL database.
 * Each instance of this class represents one row in the database.
 * 
 * ANNOTATIONS EXPLAINED:
 * @Entity - Tells JPA this is a database table
 * @Table - Specifies the table name (optional, defaults to class name)
 * @Data - Lombok annotation: Automatically generates getters, setters, toString, equals, hashCode
 * @NoArgsConstructor - Lombok: Generates a constructor with no parameters
 * @AllArgsConstructor - Lombok: Generates a constructor with all parameters
 * 
 * WORKFLOW:
 * 1. When application starts, Hibernate creates this table in MySQL
 * 2. When you save a Book object, it becomes a row in the database
 * 3. When you query, database rows are converted back to Book objects
 */
@Entity  // This is a JPA entity (database table)
@Table(name = "books")  // Table name in database
@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor  // Lombok: Generates empty constructor
@AllArgsConstructor  // Lombok: Generates constructor with all fields
public class Book {

    /**
     * PRIMARY KEY
     * @Id - Marks this as the primary key
     * @GeneratedValue - Auto-generates the value
     * GenerationType.IDENTITY - Uses database auto-increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * BOOK TITLE
     * @Column - Specifies column properties
     * nullable = false - This field cannot be null (required)
     * length = 200 - Maximum length of 200 characters
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * BOOK AUTHOR
     */
    @Column(nullable = false, length = 100)
    private String author;

    /**
     * ISBN (International Standard Book Number)
     * unique = true - No two books can have the same ISBN
     */
    @Column(unique = true, length = 20)
    private String isbn;

    /**
     * BOOK PUBLISHER
     */
    @Column(length = 100)
    private String publisher;

    /**
     * PUBLICATION YEAR
     */
    @Column(name = "publication_year")
    private Integer publicationYear;

    /**
     * BOOK CATEGORY/GENRE
     * Examples: Fiction, Science, History, Technology, etc.
     */
    @Column(length = 50)
    private String category;

    /**
     * BOOK DESCRIPTION
     * @Lob - Large Object, for storing long text
     * columnDefinition = "TEXT" - MySQL TEXT type (up to 65,535 characters)
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * TOTAL COPIES AVAILABLE
     * Tracks how many copies of this book the library has
     */
    @Column(name = "total_copies")
    private Integer totalCopies;

    /**
     * AVAILABLE COPIES
     * Tracks how many copies are currently available (not borrowed)
     */
    @Column(name = "available_copies")
    private Integer availableCopies;

    /**
     * BOOK STATUS
     * Enum for book availability status
     */
    @Enumerated(EnumType.STRING)  // Store enum as string in database
    @Column(nullable = false)
    private BookStatus status = BookStatus.AVAILABLE;

    /**
     * BOOK IMAGE URL
     * Stores URL or path to book cover image
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * CREATED TIMESTAMP
     * @CreationTimestamp - Automatically sets this when record is created
     * updatable = false - Cannot be changed after creation
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * UPDATED TIMESTAMP
     * @UpdateTimestamp - Automatically updates this when record is modified
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * BOOK STATUS ENUM
     * Defines possible status values for a book
     */
    public enum BookStatus {
        AVAILABLE,      // Book is available for borrowing
        UNAVAILABLE,    // All copies are borrowed
        MAINTENANCE,    // Book is under repair/maintenance
        RETIRED         // Book is no longer in circulation
    }
}
