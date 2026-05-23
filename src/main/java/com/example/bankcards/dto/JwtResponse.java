package com.example.bankcards.dto;

public record JwtResponse(
        String token,
        String username,
        String role) {
}