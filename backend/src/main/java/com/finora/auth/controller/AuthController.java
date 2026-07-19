package com.finora.auth.controller;

import com.finora.auth.dto.*;
import com.finora.auth.service.AuthService;
import com.finora.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ){

        return ApiResponse.success(
                authService.register(request)
        );
    }


    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ){

        return ApiResponse.success(
                authService.login(request)
        );
    }


    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ){

        return ApiResponse.success(
                authService.refresh(request)
        );
    }


    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ){

        authService.logout(
                request.refreshToken()
        );

        return ApiResponse.success(
                "Logout successful"
        );
    }
}