package com.obs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.backend.dto.AuthorResponse;
import com.obs.backend.service.AuthorService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorService authorService;

    private AuthorResponse authorResponse;

    @BeforeEach
    void setUp() {
        authorResponse = AuthorResponse.builder()
                .id(1L)
                .name("George Orwell")
                .bio("English novelist")
                .build();
    }

    @Test
    void getAllAuthors_ShouldReturnList() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(List.of(authorResponse));

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("George Orwell"));
    }

    @Test
    void getAuthorById_WhenFound_ShouldReturnAuthor() throws Exception {
        when(authorService.getAuthorById(1L)).thenReturn(authorResponse);

        mockMvc.perform(get("/api/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("George Orwell"));
    }

    @Test
    void getAuthorById_WhenNotFound_ShouldReturn404() throws Exception {
        when(authorService.getAuthorById(99L))
                .thenThrow(new EntityNotFoundException("Author not found with id: 99"));

        mockMvc.perform(get("/api/authors/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthor_WhenFound_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/authors/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAuthor_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new EntityNotFoundException("Author not found with id: 99"))
                .when(authorService).deleteAuthor(99L);

        mockMvc.perform(delete("/api/authors/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthor_WhenLinkedToBooks_ShouldReturn409() throws Exception {
        doThrow(new DataIntegrityViolationException("Cannot delete author: still linked to existing books."))
                .when(authorService).deleteAuthor(1L);

        mockMvc.perform(delete("/api/authors/1"))
                .andExpect(status().isConflict());
    }
}
