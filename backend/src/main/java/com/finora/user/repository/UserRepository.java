package com.finora.user.repository;

import com.finora.user.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmail(@NotBlank @Email String email);

    Optional<UserEntity> findByEmail(@NotBlank @Email String email);
}
