package com.fptdemo.bookservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fptdemo.bookservice.dto.BookRequestDTO;
import com.fptdemo.bookservice.dto.BookResponseDTO;
import com.fptdemo.bookservice.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/v1")  // Base path: /api/books/v1
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")  // Allow all origins (change in production!)
public class BookController {

    // Dependency Injection
    private final BookService bookService;

    /**
     * CREATE NEW BOOK
     * 
     * Endpoint: POST /api/books/v1
     * 
     * REQUEST:
     * - Method: POST
     * - Body: JSON with book details
     * - Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * RESPONSE:
     * - Status: 201 CREATED
     * - Body: Created book with ID
     * 
     * Example Request Body:
     * {
     *   "title": "Clean Code",
     *   "author": "Robert C. Martin",
     *   "isbn": "9780132350884",
     *   "publisher": "Prentice Hall",
     *   "publicationYear": 2008,
     *   "category": "Programming",
     *   "description": "A handbook of agile software craftsmanship",
     *   "totalCopies": 5,
     *   "availableCopies": 5
     * }
     * 
     * @param requestDTO - Book data from request body
     * @return ResponseEntity with created book
     * 
     * @Valid: Validates request data against constraints in DTO
     * @RequestBody: Tells Spring to parse JSON body to Java object
     * @PreAuthorize: Only ADMIN can create books
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // Only admins can create books
    public ResponseEntity<BookResponseDTO> createBook(
            @Valid @RequestBody BookRequestDTO requestDTO) {
        
        log.info("REST request to create book: {}", requestDTO.getTitle());
        
        BookResponseDTO createdBook = bookService.createBook(requestDTO);
        
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    /**
     * GET BOOK BY ID
     * 
     * Endpoint: GET /api/books/v1/{id}
     * 
     * Example: GET /api/books/v1/1
     * 
     * RESPONSE:
     * - Status: 200 OK
     * - Body: Book details
     * 
     * @param id - Book ID from URL path
     * @return ResponseEntity with book details
     * 
     * @PathVariable: Extracts {id} from URL
     * @PreAuthorize: All authenticated users can view books
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // Users and admins can view
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        log.info("REST request to get book by ID: {}", id);
        
        BookResponseDTO book = bookService.getBookById(id);
        
        return ResponseEntity.ok(book);
    }

    /**
     * GET ALL BOOKS
     * 
     * Endpoint: GET /api/books/v1
     * 
     * RESPONSE:
     * - Status: 200 OK
     * - Body: List of all books
     * 
     * Example Response:
     * [
     *   {
     *     "id": 1,
     *     "title": "Clean Code",
     *     "author": "Robert C. Martin",
     *     ...
     *   },
     *   {
     *     "id": 2,
     *     "title": "Effective Java",
     *     ...
     *   }
     * ]
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        log.info("REST request to get all books");
        
        List<BookResponseDTO> books = bookService.getAllBooks();
        
        return ResponseEntity.ok(books);
    }

    /**
     * UPDATE BOOK
     * 
     * Endpoint: PUT /api/books/v1/{id}
     * 
     * Example: PUT /api/books/v1/1
     * 
     * REQUEST:
     * - Method: PUT
     * - Body: Updated book details
     * - Headers: Authorization: Bearer <JWT_TOKEN>
     * 
     * RESPONSE:
     * - Status: 200 OK
     * - Body: Updated book
     * 
     * @param id - Book ID to update
     * @param requestDTO - Updated book data
     * @return ResponseEntity with updated book
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // Only admins can update books
    public ResponseEntity<BookResponseDTO> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO requestDTO) {
        
        log.info("REST request to update book ID: {}", id);
        
        BookResponseDTO updatedBook = bookService.updateBook(id, requestDTO);
        
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * DELETE BOOK
     * 
     * Endpoint: DELETE /api/books/v1/{id}
     * 
     * Example: DELETE /api/books/v1/1
     * 
     * RESPONSE:
     * - Status: 204 NO CONTENT
     * - Body: Empty
     * 
     * @param id - Book ID to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // Only admins can delete books
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("REST request to delete book ID: {}", id);
        
        bookService.deleteBook(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * SEARCH BOOKS
     * 
     * Endpoint: GET /api/books/v1/search?term={searchTerm}
     * 
     * Example: GET /api/books/v1/search?term=Java
     * 
     * Searches books by title, author, or category
     * 
     * @param  term - Search term from query parameter
     * @return List of matching books
     * 
     * @RequestParam: Extracts query parameter from URL
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BookResponseDTO>> searchBooks(
            @RequestParam String term) {
        
        log.info("REST request to search books with term: {}", term);
        
        List<BookResponseDTO> books = bookService.searchBooks(term);
        
        return ResponseEntity.ok(books);
    }

    /**
     * GET BOOKS BY CATEGORY
     * 
     * Endpoint: GET /api/books/v1/category/{category}
     * 
     * Example: GET /api/books/v1/category/Programming
     * 
     * @param category - Category name
     * @return List of books in that category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BookResponseDTO>> getBooksByCategory(
            @PathVariable String category) {
        
        log.info("REST request to get books by category: {}", category);
        
        List<BookResponseDTO> books = bookService.getBooksByCategory(category);
        
        return ResponseEntity.ok(books);
    }

    /**
     * GET BOOKS BY AUTHOR
     * 
     * Endpoint: GET /api/books/v1/author/{author}
     * 
     * Example: GET /api/books/v1/author/Robert
     * 
     * @param author - Author name
     * @return List of books by that author
     */
    @GetMapping("/author/{author}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BookResponseDTO>> getBooksByAuthor(
            @PathVariable String author) {
        
        log.info("REST request to get books by author: {}", author);
        
        List<BookResponseDTO> books = bookService.getBooksByAuthor(author);
        
        return ResponseEntity.ok(books);
    }

    /**
     * GET AVAILABLE BOOKS
     * 
     * Endpoint: GET /api/books/v1/available
     * 
     * Returns only books that can be borrowed
     * 
     * @return List of available books
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BookResponseDTO>> getAvailableBooks() {
        log.info("REST request to get available books");
        
        List<BookResponseDTO> books = bookService.getAvailableBooks();
        
        return ResponseEntity.ok(books);
    }

    /**
     * UPDATE BOOK AVAILABILITY
     * 
     * Endpoint: PATCH /api/books/v1/{id}/availability?increment={true/false}
     * 
     * This endpoint is called by the Borrow Service
     * 
     * @param id - Book ID
     * @param increment - true to increase, false to decrease
     * @return ResponseEntity with no content
     */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")  // Only internal services (admin) can call this
    public ResponseEntity<Void> updateBookAvailability(
            @PathVariable Long id,
            @RequestParam boolean increment) {
        
        log.info("REST request to update availability for book ID: {}, increment: {}", id, increment);
        
        bookService.updateBookAvailability(id, increment);
        
        return ResponseEntity.ok().build();
    }


}
