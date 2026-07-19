package com.finora.auth.repository;

import com.finora.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    @Query("""
       SELECT r FROM RefreshTokenEntity r
       JOIN FETCH r.user
       WHERE r.token = :token
       """)
    Optional<RefreshTokenEntity> findByTokenWithUser(
            @Param("token") String token
    );
    void deleteByUserId(UUID userId);
}