package com.example.bankcards.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CardDataMasker {

    public String mask(String plainText) {
        if (plainText == null || plainText.length() < 4) {
            log.warn("Attempted to mask invalid card number");
            return "****";
        }
        String last4 = plainText.substring(plainText.length() - 4);
        return "**** **** **** " + last4;
    }
}
