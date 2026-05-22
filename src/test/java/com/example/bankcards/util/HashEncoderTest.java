package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashEncoderTest {

    private HashEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new HashEncoder();
    }

    @Test
    void hash_shouldReturn64CharHexString() {
        String original = "4532015112830366";

        String hash = encoder.hash(original);

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    void hash_shouldBeDeterministic() {
        String original = "4532015112830366";

        String hash1 = encoder.hash(original);
        String hash2 = encoder.hash(original);

        assertEquals(hash1, hash2);
    }

    @Test
    void hash_shouldBeDifferentForDifferentInputs() {
        String hash1 = encoder.hash("4532015112830366");
        String hash2 = encoder.hash("5204738291037562");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void hash_shouldHandleEmptyString() {
        String hash = encoder.hash("");

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void hash_shouldThrowOnNull() {
        assertThrows(NullPointerException.class, () -> encoder.hash(null));
    }
}