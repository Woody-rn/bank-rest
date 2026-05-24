package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.BlockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final CardService cardService;
    private final TransferService transferService;
    private final BlockRequestService blockRequestService;

    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) CardStatus status,
            Pageable pageable) {

        if (status != null) {
            return ResponseEntity.ok(cardService.getUserCardsByStatus(user.getId(), status, pageable));
        }
        return ResponseEntity.ok(cardService.getUserCards(user.getId(), pageable));
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<CardResponse> getCard(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        if (cardService.isNotCardOwner(id, user.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/cards/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        if (cardService.isNotCardOwner(id, user.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(cardService.getCardById(id).getBalance());
    }

    @PostMapping("/cards/{id}/block-request")
    public ResponseEntity<Void> requestBlock(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        blockRequestService.requestBlock(id, user.getId());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> transfer(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request) {

        return ResponseEntity.ok(transferService.transfer(user.getId(), request));
    }

    @GetMapping("/transfers")
    public ResponseEntity<Page<TransferResponse>> getTransferHistory(
            @AuthenticationPrincipal User user,
            @RequestParam Long cardId,
            Pageable pageable) {

        return ResponseEntity.ok(transferService.getTransferHistory(cardId, user.getId(), pageable));
    }
}