package com.example.bankcards.util;

import com.example.bankcards.exception.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesCardEncryptorTest {

    private AesCardEncryptor encryptor;

    @BeforeEach
    void setUp() {
        String validKey = "9fMovLiS2BDudjdOQ39LMotcTBQyLJ1UYwiDe53Glsa=";
        encryptor = new AesCardEncryptor(validKey);
    }

    @Test
    void encryptDecrypt_shouldReturnOriginalValue() {
        String original = "4532015112830366";

        String encrypted = encryptor.encrypt(original);
        String decrypted = encryptor.decrypt(encrypted);

        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_shouldProduceDifferentResultsForSameInput() {
        String original = "4532015112830366";

        String encrypted1 = encryptor.encrypt(original);
        String encrypted2 = encryptor.encrypt(original);

        assertNotEquals(encrypted1, encrypted2);

        assertEquals(original, encryptor.decrypt(encrypted1));
        assertEquals(original, encryptor.decrypt(encrypted2));
    }

    @Test
    void encrypt_shouldNotContainOriginalValue() {
        String original = "4532015112830366";

        String encrypted = encryptor.encrypt(original);

        assertFalse(encrypted.contains(original));
    }

    @Test
    void constructor_shouldThrowEncryptionException_forInvalidKeyLength() {
        String shortKey = "qV3nL9pR2sT7wX1y";

        assertThrows(EncryptionException.class,
                () -> new AesCardEncryptor(shortKey));
    }

    @Test
    void decrypt_shouldThrowEncryptionException_forInvalidData() {
        assertThrows(EncryptionException.class,
                () -> encryptor.decrypt("not-valid-base64"));
    }

    @Test
    void constructor_shouldThrowEncryptionException_forNonBase64Key() {
        assertThrows(EncryptionException.class,
                () -> new AesCardEncryptor("not-valid-base64"));
    }


    @Test
    void decrypt_shouldThrowOnTruncatedData() {
        String original = "4532015112830366";
        String encrypted = encryptor.encrypt(original);

        String truncated = encrypted.substring(0, encrypted.length() - 4);

        assertThrows(EncryptionException.class,
                () -> encryptor.decrypt(truncated));
    }
}