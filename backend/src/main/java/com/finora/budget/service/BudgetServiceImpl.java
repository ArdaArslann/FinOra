package com.finora.budget.service;

import com.finora.budget.dto.BudgetResponse;
import com.finora.budget.dto.CreateBudgetRequest;
import com.finora.budget.dto.UpdateBudgetRequest;
import com.finora.budget.entity.BudgetEntity;
import com.finora.budget.mapper.BudgetMapper;
import com.finora.budget.repository.BudgetRepository;
import com.finora.category.entity.CategoryEntity;
import com.finora.category.repository.CategoryRepository;
import com.finora.common.exception.BusinessException;
import com.finora.common.exception.ResourceNotFoundException;
import com.finora.common.security.CurrentUserService;
import com.finora.user.entity.UserEntity;
import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;
    private final CurrentUserService currentUserService;
    private final StringRedisTemplate redisTemplate;
    
    private void evictInsightCache(UUID userId) {
        try {
            redisTemplate.delete("finora:ai-insight:cache:" + userId);
        } catch (Exception e) {}
    }
    
    @Override
    public BudgetResponse create(CreateBudgetRequest request) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        CategoryEntity category = getCategoryOrThrow(
                request.categoryId(),
                currentUser
        );

        validateDateRange(
                request.startDate(),
                request.endDate()
        );

        validateBudgetOverlapForCreate(
                category,
                request.startDate(),
                request.endDate()
        );

        BudgetEntity budget = budgetMapper.toEntity(
                request,
                category,
                currentUser
        );

        budget = budgetRepository.save(budget);
        
        evictInsightCache(currentUser.getId());

        return budgetMapper.toResponse(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAll() {

        UserEntity currentUser = currentUserService.getCurrentUser();

        return budgetRepository
                .findAllByUserOrderByStartDateDesc(currentUser)
                .stream()
                .map(budgetMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getById(UUID id) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        BudgetEntity budget =
                getBudgetOrThrow(id, currentUser);

        return budgetMapper.toResponse(budget);
    }
    @Override
    public BudgetResponse update(
            UUID id,
            UpdateBudgetRequest request
    ) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        BudgetEntity budget =
                getBudgetOrThrow(id, currentUser);

        CategoryEntity category =
                getCategoryOrThrow(request.categoryId(), currentUser);

        validateDateRange(
                request.startDate(),
                request.endDate()
        );
        validateBudgetOverlapForUpdate(
                budget.getId(),
                category,
                request.startDate(),
                request.endDate()
        );

        budget.update(
                request.amount(),
                request.period(),
                request.startDate(),
                request.endDate(),
                category
        );

        budget = budgetRepository.save(budget);
        
        evictInsightCache(currentUser.getId());

        return budgetMapper.toResponse(budget);
    }

    @Override
    public void delete(UUID id) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        BudgetEntity budget =
                getBudgetOrThrow(id, currentUser);

        budgetRepository.delete(budget);
        
        evictInsightCache(currentUser.getId());
    }

    private void validateBudgetOverlapForCreate(
            CategoryEntity category,
            LocalDate startDate,
            LocalDate endDate
    ) {

        boolean exists = budgetRepository
                .existsByCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        category,
                        endDate,
                        startDate
                );

        if (exists) {
            throw new BusinessException(
                    "BUDGET_OVERLAP",
                    "A budget already exists for this category in the selected date range."
            );
        }
    }

    private void validateBudgetOverlapForUpdate(
            UUID budgetId,
            CategoryEntity category,
            LocalDate startDate,
            LocalDate endDate
    ) {

        boolean exists = budgetRepository
                .existsByCategoryAndIdNotAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        category,
                        budgetId,
                        endDate,
                        startDate
                );

        if (exists) {
            throw new BusinessException(
                    "BUDGET_OVERLAP",
                    "A budget already exists for this category in the selected date range."
            );
        }
    }
    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    "INVALID_DATE_RANGE",
                    "Start date cannot be after end date."
            );
        }
    }

    private BudgetEntity getBudgetOrThrow(
            UUID id,
            UserEntity user
    ) {

        return budgetRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "BUDGET_NOT_FOUND",
                                "Budget not found"
                        ));
    }

    private CategoryEntity getCategoryOrThrow(
            UUID id,
            UserEntity user
    ) {

        return categoryRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "CATEGORY_NOT_FOUND",
                                "Category not found"
                        ));
    }


}