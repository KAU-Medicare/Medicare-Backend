package com.example.kaumedicare.Exception;

public class StandardCodeNotFoundException extends RuntimeException {
    public StandardCodeNotFoundException(String message) {
        super(message);
    }
}