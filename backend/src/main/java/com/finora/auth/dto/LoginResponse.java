package com.finora.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
){}