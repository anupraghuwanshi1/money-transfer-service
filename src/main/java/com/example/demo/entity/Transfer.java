package com.example.demo.entity;

import com.example.demo.enums.TransferStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account targetAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 3)
    private String targetCurrency;

    @Column(precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(precision = 19, scale = 4)
    private BigDecimal amountReceived;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 1000)
    private String failureReasons;

    protected Transfer() {
    }

    public Transfer(Account sourceAccount, Account targetAccount, BigDecimal amount, String currency, String targetCurrency,
                   BigDecimal exchangeRate, BigDecimal amountReceived, TransferStatus status, LocalDateTime createdAt,
                   String failureReasons) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.currency = currency;
        this.targetCurrency = targetCurrency;
        this.exchangeRate = exchangeRate;
        this.amountReceived = amountReceived;
        this.status = status;
        this.createdAt = createdAt;
        this.failureReasons = failureReasons;
    }

    public Long getId() {
        return id;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public Account getTargetAccount() {
        return targetAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFailureReasons() {
        return failureReasons;
    }
}
