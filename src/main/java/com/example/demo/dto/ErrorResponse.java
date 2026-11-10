package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
        @JsonProperty("trace-id") String traceId,
        int status,
        String message
) {
}
