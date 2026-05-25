package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.BlockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "User", description = "User operations with own cards and transfers")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final CardService cardService;
    private final TransferService transferService;
    private final BlockRequestService blockRequestService;

    @Operation(summary = "Get my cards with optional filter by status")
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

    @Operation(summary = "Get one of my cards by ID")
    @GetMapping("/cards/{id}")
    public ResponseEntity<CardResponse> getCard(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        if (cardService.isNotCardOwner(id, user.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @Operation(summary = "Get balance of my card")
    @GetMapping("/cards/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        if (cardService.isNotCardOwner(id, user.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(cardService.getCardById(id).getBalance());
    }

    @Operation(summary = "Request to block my card")
    @PostMapping("/cards/{id}/block-request")
    public ResponseEntity<Void> requestBlock(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        blockRequestService.requestBlock(id, user.getId());
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Transfer money between my cards")
    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> transfer(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request) {

        return ResponseEntity.ok(transferService.transfer(user.getId(), request));
    }

    @Operation(summary = "Get transfer history for my card")
    @GetMapping("/transfers")
    public ResponseEntity<Page<TransferResponse>> getTransferHistory(
            @AuthenticationPrincipal User user,
            @RequestParam Long cardId,
            Pageable pageable) {

        return ResponseEntity.ok(transferService.getTransferHistory(cardId, user.getId(), pageable));
    }
}