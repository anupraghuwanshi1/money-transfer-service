package com.example.demo;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.TransferRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.exception.TransferRejectedException;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransferRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ExchangeRateService;
import com.example.demo.service.MoneyTransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoneyTransferServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private MoneyTransferService moneyTransferService;

    @BeforeEach
    void setUp() {
        when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldRejectSingleTransferLimit() {
        User fromUser = new User("Anna");
        User toUser = new User("Ben");
        Account source = new Account(fromUser, "EUR", new BigDecimal("5000"));
        Account target = new Account(toUser, "EUR", new BigDecimal("200"));

        source.setBalance(new BigDecimal("5000"));
        target.setBalance(new BigDecimal("200"));
        source.getClass();

        try {
            java.lang.reflect.Field idField = Account.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(source, 1L);
            idField.set(target, 2L);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(target));

        TransferRejectedException ex = assertThrows(TransferRejectedException.class,
                () -> moneyTransferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("1200"))));

        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("Single transfer limit"));
    }
}
