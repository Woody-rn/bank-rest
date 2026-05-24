package com.example.bankcards.exception;

public class BlockRequestNotFoundException extends RuntimeException {
    public BlockRequestNotFoundException(String message) {
        super(message);
    }
}