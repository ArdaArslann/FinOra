package com.finora.category.repository;

import com.finora.category.entity.CategoryEntity;
import com.finora.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    boolean existsByNameIgnoreCaseAndUser(
            String name,
            UserEntity user
    );

    List<CategoryEntity> findAllByUserOrderByNameAsc(
            UserEntity user
    );

    Optional<CategoryEntity> findByIdAndUser(
            UUID id,
            UserEntity user
    );
}
