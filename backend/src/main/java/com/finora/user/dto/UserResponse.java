package com.finora.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserResponse(
        String firstName,
        String lastName,
        String email,
        UUID id
) {
}
