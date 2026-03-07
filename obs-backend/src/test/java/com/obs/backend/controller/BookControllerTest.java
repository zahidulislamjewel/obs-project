package com.obs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.backend.dto.BookRequest;
import com.obs.backend.dto.BookResponse;
import com.obs.backend.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private BookResponse bookResponse;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        bookResponse = BookResponse.builder()
                .id(1L)
                .title("1984")
                .description("Dystopian novel")
                .price(new BigDecimal("12.99"))
                .isbn("978-0451524935")
                .publishedDate(LocalDate.of(1949, 6, 8))
                .authorId(1L)
                .authorName("George Orwell")
                .categoryId(1L)
                .categoryName("Fiction")
                .stock(100)
                .build();

        bookRequest = BookRequest.builder()
                .title("1984")
                .description("Dystopian novel")
                .price(new BigDecimal("12.99"))
                .isbn("978-0451524935")
                .publishedDate(LocalDate.of(1949, 6, 8))
                .authorId(1L)
                .categoryId(1L)
                .stock(100)
                .build();
    }

    @Test
    void getAllBooks_ShouldReturnPagedResponse() throws Exception {
        Page<BookResponse> page = new PageImpl<>(List.of(bookResponse), PageRequest.of(0, 10), 1);
        when(bookService.getAllBooks(any())).thenReturn(page);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("1984"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getBookById_WhenFound_ShouldReturnBook() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(bookResponse);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("1984"))
                .andExpect(jsonPath("$.authorName").value("George Orwell"));
    }

    @Test
    void getBookById_WhenNotFound_ShouldReturn404() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new EntityNotFoundException("Book not found with id: 99"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBook_WithValidRequest_ShouldReturn201() throws Exception {
        when(bookService.createBook(any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("1984"));
    }

    @Test
    void createBook_WithInvalidRequest_ShouldReturn400() throws Exception {
        BookRequest invalidRequest = BookRequest.builder()
                .title("") // blank title
                .price(new BigDecimal("-1.00")) // negative price
                .authorId(1L)
                .categoryId(1L)
                .stock(10)
                .build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBook_WhenFound_ShouldReturnUpdatedBook() throws Exception {
        BookResponse updated = BookResponse.builder()
                .id(1L)
                .title("Nineteen Eighty-Four")
                .description("Updated")
                .price(new BigDecimal("15.99"))
                .isbn("978-0451524935")
                .authorId(1L)
                .authorName("George Orwell")
                .categoryId(1L)
                .categoryName("Fiction")
                .stock(80)
                .build();

        when(bookService.updateBook(eq(1L), any(BookRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Nineteen Eighty-Four"));
    }

    @Test
    void deleteBook_WhenFound_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBook_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new EntityNotFoundException("Book not found with id: 99"))
                .when(bookService).deleteBook(99L);

        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound());
    }
}
