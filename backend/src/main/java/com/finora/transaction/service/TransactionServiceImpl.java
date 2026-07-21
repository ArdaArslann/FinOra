package com.finora.transaction.service;

import com.finora.category.entity.CategoryEntity;
import com.finora.category.repository.CategoryRepository;
import com.finora.common.exception.ResourceNotFoundException;
import com.finora.common.security.CurrentUserService;
import com.finora.transaction.dto.CreateTransactionRequest;
import com.finora.transaction.dto.TransactionResponse;
import com.finora.transaction.dto.UpdateTransactionRequest;
import com.finora.transaction.entity.TransactionEntity;
import com.finora.transaction.mapper.TransactionMapper;
import com.finora.transaction.repository.TransactionRepository;
import com.finora.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final CurrentUserService currentUserService;

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        CategoryEntity category = getCategoryOrThrow(
                request.categoryId(),
                currentUser
        );

        TransactionEntity transaction = transactionMapper.toEntity(
                request,
                category,
                currentUser
        );

        transaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions() {

        UserEntity currentUser = currentUserService.getCurrentUser();

        return transactionRepository
                .findAllByUserOrderByTransactionDateDesc(currentUser)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        TransactionEntity transaction =
                getTransactionOrThrow(id, currentUser);

        return transactionMapper.toResponse(transaction);
    }

    @Override
    public TransactionResponse updateTransaction(
            UUID id,
            UpdateTransactionRequest request
    ) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        TransactionEntity transaction =
                getTransactionOrThrow(id, currentUser);

        CategoryEntity category =
                getCategoryOrThrow(request.categoryId(), currentUser);

        transaction.update(
                request.amount(),
                request.type(),
                request.description(),
                request.transactionDate(),
                category
        );

        transaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(transaction);
    }

    @Override
    public void deleteTransaction(UUID id) {

        UserEntity currentUser = currentUserService.getCurrentUser();

        TransactionEntity transaction =
                getTransactionOrThrow(id, currentUser);

        transactionRepository.delete(transaction);
    }

    private TransactionEntity getTransactionOrThrow(
            UUID id,
            UserEntity user
    ) {

        return transactionRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("TRANSACTION_NOT_FOUND","Transaction not found"));
    }

    private CategoryEntity getCategoryOrThrow(
            UUID id,
            UserEntity user
    ) {

        return categoryRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("CATEGORY_NOT_FOUND","Category not found"));
    }

}
