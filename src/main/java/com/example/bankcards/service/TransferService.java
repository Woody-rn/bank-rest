package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.exception.CardAccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardValidationException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.AesCardEncryptor;
import com.example.bankcards.util.CardDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final CardRepository cardRepository;
    private final TransferRepository transferRepository;
    private final AesCardEncryptor encryptor;
    private final CardDataMasker masker;

    @Transactional
    public TransferResponse transfer(Long userId, TransferRequest request) {
        validateTransferRequest(request);

        Card fromCard = getCardWithLock(request.fromCardId());
        Card toCard = getCard(request.toCardId());

        validateOwnership(fromCard, userId);
        validateOwnership(toCard, userId);
        validateCardActive(fromCard);
        validateCardActive(toCard);
        validateSufficientFunds(fromCard, request.amount());

        executeTransfer(fromCard, toCard, request.amount());
        Transfer transfer = saveTransfer(fromCard, toCard, request);

        log.info("Transfer completed: id={}, from={}, to={}, amount={}",
                transfer.getId(), fromCard.getId(), toCard.getId(), request.amount());

        return mapToResponse(transfer);
    }

    public Page<TransferResponse> getTransferHistory(Long cardId, Long userId, Pageable pageable) {
        Card card = getCard(cardId);
        validateOwnership(card, userId);

        return transferRepository
                .findByFromCardIdOrToCardIdOrderByCreatedAtDesc(cardId, cardId, pageable)
                .map(this::mapToResponse);
    }

    private void executeTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
        cardRepository.saveAll(List.of(fromCard, toCard));
    }

    private Transfer saveTransfer(Card fromCard, Card toCard, TransferRequest request) {
        Transfer transfer = Transfer.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(request.amount())
                .description(request.description())
                .build();
        return transferRepository.save(transfer);
    }

    private TransferResponse mapToResponse(Transfer transfer) {
        String fromMasked = masker.mask(encryptor.decrypt(transfer.getFromCard().getCardNumberEncrypted()));
        String toMasked = masker.mask(encryptor.decrypt(transfer.getToCard().getCardNumberEncrypted()));

        return new TransferResponse(
                transfer.getId(),
                fromMasked,
                toMasked,
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getCreatedAt()
        );
    }

    private void validateTransferRequest(TransferRequest request) {
        if (request.fromCardId().equals(request.toCardId())) {
            throw new CardValidationException("Cannot transfer to the same card");
        }
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardValidationException("Amount must be positive");
        }
    }

    private void validateOwnership(Card card, Long userId) {
        if (!card.getUser().getId().equals(userId)) {
            throw new CardAccessDeniedException("You don't own card: " + card.getId());
        }
    }

    private void validateCardActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardValidationException("Card is not active: " + card.getId() + " status=" + card.getStatus());
        }
    }

    private void validateSufficientFunds(Card card, BigDecimal amount) {
        if (card.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds: card=%s, balance=%s, amount=%s",
                            card.getId(), card.getBalance(), amount));
        }
    }

    private Card getCard(Long cardId) {
        return cardRepository.findByIdAndIsDeletedFalse(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
    }

    private Card getCardWithLock(Long cardId) {
        return cardRepository.findByIdWithLock(cardId)
                .orElseThrow(() -> new CardNotFoundException("Source card not found: " + cardId));
    }
}