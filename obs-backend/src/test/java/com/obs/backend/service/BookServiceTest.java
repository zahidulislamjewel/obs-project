package com.obs.backend.service;

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
import com.obs.backend.service.impl.BookServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Author author;
    private Category category;
    private Book book;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        author = Author.builder().id(1L).name("George Orwell").bio("English novelist").build();
        category = Category.builder().id(1L).name("Fiction").description("Fiction books").build();

        book = Book.builder()
                .id(1L)
                .title("1984")
                .description("Dystopian novel")
                .price(new BigDecimal("12.99"))
                .isbn("978-0451524935")
                .publishedDate(LocalDate.of(1949, 6, 8))
                .stock(100)
                .build();

        BookAuthor ba = new BookAuthor();
        ba.setBook(book);
        ba.setAuthor(author);
        book.getBookAuthors().add(ba);

        BookCategory bc = new BookCategory();
        bc.setBook(book);
        bc.setCategory(category);
        book.getBookCategories().add(bc);

        bookRequest = BookRequest.builder()
                .title("1984")
                .description("Dystopian novel")
                .price(new BigDecimal("12.99"))
                .isbn("978-0451524935")
                .publishedDate(LocalDate.of(1949, 6, 8))
                .authorIds(List.of(1L))
                .categoryIds(List.of(1L))
                .stock(100)
                .build();
    }

    @Test
    void getAllBooks_ShouldReturnPagedBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        Page<BookResponse> result = bookService.getAllBooks(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("1984");
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void getBookById_WhenFound_ShouldReturnBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponse result = bookService.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("1984");
        assertThat(result.getAuthors()).hasSize(1);
        assertThat(result.getAuthors().get(0).getName()).isEqualTo("George Orwell");
    }

    @Test
    void getBookById_WhenNotFound_ShouldThrowException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createBook_ShouldSaveAndReturnBook() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse result = bookService.createBook(bookRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("1984");
        assertThat(result.getPrice()).isEqualByComparingTo("12.99");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_WhenAuthorNotFound_ShouldThrowException() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBook(bookRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author not found");
    }

    @Test
    void createBook_WhenCategoryNotFound_ShouldThrowException() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBook(bookRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void updateBook_WhenFound_ShouldUpdateAndReturn() {
        BookRequest updateRequest = BookRequest.builder()
                .title("Nineteen Eighty-Four")
                .description("Updated description")
                .price(new BigDecimal("15.99"))
                .isbn("978-0451524935")
                .publishedDate(LocalDate.of(1949, 6, 8))
                .authorIds(List.of(1L))
                .categoryIds(List.of(1L))
                .stock(80)
                .build();

        Book updatedBook = Book.builder()
                .id(1L)
                .title("Nineteen Eighty-Four")
                .description("Updated description")
                .price(new BigDecimal("15.99"))
                .isbn("978-0451524935")
                .publishedDate(LocalDate.of(1949, 6, 8))
                .stock(80)
                .build();

        BookAuthor uba = new BookAuthor();
        uba.setBook(updatedBook);
        uba.setAuthor(author);
        updatedBook.getBookAuthors().add(uba);

        BookCategory ubc = new BookCategory();
        ubc.setBook(updatedBook);
        ubc.setCategory(category);
        updatedBook.getBookCategories().add(ubc);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        BookResponse result = bookService.updateBook(1L, updateRequest);

        assertThat(result.getTitle()).isEqualTo("Nineteen Eighty-Four");
        assertThat(result.getStock()).isEqualTo(80);
        assertThat(result.getAuthors()).hasSize(1);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_WhenNotFound_ShouldThrowException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(99L, bookRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteBook_WhenFound_ShouldDelete() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteBook_WhenNotFound_ShouldThrowException() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(bookRepository, never()).deleteById(any());
    }
}
