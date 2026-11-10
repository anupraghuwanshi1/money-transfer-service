package com.example.demo.controller;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.CreateAccountRequest;
import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.TransferResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.MoneyTransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MoneyTransferController {

    private final MoneyTransferService moneyTransferService;

    public MoneyTransferController(MoneyTransferService moneyTransferService) {
        this.moneyTransferService = moneyTransferService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moneyTransferService.createUser(request));
    }

    @PostMapping("/users/{userId}/accounts")
    public ResponseEntity<AccountResponse> createAccount(@PathVariable Long userId,
                                                      @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moneyTransferService.createAccount(userId, request));
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(moneyTransferService.getAccount(accountId));
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(moneyTransferService.transfer(request));
    }

    @GetMapping("/accounts/{accountId}/transfers")
    public ResponseEntity<List<TransferResponse>> getTransferHistory(@PathVariable Long accountId) {
        return ResponseEntity.ok(moneyTransferService.getHistory(accountId));
    }
}
