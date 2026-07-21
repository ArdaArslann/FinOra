package com.finora.category.service;

import com.finora.category.dto.CategoryResponse;
import com.finora.category.dto.CreateCategoryRequest;
import com.finora.category.dto.UpdateCategoryRequest;
import com.finora.category.entity.CategoryEntity;
import com.finora.category.mapper.CategoryMapper;
import com.finora.category.repository.CategoryRepository;
import com.finora.common.exception.BusinessException;
import com.finora.common.exception.ResourceNotFoundException;
import com.finora.common.security.CurrentUserService;
import com.finora.user.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        validateCategoryName(request.name(), currentUser);

        CategoryEntity category =
                categoryMapper.toEntity(request, currentUser);

        CategoryEntity savedCategory =
                categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public List<CategoryResponse> getCategories() {

        UserEntity currentUser = currentUserService.getCurrentUser();

        return categoryRepository
                .findAllByUserOrderByNameAsc(currentUser)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse getCategoryById(UUID id) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        CategoryEntity category =
                getCategoryByIdOrThrow(id, currentUser);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(
            UUID id,
            UpdateCategoryRequest request
    ) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        CategoryEntity category =
                getCategoryByIdOrThrow(id, currentUser);

        if (!category.getName().equalsIgnoreCase(request.name())) {
            validateCategoryName(request.name(), currentUser);
        }

        category.update(
                request.name(),
                request.icon(),
                request.color()
        );

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        CategoryEntity category =
                getCategoryByIdOrThrow(id, currentUser);

        categoryRepository.delete(category);
    }

    private void validateCategoryName(
            String name,
            UserEntity user
    ) {

        if (categoryRepository.existsByNameIgnoreCaseAndUser(name, user)) {

            throw new BusinessException(
                    "CATEGORY_ALREADY_EXISTS",
                    "Category with this name already exists."
            );
        }
    }

    private CategoryEntity getCategoryByIdOrThrow(
            UUID id,
            UserEntity user
    ) {

        return categoryRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "CATEGORY_NOT_FOUND",
                                "Category not found."
                        )
                );
    }
}