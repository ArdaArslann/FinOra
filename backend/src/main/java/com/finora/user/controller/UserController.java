package com.finora.user.controller;

import com.finora.common.dto.ApiResponse;
import com.finora.user.dto.UserResponse;
import com.finora.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(){
        return new ApiResponse<>(
                true,
                userService.getCurrentUser()
        );
    }
}
