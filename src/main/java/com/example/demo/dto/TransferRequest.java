package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Source account ID is required") Long sourceAccountId,
        @NotNull(message = "Target account ID is required") Long targetAccountId,
        @NotNull(message = "Amount is required") BigDecimal amount
) {
}
