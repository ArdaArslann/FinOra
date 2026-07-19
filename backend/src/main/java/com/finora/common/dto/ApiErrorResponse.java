package com.finora.common.dto;

public record ApiErrorResponse(
        String code,
        String message,
        int status
) {
}