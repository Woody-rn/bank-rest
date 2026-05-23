package com.example.bankcards.security;

import com.example.bankcards.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-must-be-at-least-32-bytes-long");
        jwtProperties.setExpirationMs(3600000);
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtTokenProvider.generateToken("admin", "ADMIN");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String token = jwtTokenProvider.generateToken("admin", "ADMIN");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("admin", username);
    }

    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        String token = jwtTokenProvider.generateToken("user", "USER");

        String role = jwtTokenProvider.getRoleFromToken(token);

        assertEquals("USER", role);
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        String token = jwtTokenProvider.generateToken("admin", "ADMIN");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalse_forInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.string"));
    }

    @Test
    void validateToken_shouldReturnFalse_forEmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void validateToken_shouldReturnFalse_forNullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void validateToken_shouldReturnFalse_forTamperedToken() {
        String token = jwtTokenProvider.generateToken("admin", "ADMIN");
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    void tokensWithDifferentUsers_shouldHaveDifferentSubjects() {
        String adminToken = jwtTokenProvider.generateToken("admin", "ADMIN");
        String userToken = jwtTokenProvider.generateToken("user", "USER");

        String adminSubject = jwtTokenProvider.getUsernameFromToken(adminToken);
        String userSubject = jwtTokenProvider.getUsernameFromToken(userToken);

        assertNotEquals(adminSubject, userSubject);
    }
}