package com.finora.budget.controller;

import com.finora.budget.dto.BudgetResponse;
import com.finora.budget.dto.CreateBudgetRequest;
import com.finora.budget.dto.UpdateBudgetRequest;
import com.finora.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.finora.common.dto.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> create(
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        BudgetResponse response = budgetService.create(request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}