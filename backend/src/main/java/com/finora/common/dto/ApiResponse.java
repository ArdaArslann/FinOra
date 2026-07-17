package com.finora.common.dto;

public record ApiResponse<T>(
    boolean success,
    T data
){}
