package com.finora.auth.service;

import com.finora.auth.dto.LoginRequest;
import com.finora.auth.dto.LoginResponse;
import com.finora.auth.dto.RegisterRequest;
import com.finora.auth.dto.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);


}
