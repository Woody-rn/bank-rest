package com.example.bankcards.util;

import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class CardNumberGenerator {

    private static final String PREFIX = "4";
    private static final int GENERATED_DIGITS = 15;
    private final SecureRandom secureRandom = new SecureRandom();
    private final HashEncoder hashEncoder;
    private final CardRepository cardRepository;

    public String generateUniqueNumber() {
        String number;
        String hash;
        do {
            number = generate();
            hash = hashEncoder.hash(number);
        } while (cardRepository.existsByCardNumberHash(hash));
        return number;
    }

    private String generate() {
        StringBuilder sb = new StringBuilder(PREFIX);
        for (int i = 0; i < GENERATED_DIGITS; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}