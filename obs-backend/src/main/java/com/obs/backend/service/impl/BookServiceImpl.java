package com.obs.backend.service.impl;

import com.obs.backend.dto.BookRequest;
import com.obs.backend.dto.BookResponse;
import com.obs.backend.entity.Author;
import com.obs.backend.entity.Book;
import com.obs.backend.entity.Category;
import com.obs.backend.repository.AuthorRepository;
import com.obs.backend.repository.BookRepository;
import com.obs.backend.repository.CategoryRepository;
import com.obs.backend.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(BookResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
        return BookResponse.from(book);
    }

    @Override
    public BookResponse createBook(BookRequest request) {
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + request.getAuthorId()));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.getCategoryId()));

        Book book = Book.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .isbn(request.getIsbn())
                .publishedDate(request.getPublishedDate())
                .author(author)
                .category(category)
                .stock(request.getStock())
                .build();

        return BookResponse.from(bookRepository.save(book));
    }

    @Override
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + request.getAuthorId()));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.getCategoryId()));

        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());
        book.setIsbn(request.getIsbn());
        book.setPublishedDate(request.getPublishedDate());
        book.setAuthor(author);
        book.setCategory(category);
        book.setStock(request.getStock());

        return BookResponse.from(bookRepository.save(book));
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }
}
