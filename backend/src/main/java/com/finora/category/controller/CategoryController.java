package com.finora.category.controller;


import com.finora.category.dto.CategoryResponse;
import com.finora.category.dto.CreateCategoryRequest;
import com.finora.category.dto.UpdateCategoryRequest;
import com.finora.category.service.CategoryService;
import com.finora.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        CategoryResponse response = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {

        return ResponseEntity.ok(
                ApiResponse.success(categoryService.getCategories())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(categoryService.getCategoryById(id))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        categoryService.updateCategory(id, request)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id
    ) {

        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}
