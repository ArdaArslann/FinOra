package com.finora.common.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiErrorResponse error,
        Map<String, String> validationErrors,
        LocalDateTime timestamp
) {


    public static <T> ApiResponse<T> success(T data) {

        return new ApiResponse<>(
                true,
                data,
                null,
                null,
                LocalDateTime.now()
        );
    }


    public static <T> ApiResponse<T> error(
            ApiErrorResponse error
    ) {

        return new ApiResponse<>(
                false,
                null,
                error,
                null,
                LocalDateTime.now()
        );
    }


    public static <T> ApiResponse<T> validationError(
            ApiErrorResponse error,
            Map<String,String> validationErrors
    ){

        return new ApiResponse<>(
                false,
                null,
                error,
                validationErrors,
                LocalDateTime.now()
        );
    }
}