package com.example.demo.dto;

import com.example.demo.entity.Transfer;
import com.example.demo.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long id,
        Long sourceAccountId,
        Long targetAccountId,
        BigDecimal amount,
        String currency,
        BigDecimal amountReceived,
        String targetCurrency,
        BigDecimal exchangeRate,
        TransferStatus status,
        LocalDateTime createdAt,
        String failureReasons
) {
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccount().getId(),
                transfer.getTargetAccount().getId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getAmountReceived(),
                transfer.getTargetCurrency(),
                transfer.getExchangeRate(),
                transfer.getStatus(),
                transfer.getCreatedAt(),
                transfer.getFailureReasons()
        );
    }
}
