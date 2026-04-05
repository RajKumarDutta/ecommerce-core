package com.rdutta.ecommerceapp.common.util.exception;

import com.rdutta.ecommerceapp.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleRuntime(RuntimeException ex) {
        return ApiResponse.failure(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleGeneric(Exception ex) {
        return ApiResponse.failure("Something went wrong");
    }
}