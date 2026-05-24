package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardValidationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.AesCardEncryptor;
import com.example.bankcards.util.CardDataMasker;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.HashEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private UserRepository userRepository;
    @Mock private AesCardEncryptor encryptor;
    @Mock private HashEncoder hashEncoder;
    @Mock private CardDataMasker masker;
    @Mock private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;
    private CardRequest cardRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@bank.ru")
                .build();

        card = Card.builder()
                .id(1L)
                .cardNumberEncrypted("encrypted_1234")
                .cardNumberHash("hash_1234")
                .user(user)
                .cardHolderName("TEST USER")
                .expiryMonth(12)
                .expiryYear(2027)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .isDeleted(false)
                .build();

        cardRequest = new CardRequest("Test User", 12, 2027, 1L);
    }

    @Test
    void createCard_shouldReturnCardResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardNumberGenerator.generateUniqueNumber()).thenReturn("4532015112830366");
        when(encryptor.encrypt("4532015112830366")).thenReturn("encrypted");
        when(hashEncoder.hash("4532015112830366")).thenReturn("hash");
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(encryptor.decrypt("encrypted_1234")).thenReturn("4532015112830366");
        when(masker.mask("4532015112830366")).thenReturn("**** **** **** 0366");

        CardResponse response = cardService.createCard(cardRequest);

        assertNotNull(response);
        assertEquals("TEST USER", response.getCardHolderName());
        assertEquals("**** **** **** 0366", response.getMaskedNumber());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        CardRequest request = new CardRequest("Test", 12, 2027, 99L);

        assertThrows(UserNotFoundException.class, () -> cardService.createCard(request));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCard_shouldThrowCardValidationException_whenExpiredDate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CardRequest expiredRequest = new CardRequest("Test", 1, 2025, 1L);

        assertThrows(CardValidationException.class, () -> cardService.createCard(expiredRequest));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCardById_shouldReturnCardResponse() {
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));
        when(encryptor.decrypt("encrypted_1234")).thenReturn("4532015112830366");
        when(masker.mask("4532015112830366")).thenReturn("**** **** **** 0366");

        CardResponse response = cardService.getCardById(1L);

        assertEquals(1L, response.getId());
        assertEquals("**** **** **** 0366", response.getMaskedNumber());
    }

    @Test
    void getCardById_shouldThrowCardNotFoundException() {
        when(cardRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(99L));
    }

    @Test
    void getUserCards_shouldReturnPagedResponse() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(cardRepository.findByUserIdAndIsDeletedFalse(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(card), pageable, 1));
        when(encryptor.decrypt(any())).thenReturn("4532015112830366");
        when(masker.mask(any())).thenReturn("**** **** **** 0366");

        Page<CardResponse> result = cardService.getUserCards(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("**** **** **** 0366", result.getContent().getFirst().getMaskedNumber());
    }

    @Test
    void blockCard_shouldChangeStatusToBlocked() {
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));
        when(encryptor.decrypt(any())).thenReturn("4532015112830366");
        when(masker.mask(any())).thenReturn("**** **** **** 0366");

        CardResponse response = cardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        assertEquals(CardStatus.BLOCKED, response.getStatus());
    }

    @Test
    void activateCard_shouldChangeStatusToActive() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));
        when(encryptor.decrypt(any())).thenReturn("4532015112830366");
        when(masker.mask(any())).thenReturn("**** **** **** 0366");

        CardResponse response = cardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
    }

    @Test
    void deleteCard_shouldSetIsDeletedTrue() {
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        assertTrue(card.getIsDeleted());
    }
}