package com.example.bankcards.exception;

public class CardValidationException extends RuntimeException {
    public CardValidationException(String message) {
        super(message);
    }
}