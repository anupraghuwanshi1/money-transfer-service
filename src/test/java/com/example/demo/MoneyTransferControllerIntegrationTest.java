package com.example.demo;

import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransferRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ExchangeRateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MoneyTransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        transferRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        when(exchangeRateService.getRate(anyString(), anyString())).thenReturn(new BigDecimal("1.10"));
    }

    @Test
    void shouldTransferMoneyAcrossCurrenciesAndPersistHistory() throws Exception {
        User anna = userRepository.save(new User("Anna"));
        User ben = userRepository.save(new User("Ben"));
        Account eurAccount = accountRepository.save(new Account(anna, "EUR", new BigDecimal("1000.00")));
        Account usdAccount = accountRepository.save(new Account(ben, "USD", new BigDecimal("0.00")));

        MvcResult result = mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":" + eurAccount.getId() + ",\"targetAccountId\":" + usdAccount.getId() + ",\"amount\":100}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("EUR", response.get("currency").asText());
        assertEquals("USD", response.get("targetCurrency").asText());
        assertEquals("110.0", response.get("amountReceived").asText());

        Account updatedSource = accountRepository.findById(eurAccount.getId()).orElseThrow();
        Account updatedTarget = accountRepository.findById(usdAccount.getId()).orElseThrow();
        assertEquals(new BigDecimal("900.00"), updatedSource.getBalance());
        assertEquals(new BigDecimal("110.0000"), updatedTarget.getBalance());

        MvcResult history = mockMvc.perform(get("/api/accounts/" + eurAccount.getId() + "/transfers"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode historyNode = objectMapper.readTree(history.getResponse().getContentAsString());
        assertEquals(1, historyNode.size());
        assertEquals("SUCCESS", historyNode.get(0).get("status").asText());
        assertEquals(1.10, historyNode.get(0).get("exchangeRate").asDouble(), 0.000001);
    }

    @Test
    void shouldRejectViolatingTransfersWithRuleSummary() throws Exception {
        User anna = userRepository.save(new User("Anna"));
        User ben = userRepository.save(new User("Ben"));
        Account eurAccount = accountRepository.save(new Account(anna, "EUR", new BigDecimal("5000.00")));
        Account usdAccount = accountRepository.save(new Account(ben, "USD", new BigDecimal("0.00")));

        MvcResult result = mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":" + eurAccount.getId() + ",\"targetAccountId\":" + usdAccount.getId() + ",\"amount\":1200}"))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(response.has("trace-id"));
        assertEquals(422, response.get("status").asInt());
        assertTrue(response.get("message").asText().contains("Single transfer limit"));
        assertTrue(response.get("message").asText().contains("Cross-currency limit"));

        JsonNode history = objectMapper.readTree(mockMvc.perform(get("/api/accounts/" + eurAccount.getId() + "/transfers"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        assertEquals(1, history.size());
        assertEquals("REJECTED", history.get(0).get("status").asText());
        assertTrue(history.get(0).get("failureReasons").asText().contains("Single transfer limit"));
    }

    @Test
    void shouldCreateUserAndAccountThroughApi() throws Exception {
        MvcResult userResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Charlie\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode user = objectMapper.readTree(userResult.getResponse().getContentAsString());
        assertTrue(user.get("id").asLong() > 0);
        assertEquals("Charlie", user.get("name").asText());

        MvcResult accountResult = mockMvc.perform(post("/api/users/" + user.get("id").asLong() + "/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currency\":\"usd\",\"openingBalance\":250.50}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode account = objectMapper.readTree(accountResult.getResponse().getContentAsString());
        assertTrue(account.get("id").asLong() > 0);
        assertEquals(user.get("id").asLong(), account.get("userId").asLong());
        assertEquals("USD", account.get("currency").asText());
        assertEquals(250.50, account.get("balance").asDouble(), 0.000001);
    }
}
