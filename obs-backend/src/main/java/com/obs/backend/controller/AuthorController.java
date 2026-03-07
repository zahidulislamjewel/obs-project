package com.obs.backend.controller;

import com.obs.backend.dto.AuthorResponse;
import com.obs.backend.repository.AuthorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {
        List<AuthorResponse> authors = authorRepository.findAll()
                .stream()
                .map(AuthorResponse::from)
                .toList();
        return ResponseEntity.ok(authors);
    }
}
