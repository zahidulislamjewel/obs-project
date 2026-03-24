package com.obs.backend.service.impl;

import com.obs.backend.dto.BookRequest;
import com.obs.backend.dto.BookResponse;
import com.obs.backend.entity.Author;
import com.obs.backend.entity.Book;
import com.obs.backend.entity.BookAuthor;
import com.obs.backend.entity.BookCategory;
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

import java.util.List;

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
        List<Author> authors = resolveAuthors(request.getAuthorIds());
        List<Category> categories = resolveCategories(request.getCategoryIds());

        Book book = Book.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .isbn(request.getIsbn())
                .publishedDate(request.getPublishedDate())
                .stock(request.getStock())
                .coverImageUrl(request.getCoverImageUrl())
                .build();

        authors.forEach(a -> book.getBookAuthors().add(
                BookAuthor.builder().book(book).author(a).build()));
        categories.forEach(c -> book.getBookCategories().add(
                BookCategory.builder().book(book).category(c).build()));

        return BookResponse.from(bookRepository.save(book));
    }

    @Override
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));

        List<Author> authors = resolveAuthors(request.getAuthorIds());
        List<Category> categories = resolveCategories(request.getCategoryIds());

        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());
        book.setIsbn(request.getIsbn());
        book.setPublishedDate(request.getPublishedDate());
        book.setStock(request.getStock());
        book.setCoverImageUrl(request.getCoverImageUrl());

        book.getBookAuthors().clear();
        authors.forEach(a -> book.getBookAuthors().add(
                BookAuthor.builder().book(book).author(a).build()));

        book.getBookCategories().clear();
        categories.forEach(c -> book.getBookCategories().add(
                BookCategory.builder().book(book).category(c).build()));

        return BookResponse.from(bookRepository.save(book));
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    private List<Author> resolveAuthors(List<Long> ids) {
        return ids.stream()
                .map(aid -> authorRepository.findById(aid)
                        .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + aid)))
                .toList();
    }

    private List<Category> resolveCategories(List<Long> ids) {
        return ids.stream()
                .map(cid -> categoryRepository.findById(cid)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + cid)))
                .toList();
    }
}
