package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardResponse createCard(CardRequest request);

    CardResponse blockCard(Long cardId);

    CardResponse activateCard(Long cardId);

    void deleteCard(Long cardId);

    Page<CardResponse> getUserCards(Long userId, Pageable pageable);

    Page<CardResponse> getUserCardsByStatus(Long userId, CardStatus status, Pageable pageable);

    Page<CardResponse> getAllCards(Pageable pageable);

    CardResponse getCardById(Long cardId);

    boolean isNotCardOwner(Long cardId, Long userId);
}