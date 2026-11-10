package com.example.demo.controller;

import com.example.demo.dto.ErrorResponse;
import com.example.demo.exception.ExchangeRateUnavailableException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.TransferRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(TransferRejectedException.class)
    public ResponseEntity<ErrorResponse> handleTransferRejected(TransferRejectedException ex) {
        return ResponseEntity.unprocessableEntity().body(new ErrorResponse(UUID.randomUUID().toString(), 422, ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(UUID.randomUUID().toString(), 404, ex.getMessage()));
    }

    @ExceptionHandler(ExchangeRateUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleExchangeRateUnavailable(ExchangeRateUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(UUID.randomUUID().toString(), 502, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(UUID.randomUUID().toString(), 400, message));
    }
}
