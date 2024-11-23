package com.example.kaumedicare.Exception;

import lombok.Getter;

@Getter
public class DURConflictException extends RuntimeException {
    public DURConflictException(String message) {
        super(message);
    }
}