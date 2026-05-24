package com.example.bankcards;

import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected CardRepository cardRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    protected void resetCard(Long cardId) {
        cardRepository.findById(cardId).ifPresent(card -> {
            card.setStatus(CardStatus.ACTIVE);
            card.setBalance(BigDecimal.valueOf(50000));
            cardRepository.save(card);
        });
    }
}