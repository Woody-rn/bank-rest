package com.example.bankcards.dto.response;

import com.example.bankcards.entity.BlockRequestStatus;
import java.time.LocalDateTime;

public record BlockRequestResponse(
        Long id,
        Long cardId,
        String cardHolderName,
        Long userId,
        String username,
        BlockRequestStatus status,
        LocalDateTime createdAt
) {}