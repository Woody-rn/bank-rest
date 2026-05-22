package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardDataMaskerTest {

    private CardDataMasker cardDataMasker;

    @BeforeEach
    void setUp() {
        cardDataMasker = new CardDataMasker();
    }

    @Test
    void mask_shouldShowOnlyLast4Digits() {
        String original = "4532015112830366";

        String masked = cardDataMasker.mask(original);

        assertEquals("**** **** **** 0366", masked);
    }

    @Test
    void mask_shouldWorkForShortInput() {
        assertEquals("****", cardDataMasker.mask("12"));
        assertEquals("****", cardDataMasker.mask("123"));
    }

    @Test
    void mask_shouldHandleNull() {
        assertEquals("****", cardDataMasker.mask(null));
    }

}