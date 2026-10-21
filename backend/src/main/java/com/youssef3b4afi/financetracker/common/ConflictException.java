package com.youssef3b4afi.financetracker.common;

/** Thrown when a request breaks a business rule (duplicate name, category in use, ...). Mapped to HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
