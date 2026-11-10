package com.example.demo.service;

import com.example.demo.exception.ExchangeRateUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class ExchangeRateService {

    private final RestTemplate restTemplate;

    @Value("${exchange-rate.base-url:https://api.frankfurter.app}")
    private String baseUrl;

    public ExchangeRateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }

        String url = baseUrl + "/latest?from=" + fromCurrency.toUpperCase() + "&to=" + toCurrency.toUpperCase();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || response.get("rates") == null) {
                throw new ExchangeRateUnavailableException("Exchange rate unavailable for " + fromCurrency + " to " + toCurrency);
            }

            Map<String, Number> rates = (Map<String, Number>) response.get("rates");
            Number value = rates.get(toCurrency.toUpperCase());
            if (value == null) {
                throw new ExchangeRateUnavailableException("Exchange rate unavailable for " + fromCurrency + " to " + toCurrency);
            }

            return new BigDecimal(value.toString());
        } catch (RestClientException | ClassCastException ex) {
            throw new ExchangeRateUnavailableException("Exchange rate unavailable for " + fromCurrency + " to " + toCurrency);
        }
    }
}
