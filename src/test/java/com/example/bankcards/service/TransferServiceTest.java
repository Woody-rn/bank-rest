package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardAccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardValidationException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.AesCardEncryptor;
import com.example.bankcards.util.CardDataMasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private TransferRepository transferRepository;
    @Mock private AesCardEncryptor encryptor;
    @Mock private CardDataMasker masker;

    @InjectMocks
    private TransferService transferService;

    private Card fromCard;
    private Card toCard;
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        fromCard = Card.builder()
                .id(1L)
                .cardNumberEncrypted("enc_from")
                .user(user)
                .cardHolderName("FROM USER")
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .isDeleted(false)
                .build();

        toCard = Card.builder()
                .id(2L)
                .cardNumberEncrypted("enc_to")
                .user(user)
                .cardHolderName("TO USER")
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .isDeleted(false)
                .build();

        transfer = Transfer.builder()
                .id(1L)
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(BigDecimal.valueOf(200))
                .description("Test transfer")
                .build();
    }

    @Test
    void transfer_shouldSucceed_whenValidRequest() {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(200), "Test");

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(toCard));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(encryptor.decrypt("enc_from")).thenReturn("1111222233334444");
        when(encryptor.decrypt("enc_to")).thenReturn("5555666677778888");
        when(masker.mask("1111222233334444")).thenReturn("**** **** **** 4444");
        when(masker.mask("5555666677778888")).thenReturn("**** **** **** 8888");

        TransferResponse response = transferService.transfer(1L, request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(200), response.getAmount());
        assertEquals("**** **** **** 4444", response.getFromMaskedNumber());
        assertEquals("**** **** **** 8888", response.getToMaskedNumber());

        assertEquals(BigDecimal.valueOf(800), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(700), toCard.getBalance());

        verify(cardRepository).saveAll(anyList());
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void transfer_shouldThrowCardValidationException_whenSameCard() {
        TransferRequest request = new TransferRequest(1L, 1L, BigDecimal.valueOf(100), "Same");

        assertThrows(CardValidationException.class, () -> transferService.transfer(1L, request));
        verify(cardRepository, never()).findByIdWithLock(any());
    }

    @Test
    void transfer_shouldThrowCardNotFoundException_whenSourceCardNotFound() {
        TransferRequest request = new TransferRequest(99L, 2L, BigDecimal.valueOf(100), "Test");

        when(cardRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void transfer_shouldThrowCardNotFoundException_whenTargetCardNotFound() {
        TransferRequest request = new TransferRequest(1L, 99L, BigDecimal.valueOf(100), "Test");

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void transfer_shouldThrowCardAccessDeniedException_whenNotOwnerOfSource() {
        User otherUser = User.builder().id(99L).username("other").build();
        fromCard.setUser(otherUser);

        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100), "Test");

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(toCard));

        assertThrows(CardAccessDeniedException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void transfer_shouldThrowCardAccessDeniedException_whenNotOwnerOfTarget() {
        User otherUser = User.builder().id(99L).username("other").build();
        toCard.setUser(otherUser);

        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100), "Test");

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(toCard));

        assertThrows(CardAccessDeniedException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void transfer_shouldThrowCardValidationException_whenSourceNotActive() {
        fromCard.setStatus(CardStatus.BLOCKED);
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100), "Test");

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(toCard));

        assertThrows(CardValidationException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void transfer_shouldThrowCardValidationException_whenTargetNotActive() {
        toCard.setStatus(CardStatus.EXPIRED);
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100), "Test");

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(toCard));

        assertThrows(CardValidationException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void transfer_shouldThrowInsufficientFundsException_whenBalanceTooLow() {
        fromCard.setBalance(BigDecimal.valueOf(50));
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(100), "Test");

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(toCard));

        assertThrows(InsufficientFundsException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void transfer_shouldThrowCardValidationException_whenAmountZero() {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.ZERO, "Zero");

        assertThrows(CardValidationException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void transfer_shouldThrowCardValidationException_whenAmountNegative() {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(-100), "Negative");

        assertThrows(CardValidationException.class, () -> transferService.transfer(1L, request));
    }
}