package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank(message = "Currency is required") String currency,
        @NotNull(message = "Opening balance is required") BigDecimal openingBalance
) {
}
