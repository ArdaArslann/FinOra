package com.finora.budget.service;

import com.finora.budget.dto.BudgetUsageResponse;
import com.finora.budget.dto.CreateBudgetRequest;
import com.finora.budget.dto.UpdateBudgetRequest;
import java.util.List;
import java.util.UUID;

public interface BudgetService {

    BudgetUsageResponse create(CreateBudgetRequest request);

    List<BudgetUsageResponse> getAll();

    BudgetUsageResponse getById(UUID id);

    BudgetUsageResponse update(UUID id, UpdateBudgetRequest request);

    void delete(UUID id);
}