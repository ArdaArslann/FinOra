package com.finora.auth.service;

import com.finora.auth.entity.RefreshTokenEntity;
import com.finora.auth.repository.RefreshTokenRepository;
import com.finora.common.exception.BusinessException;
import com.finora.common.exception.ResourceNotFoundException;
import com.finora.user.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;


    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }


    @Override
    public RefreshTokenEntity createRefreshToken(UserEntity user) {

        String token = UUID.randomUUID().toString();

        RefreshTokenEntity refreshToken = RefreshTokenEntity.create(
                token,
                user,
                LocalDateTime.now().plusDays(7)
        );

        return refreshTokenRepository.save(refreshToken);
    }


    @Override
    public RefreshTokenEntity verifyRefreshToken(String token) {

        RefreshTokenEntity refreshToken =
                refreshTokenRepository.findByTokenWithUser(token)
                        .orElseThrow(() ->
                                new BusinessException("Invalid refresh token")
                        );


        if(refreshToken.isExpired()){
            refreshTokenRepository.delete(refreshToken);

            throw new BusinessException("Refresh token expired");
        }


        return refreshToken;
    }


    @Override
    public void deleteByToken(String token) {

        RefreshTokenEntity refreshToken =
                refreshTokenRepository.findByTokenWithUser(token)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Refresh token not found"
                                ));

        refreshTokenRepository.delete(refreshToken);
    }
}