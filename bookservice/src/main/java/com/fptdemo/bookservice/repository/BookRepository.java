package com.fptdemo.bookservice.repository;

import com.fptdemo.bookservice.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BOOK REPOSITORY INTERFACE
 * 
 * REPOSITORY PATTERN EXPLANATION:
 * - Repository handles all database operations
 * - It's an abstraction layer between database and business logic
 * - Spring Data JPA automatically implements this interface
 * 
 * JpaRepository<Book, Long> means:
 * - Book: The entity type this repository manages
 * - Long: The type of the primary key (id field)
 * 
 * WHAT SPRING DATA JPA PROVIDES AUTOMATICALLY:
 * - save(): Save or update a book
 * - findById(): Find book by ID
 * - findAll(): Get all books
 * - deleteById(): Delete book by ID
 * - count(): Count total books
 * - existsById(): Check if book exists
 * 
 * CUSTOM METHODS:
 * Spring Data JPA can automatically implement methods based on method names!
 * 
 * METHOD NAMING CONVENTION:
 * - findBy[FieldName]: Find by specific field
 * - findBy[Field]And[Field]: Find by multiple fields
 * - findBy[Field]Containing: Find by partial match
 * - findBy[Field]OrderBy[Field]: Find and sort
 * 
 * @Repository: Marks this as a repository component
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * FIND BOOK BY ISBN
     * 
     * Spring Data JPA automatically implements this method!
     * It generates SQL: SELECT * FROM books WHERE isbn = ?
     * 
     * @param isbn - The ISBN to search for
     * @return Optional<Book> - Book if found, empty if not found
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * FIND BOOKS BY AUTHOR
     * 
     * Auto-generated SQL: SELECT * FROM books WHERE author = ?
     * 
     * @param author - Author name to search
     * @return List<Book> - All books by this author
     */
    List<Book> findByAuthor(String author);

    /**
     * FIND BOOKS BY CATEGORY
     * 
     * Auto-generated SQL: SELECT * FROM books WHERE category = ?
     */
    List<Book> findByCategory(String category);

    /**
     * FIND BOOKS BY STATUS
     * 
     * Auto-generated SQL: SELECT * FROM books WHERE status = ?
     */
    List<Book> findByStatus(Book.BookStatus status);

    /**
     * SEARCH BOOKS BY TITLE (PARTIAL MATCH)
     * 
     * 'Containing' keyword enables partial/fuzzy search
     * Auto-generated SQL: SELECT * FROM books WHERE title LIKE %?%
     * 
     * Example: If title = "Java", it finds:
     * - "Java Programming"
     * - "Learning Java"
     * - "Advanced Java Concepts"
     */
    List<Book> findByTitleContaining(String title);

    /**
     * SEARCH BOOKS BY AUTHOR (PARTIAL MATCH)
     * 
     * 'IgnoreCase' makes search case-insensitive
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * FIND AVAILABLE BOOKS
     * 
     * Combines multiple conditions
     * Auto-generated SQL: 
     * SELECT * FROM books 
     * WHERE status = 'AVAILABLE' 
     * AND available_copies > 0
     */
    List<Book> findByStatusAndAvailableCopiesGreaterThan(
        Book.BookStatus status, 
        Integer minCopies
    );

    /**
     * CUSTOM JPQL QUERY - SEARCH BOOKS
     * 
     * When method naming is complex, use @Query annotation
     * 
     * JPQL (Java Persistence Query Language):
     * - Similar to SQL but uses entity names (Book) instead of table names
     * - Uses entity field names instead of column names
     * 
     * This query searches for books matching title OR author OR category
     * LOWER() makes search case-insensitive
     * 
     * @param searchTerm - The term to search for
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Book> searchBooks(@Param("searchTerm") String searchTerm);

    /**
     * FIND BOOKS BY PUBLICATION YEAR RANGE
     * 
     * Finds books published between start and end year
     */
    List<Book> findByPublicationYearBetween(Integer startYear, Integer endYear);

    /**
     * COUNT AVAILABLE BOOKS
     * 
     * Returns number of books that can be borrowed
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.status = 'AVAILABLE' AND b.availableCopies > 0")
    Long countAvailableBooks();

    /**
     * CHECK IF ISBN EXISTS
     * 
     * Useful for validating unique ISBN before saving
     */
    boolean existsByIsbn(String isbn);
}
