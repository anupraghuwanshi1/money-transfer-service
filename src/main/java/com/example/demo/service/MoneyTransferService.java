package com.example.demo.service;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.TransferResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.Transfer;
import com.example.demo.entity.User;
import com.example.demo.enums.TransferStatus;
import com.example.demo.exception.ExchangeRateUnavailableException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.TransferRejectedException;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransferRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MoneyTransferService {

    private static final BigDecimal SINGLE_TRANSFER_LIMIT = new BigDecimal("1000");
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("2500");
    private static final BigDecimal CROSS_CURRENCY_LIMIT = new BigDecimal("500");

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final ExchangeRateService exchangeRateService;

    public MoneyTransferService(UserRepository userRepository,
                               AccountRepository accountRepository,
                               TransferRepository transferRepository,
                               ExchangeRateService exchangeRateService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.exchangeRateService = exchangeRateService;
    }

    public UserResponse createUser(CreateUserRequest request) {
        User user = new User(request.name());
        return UserResponse.from(userRepository.save(user));
    }

    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String currency = request.currency().trim().toUpperCase();
        BigDecimal openingBalance = request.openingBalance() == null ? BigDecimal.ZERO : request.openingBalance();

        Account account = new Account(user, currency, openingBalance);
        return AccountResponse.from(accountRepository.save(account));
    }

    public AccountResponse getAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        return AccountResponse.from(account);
    }

    public List<TransferResponse> getHistory(Long accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        return transferRepository.findHistoryForAccount(accountId).stream()
                .map(TransferResponse::from)
                .toList();
    }

    @Transactional(noRollbackFor = TransferRejectedException.class)
    public TransferResponse transfer(TransferRequest request) {
        Account source = accountRepository.findById(request.sourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + request.sourceAccountId()));
        Account target = accountRepository.findById(request.targetAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Target account not found: " + request.targetAccountId()));

        if (source.getId() != null && source.getId().equals(target.getId())) {
            throw new TransferRejectedException("Source and target accounts must be different");
        }

        BigDecimal amount = request.amount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferRejectedException("Amount must be greater than zero");
        }

        if (source.getBalance().compareTo(amount) < 0) {
            throw new TransferRejectedException("Insufficient funds");
        }

        List<String> violations = new ArrayList<>();
        if (amount.compareTo(SINGLE_TRANSFER_LIMIT) > 0) {
            violations.add("Single transfer limit");
        }

        BigDecimal dailyTotal = getSuccessfulTransfersToday(source.getId());
        if (dailyTotal.add(amount).compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            violations.add("Daily limit");
        }

        boolean crossCurrency = !source.getCurrency().equalsIgnoreCase(target.getCurrency());
        if (crossCurrency && amount.compareTo(CROSS_CURRENCY_LIMIT) > 0) {
            violations.add("Cross-currency limit");
        }

        if (!violations.isEmpty()) {
            String failureReasons = String.join(", ", violations);
            Transfer rejected = new Transfer(
                    source,
                    target,
                    amount,
                    source.getCurrency(),
                    target.getCurrency(),
                    null,
                    null,
                    TransferStatus.REJECTED,
                    LocalDateTime.now(),
                    failureReasons
            );
            transferRepository.save(rejected);
            throw new TransferRejectedException("Rules violated: " + failureReasons);
        }

        BigDecimal exchangeRate = BigDecimal.ONE;
        BigDecimal amountReceived = amount;

        try {
            if (crossCurrency) {
                exchangeRate = exchangeRateService.getRate(source.getCurrency(), target.getCurrency());
                amountReceived = amount.multiply(exchangeRate).setScale(4, RoundingMode.HALF_UP);
            }
        } catch (ExchangeRateUnavailableException ex) {
            Transfer failed = new Transfer(
                    source,
                    target,
                    amount,
                    source.getCurrency(),
                    target.getCurrency(),
                    null,
                    null,
                    TransferStatus.REJECTED,
                    LocalDateTime.now(),
                    "Exchange rate unavailable"
            );
            transferRepository.save(failed);
            throw new TransferRejectedException("Exchange rate unavailable for " + source.getCurrency() + " to " + target.getCurrency());
        }

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amountReceived));
        accountRepository.save(source);
        accountRepository.save(target);

        Transfer successfulTransfer = new Transfer(
                source,
                target,
                amount,
                source.getCurrency(),
                target.getCurrency(),
                exchangeRate,
                amountReceived,
                TransferStatus.SUCCESS,
                LocalDateTime.now(),
                null
        );
        Transfer saved = transferRepository.save(successfulTransfer);
        return TransferResponse.from(saved);
    }

    private BigDecimal getSuccessfulTransfersToday(Long accountId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime nextDay = today.plusDays(1).atStartOfDay();
        BigDecimal total = transferRepository.sumSuccessfulTransfersForDay(accountId, start, nextDay);
        return total == null ? BigDecimal.ZERO : total;
    }
}
