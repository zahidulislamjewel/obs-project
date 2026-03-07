package com.obs.backend.service;

import com.obs.backend.dto.AuthorRequest;
import com.obs.backend.dto.AuthorResponse;
import java.util.List;

public interface AuthorService {
    List<AuthorResponse> getAllAuthors();
    AuthorResponse createAuthor(AuthorRequest request);
}
