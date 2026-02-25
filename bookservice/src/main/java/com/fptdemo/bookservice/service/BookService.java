package com.fptdemo.bookservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fptdemo.bookservice.dto.BookRequestDTO;
import com.fptdemo.bookservice.dto.BookResponseDTO;
import com.fptdemo.bookservice.entity.Book;
import com.fptdemo.bookservice.exception.BookNotFoundException;
import com.fptdemo.bookservice.exception.DuplicateIsbnException;
import com.fptdemo.bookservice.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    // Dependency Injection - Spring automatically injects this
    private final BookRepository bookRepository;

    @Transactional
    public BookResponseDTO createBook(BookRequestDTO requestDTO) {
        log.info("Creating new book with title: {}", requestDTO.getTitle());
        

        if (requestDTO.getIsbn() != null && bookRepository.existsByIsbn(requestDTO.getIsbn())) {
            log.error("Book with ISBN {} already exists", requestDTO.getIsbn());
            throw new DuplicateIsbnException("Book with ISBN " + requestDTO.getIsbn() + " already exists");
        }
        

        Book book = convertToEntity(requestDTO);
        

        if (book.getStatus() == null) {
            book.setStatus(book.getAvailableCopies() > 0 ? 
                          Book.BookStatus.AVAILABLE : 
                          Book.BookStatus.UNAVAILABLE);
        }
        
        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully with ID: {}", savedBook.getId());
        
        return new BookResponseDTO(savedBook);
    }

    /**
     * GET BOOK BY ID
     */
    @SuppressWarnings("null")
    public BookResponseDTO getBookById(Long id) {
        log.info("Fetching book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        
        return new BookResponseDTO(book);
    }

    /**
     * GET ALL BOOKS
     */
    public List<BookResponseDTO> getAllBooks() {
        log.info("Fetching all books");
        
        return bookRepository.findAll()
                .stream()
                .map(BookResponseDTO::new)  // Same as: book -> new BookResponseDTO(book)
                .collect(Collectors.toList());
    }

    /**
     * UPDATE BOOK
     */
    @Transactional
    @SuppressWarnings("null")
    public BookResponseDTO updateBook(Long id, BookRequestDTO requestDTO) {
        log.info("Updating book with ID: {}", id);
        
        // Find existing book
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        
        // Check if ISBN is being changed to one that already exists
        if (requestDTO.getIsbn() != null && 
            !requestDTO.getIsbn().equals(existingBook.getIsbn()) && 
            bookRepository.existsByIsbn(requestDTO.getIsbn())) {
            throw new DuplicateIsbnException("Book with ISBN " + requestDTO.getIsbn() + " already exists");
        }
        
        // Update fields (keep existing values if new value is null)
        updateBookFields(existingBook, requestDTO);
        
        // Save updated book (Hibernate generates UPDATE SQL)
        Book updatedBook = bookRepository.save(existingBook);
        log.info("Book updated successfully with ID: {}", updatedBook.getId());
        
        return new BookResponseDTO(updatedBook);
    }

    /**
     * DELETE BOOK
     */
    @Transactional
    @SuppressWarnings("null")
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);
        
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with ID: " + id);
        }
        
        bookRepository.deleteById(id);
        log.info("Book deleted successfully with ID: {}", id);
    }

    /**
     * SEARCH BOOKS
     */
    public List<BookResponseDTO> searchBooks(String searchTerm) {
        log.info("Searching books with term: {}", searchTerm);
        
        return bookRepository.searchBooks(searchTerm)
                .stream()
                .map(BookResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * GET BOOKS BY CATEGORY
     */
    public List<BookResponseDTO> getBooksByCategory(String category) {
        log.info("Fetching books by category: {}", category);
        
        return bookRepository.findByCategory(category)
                .stream()
                .map(BookResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * GET BOOKS BY AUTHOR
     */
    public List<BookResponseDTO> getBooksByAuthor(String author) {
        log.info("Fetching books by author: {}", author);
        
        return bookRepository.findByAuthorContainingIgnoreCase(author)
                .stream()
                .map(BookResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * GET AVAILABLE BOOKS
     * 
     * Returns only books that can be borrowed
     */
    public List<BookResponseDTO> getAvailableBooks() {
        log.info("Fetching available books");
        
        return bookRepository.findByStatusAndAvailableCopiesGreaterThan(
                Book.BookStatus.AVAILABLE, 0)
                .stream()
                .map(BookResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * UPDATE BOOK AVAILABILITY

     */
    @Transactional
    @SuppressWarnings("null")
    public void updateBookAvailability(Long bookId, boolean increment) {
        log.info("Updating availability for book ID: {}, increment: {}", bookId, increment);
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));
        
        if (increment) {
            // Book is returned - increase available copies
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            if (book.getAvailableCopies() > 0) {
                book.setStatus(Book.BookStatus.AVAILABLE);
            }
        } else {
            // Book is borrowed - decrease available copies
            if (book.getAvailableCopies() <= 0) {
                throw new IllegalStateException("No copies available for book: " + book.getTitle());
            }
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            if (book.getAvailableCopies() == 0) {
                book.setStatus(Book.BookStatus.UNAVAILABLE);
            }
        }
        
        bookRepository.save(book);
        log.info("Book availability updated. Available copies: {}", book.getAvailableCopies());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Convert BookRequestDTO to Book Entity
     */
    private Book convertToEntity(BookRequestDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublisher(dto.getPublisher());
        book.setPublicationYear(dto.getPublicationYear());
        book.setCategory(dto.getCategory());
        book.setDescription(dto.getDescription());
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getAvailableCopies());
        book.setStatus(dto.getStatus());
        book.setImageUrl(dto.getImageUrl());
        return book;
    }

    /**
     * Update existing book with new data from DTO
     */
    private void updateBookFields(Book book, BookRequestDTO dto) {
        if (dto.getTitle() != null) book.setTitle(dto.getTitle());
        if (dto.getAuthor() != null) book.setAuthor(dto.getAuthor());
        if (dto.getIsbn() != null) book.setIsbn(dto.getIsbn());
        if (dto.getPublisher() != null) book.setPublisher(dto.getPublisher());
        if (dto.getPublicationYear() != null) book.setPublicationYear(dto.getPublicationYear());
        if (dto.getCategory() != null) book.setCategory(dto.getCategory());
        if (dto.getDescription() != null) book.setDescription(dto.getDescription());
        if (dto.getTotalCopies() != null) book.setTotalCopies(dto.getTotalCopies());
        if (dto.getAvailableCopies() != null) book.setAvailableCopies(dto.getAvailableCopies());
        if (dto.getStatus() != null) book.setStatus(dto.getStatus());
        if (dto.getImageUrl() != null) book.setImageUrl(dto.getImageUrl());
    }
}
