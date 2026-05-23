package com.example.bankcards.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CardRequest(
        @NotBlank(message = "Card holder name is required") String cardHolderName,

        @NotNull(message = "Expiry month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required")
        @Min(value = 2024, message = "Year must be current or future")
        Integer expiryYear,
        Long userId
) {
}