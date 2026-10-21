package com.youssef3b4afi.financetracker.common;

/** Thrown when a resource doesn't exist or belongs to another user. Mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String resource, Long id) {
        super(resource + " not found: " + id);
    }
}
