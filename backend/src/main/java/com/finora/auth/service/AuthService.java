package com.finora.auth.service;

import com.finora.auth.dto.*;

public interface AuthService {
    RegisterResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
    RefreshTokenResponse refresh(RefreshTokenRequest request);
    void logout(String refreshToken);
}
