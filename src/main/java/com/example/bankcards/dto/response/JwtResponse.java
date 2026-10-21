package com.example.bankcards.dto.response;

public record JwtResponse(
        String token,
        String username,
        String role) {
}