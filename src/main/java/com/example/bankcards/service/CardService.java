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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final AesCardEncryptor encryptor;
    private final HashEncoder hashEncoder;
    private final CardDataMasker masker;
    private final CardNumberGenerator cardNumberGenerator;

    public CardResponse createCard(CardRequest request) {
        User user = getUser(request.userId());
        validateExpiryDate(request.expiryMonth(), request.expiryYear());

        String rawNumber = cardNumberGenerator.generateUniqueNumber();
        Card card = buildCard(request, user, rawNumber);
        card = cardRepository.save(card);

        log.info("Card created: id={}, user={}", card.getId(), user.getUsername());
        return mapToResponse(card);
    }

    @Transactional
    public CardResponse blockCard(Long cardId) {
        Card card = findCardOrThrow(cardId);
        card.setStatus(CardStatus.BLOCKED);
        log.info("Card blocked: id={}", cardId);
        return mapToResponse(card);
    }

    @Transactional
    public CardResponse activateCard(Long cardId) {
        Card card = findCardOrThrow(cardId);
        card.setStatus(CardStatus.ACTIVE);
        log.info("Card activated: id={}", cardId);
        return mapToResponse(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = findCardOrThrow(cardId);
        card.setIsDeleted(true);
        log.info("Card soft-deleted: id={}", cardId);
    }

    public Page<CardResponse> getUserCards(Long userId, Pageable pageable) {
        return cardRepository.findByUserIdAndIsDeletedFalse(userId, pageable)
                .map(this::mapToResponse);
    }

    public Page<CardResponse> getUserCardsByStatus(Long userId, CardStatus status, Pageable pageable) {
        return cardRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, status, pageable)
                .map(this::mapToResponse);
    }

    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardRepository.findAllByIsDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    public CardResponse getCardById(Long cardId) {
        return mapToResponse(findCardOrThrow(cardId));
    }

    public boolean isNotCardOwner(Long cardId, Long userId) {
        return cardRepository.findByIdAndIsDeletedFalse(cardId)
                .map(card -> !card.getUser().getId().equals(userId))
                .orElse(true);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    private Card findCardOrThrow(Long cardId) {
        return cardRepository.findByIdAndIsDeletedFalse(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
    }

    private Card buildCard(CardRequest request, User user, String rawNumber) {
        return Card.builder()
                .cardNumberEncrypted(encryptor.encrypt(rawNumber))
                .cardNumberHash(hashEncoder.hash(rawNumber))
                .user(user)
                .cardHolderName(formatCardHolderName(request.cardHolderName()))
                .expiryMonth(request.expiryMonth())
                .expiryYear(request.expiryYear())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
    }

    private void validateExpiryDate(Integer month, Integer year) {
        if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
            throw new CardValidationException(
                    String.format("Expiry date %02d/%d is in the past", month, year));
        }
    }

    private String formatCardHolderName(String name) {
        return name.toUpperCase();
    }

    private String decryptAndMask(String encrypted) {
        return masker.mask(encryptor.decrypt(encrypted));
    }

    private CardResponse mapToResponse(Card card) {
        return new CardResponse(
                card.getId(),
                decryptAndMask(card.getCardNumberEncrypted()),
                card.getCardHolderName(),
                String.format("%02d/%d", card.getExpiryMonth(), card.getExpiryYear() % 100),
                card.getStatus(),
                card.getBalance(),
                card.getCreatedAt()
        );
    }
}