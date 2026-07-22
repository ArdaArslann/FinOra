package com.finora.budget.controller;

import com.finora.budget.dto.BudgetResponse;
import com.finora.budget.dto.CreateBudgetRequest;
import com.finora.budget.dto.UpdateBudgetRequest;
import com.finora.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public BudgetResponse create(
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        return budgetService.create(request);
    }

    @GetMapping
    public List<BudgetResponse> getAll() {
        return budgetService.getAll();
    }

    @GetMapping("/{id}")
    public BudgetResponse getById(
            @PathVariable UUID id
    ) {
        return budgetService.getById(id);
    }

    @PutMapping("/{id}")
    public BudgetResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request
    ) {
        return budgetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        budgetService.delete(id);
    }
}