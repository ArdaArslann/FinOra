package com.finora.common.exception;

import com.finora.common.dto.ApiErrorResponse;
import com.finora.common.dto.ApiResponse;
import com.finora.dashboard.ai.FinancialInsightRateLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException exception
    ){

        ApiErrorResponse error = new ApiErrorResponse(
                exception.getCode(),
                exception.getMessage(),
                400
        );


        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponse.error(error)
                );
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException exception
    ){

        ApiErrorResponse error = new ApiErrorResponse(
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                404
        );


        return ResponseEntity
                .status(404)
                .body(
                        ApiResponse.error(error)
                );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        Map<String,String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );


        ApiErrorResponse apiError = new ApiErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed",
                400
        );


        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponse.validationError(
                                apiError,
                                errors
                        )
                );
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException exception
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                "RECEIPT_FILE_TOO_LARGE",
                "Receipt file size cannot exceed 5 MB.",
                400
        );

        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponse.error(error)
                );
    }

    @ExceptionHandler(FinancialInsightRateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleFinancialInsightRateLimit(
            FinancialInsightRateLimitException exception
    ) {

        ApiErrorResponse error =
                new ApiErrorResponse(
                        "FINANCIAL_INSIGHT_RATE_LIMIT",
                        exception.getMessage(),
                        HttpStatus.TOO_MANY_REQUESTS.value()
                );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(
                        ApiResponse.error(error)
                );
    }
}