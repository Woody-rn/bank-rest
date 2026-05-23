package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class CardResponse {
    private Long id;
    private String maskedNumber;
    private String cardHolderName;
    private String expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}