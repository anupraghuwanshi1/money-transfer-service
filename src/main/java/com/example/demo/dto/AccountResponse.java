package com.example.demo.dto;

import com.example.demo.entity.Account;

import java.math.BigDecimal;

public record AccountResponse(Long id, Long userId, String currency, BigDecimal balance) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getUser().getId(), account.getCurrency(), account.getBalance());
    }
}
