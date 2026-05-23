package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class TransferResponse {
    private Long id;
    private String fromMaskedNumber;
    private String toMaskedNumber;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
}