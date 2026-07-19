package com.finora.common.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiErrorResponse error,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                data,
                null,
                LocalDateTime.now()
        );
    }


    public static <T> ApiResponse<T> error(ApiErrorResponse error) {
        return new ApiResponse<>(
                false,
                null,
                error,
                LocalDateTime.now()
        );
    }
}