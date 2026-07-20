package com.finora.category.service;


import com.finora.category.dto.CategoryResponse;
import com.finora.category.dto.CreateCategoryRequest;
import com.finora.category.dto.UpdateCategoryRequest;

import java.util.List;
import java.util.UUID;


public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    List<CategoryResponse> getCategories();

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request);

    void deleteCategory(UUID id);
}
