package com.finora.category.dto;

import jakarta.validation.constraints.NotBlank;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

public record CreateCategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,

        @NotBlank(message = "Category icon is required")
        String icon,

        @NotBlank(message= "Category color is required")
        String color
){}
