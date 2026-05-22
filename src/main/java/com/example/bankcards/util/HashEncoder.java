package com.example.bankcards.util;

import com.example.bankcards.exception.EncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Component
public class HashEncoder {
    private static final String SHA_256_ALGORITHM = "SHA-256";

    public String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256_ALGORITHM);
            byte[] hashBytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));

            log.debug("Card number hashed successfully");
            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to hash card number", e);
            throw new EncryptionException("Failed to hash card number", e);
        }
    }
}
