package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.BlockRequestService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "Admin operations with cards, users and block requests")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CardService cardService;
    private final BlockRequestService blockRequestService;
    private final UserService userService;

    @Operation(summary = "Create a new card for any user")
    @PostMapping("/cards")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
    }

    @Operation(summary = "Get all cards with pagination")
    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @Operation(summary = "Get one card by ID")
    @GetMapping("/cards/{id}")
    public ResponseEntity<CardResponse> getCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @Operation(summary = "Block a card")
    @PutMapping("/cards/{id}/block")
    public ResponseEntity<CardResponse> blockCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.blockCard(id));
    }

    @Operation(summary = "Activate a card")
    @PutMapping("/cards/{id}/activate")
    public ResponseEntity<CardResponse> activateCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.activateCard(id));
    }

    @Operation(summary = "Soft delete a card (mark as removed)")
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create a new user")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @Operation(summary = "Get all active users")
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    @Operation(summary = "Get one user by ID")
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Disable a user (soft delete)")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all pending block requests")
    @GetMapping("/block-requests")
    public ResponseEntity<Page<BlockRequest>> getPendingRequests(Pageable pageable) {
        return ResponseEntity.ok(blockRequestService.getPendingRequests(pageable));
    }

    @Operation(summary = "Approve a block request")
    @PutMapping("/block-requests/{id}/approve")
    public ResponseEntity<BlockRequest> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(blockRequestService.approveRequest(id));
    }

    @Operation(summary = "Reject a block request")
    @PutMapping("/block-requests/{id}/reject")
    public ResponseEntity<BlockRequest> rejectRequest(@PathVariable Long id) {
        return ResponseEntity.ok(blockRequestService.rejectRequest(id));
    }
}