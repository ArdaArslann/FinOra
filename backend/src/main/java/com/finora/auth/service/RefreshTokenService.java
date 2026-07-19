package com.finora.auth.service;

import com.finora.auth.entity.RefreshTokenEntity;
import com.finora.user.entity.UserEntity;

public interface RefreshTokenService {

    RefreshTokenEntity createRefreshToken(UserEntity user);

    RefreshTokenEntity verifyRefreshToken(String token);

    void deleteByToken(String token);
}