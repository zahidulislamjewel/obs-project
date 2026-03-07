package com.obs.backend.service;

import com.obs.backend.dto.CategoryResponse;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
}
