package com.finora.auth.entity;

import com.finora.common.entity.BaseEntity;
import com.finora.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;


    public static RefreshTokenEntity create(
            String token,
            UserEntity user,
            LocalDateTime expiresAt
    ){
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.token = token;
        refreshToken.user = user;
        refreshToken.expiresAt = expiresAt;

        return refreshToken;
    }


    public boolean isExpired(){
        return expiresAt.isBefore(LocalDateTime.now());
    }
}