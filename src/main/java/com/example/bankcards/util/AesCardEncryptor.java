package com.example.bankcards.util;

import com.example.bankcards.exception.EncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class AesCardEncryptor {

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;
    private static final int AES_256_KEY_SIZE = 32;

    private final SecretKeySpec secretKey;

    public AesCardEncryptor(@Value("${app.encryption.secret-key}") String secretKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKeyStr);
            if (keyBytes.length != AES_256_KEY_SIZE) {
                throw new EncryptionException(
                        "Encryption key must decode to exactly " + AES_256_KEY_SIZE
                                + " bytes, got: " + keyBytes.length);
            }
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
            log.info("CardEncryptionService initialized successfully");

        } catch (IllegalArgumentException e) {
            log.error("Invalid encryption key format", e);
            throw new EncryptionException("Invalid encryption key format. Must be valid Base64.", e);
        }
    }

    public String encrypt(String plainText) {
        try {
            byte[] initializationVector = generateIV();
            IvParameterSpec ivSpec = new IvParameterSpec(initializationVector);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = combineIVAndData(initializationVector, encrypted);

            log.debug("Card number encrypted successfully");
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("Failed to encrypt card number", e);
            throw new EncryptionException("Failed to encrypt card number", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            byte[] initializationVector = extractIV(combined);
            byte[] encrypted = extractData(combined);

            IvParameterSpec ivSpec = new IvParameterSpec(initializationVector);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            log.debug("Card number decrypted successfully");
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Failed to decrypt card data", e);
            throw new EncryptionException("Failed to decrypt card number", e);
        }
    }

    private byte[] generateIV() {
        byte[] initializationVector = new byte[IV_SIZE];
        new SecureRandom().nextBytes(initializationVector);

        return initializationVector;
    }

    private byte[] combineIVAndData(byte[] initializationVector, byte[] encrypted) {
        byte[] combined = new byte[IV_SIZE + encrypted.length];
        System.arraycopy(initializationVector, 0, combined, 0, IV_SIZE);
        System.arraycopy(encrypted, 0, combined, IV_SIZE, encrypted.length);

        return combined;
    }

    private byte[] extractIV(byte[] combined) {
        byte[] initializationVector = new byte[IV_SIZE];
        System.arraycopy(combined, 0, initializationVector, 0, IV_SIZE);

        return initializationVector;
    }

    private byte[] extractData(byte[] combined) {
        byte[] data = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, IV_SIZE, data, 0, data.length);

        return data;
    }
}