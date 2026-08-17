package com.finora.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
                @NotBlank(message = "Category name is required") String name,

                @NotBlank(message = "Category icon is required") String icon,

                @NotBlank(message = "Category color is required") String color) {
}
