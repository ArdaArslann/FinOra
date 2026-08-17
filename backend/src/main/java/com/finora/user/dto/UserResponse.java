package com.finora.user.dto;

import java.util.UUID;

public record UserResponse(
        String firstName,
        String lastName,
        String email,
        UUID id
) {
}
