package com.finora.auth.controller;

import com.finora.auth.dto.*;
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

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ){
        return new ApiResponse<>(
                true,
                authService.refresh(request)
        );
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ){

        authService.logout(request.refreshToken());

        return new ApiResponse<>(
                true,
                "Logout successful"
        );
    }
}
