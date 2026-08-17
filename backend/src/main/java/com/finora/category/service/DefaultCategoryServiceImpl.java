package com.finora.category.service;

import com.finora.category.entity.CategoryEntity;
import com.finora.category.repository.CategoryRepository;
import com.finora.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultCategoryServiceImpl implements DefaultCategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public void createDefaultCategories(UserEntity user) {

        List<CategoryEntity> categories = List.of(
                CategoryEntity.create(
                        "Food",
                        "restaurant",
                        "#FF6B6B",
                        true,
                        user
                ),
                CategoryEntity.create(
                        "Transport",
                        "directions_car",
                        "#4D96FF",
                        true,
                        user
                ),
                CategoryEntity.create(
                        "Shopping",
                        "shopping_bag",
                        "#9B59B6",
                        true,
                        user
                ),
                CategoryEntity.create(
                        "Bills",
                        "receipt_long",
                        "#F39C12",
                        true,
                        user
                ),
                CategoryEntity.create(
                        "Entertainment",
                        "movie",
                        "#2ECC71",
                        true,
                        user
                ),
                CategoryEntity.create(
                        "Health",
                        "health_and_safety",
                        "#E74C3C",
                        true,
                        user
                ),
                CategoryEntity.create(
                        "Other",
                        "category",
                        "#95A5A6",
                        true,
                        user
                )
        );

        categoryRepository.saveAll(categories);
    }
}