package com.finora.auth.controller;

import com.finora.auth.dto.LoginRequest;
import com.finora.auth.dto.LoginResponse;
import com.finora.auth.dto.RegisterRequest;
import com.finora.auth.dto.RegisterResponse;
import com.finora.auth.service.AuthService;
import com.finora.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return new ApiResponse<>(
                true,
                authService.register(registerRequest)
        );
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return new ApiResponse<>(
                true,
                authService.login(loginRequest)
        );
    }
}
