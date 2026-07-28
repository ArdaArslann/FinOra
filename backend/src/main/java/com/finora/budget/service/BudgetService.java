package com.finora.budget.service;

import com.finora.budget.dto.BudgetResponse;
import com.finora.budget.dto.CreateBudgetRequest;
import com.finora.budget.dto.UpdateBudgetRequest;
import java.util.List;
import java.util.UUID;

public interface BudgetService {

    BudgetResponse create(CreateBudgetRequest request);

    List<BudgetResponse> getAll();

    BudgetResponse getById(UUID id);

    BudgetResponse update(UUID id, UpdateBudgetRequest request);

    void delete(UUID id);
}