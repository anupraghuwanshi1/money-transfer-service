package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank(message = "Name is required") String name) {
}
