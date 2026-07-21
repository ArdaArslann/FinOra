package com.finora.transaction.repository;

import com.finora.category.entity.CategoryEntity;
import com.finora.transaction.entity.TransactionEntity;
import com.finora.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByUserOrderByTransactionDateDesc(UserEntity user);
    boolean existsByCategory(CategoryEntity category);
    Optional<TransactionEntity> findByIdAndUser(UUID id, UserEntity user);
}