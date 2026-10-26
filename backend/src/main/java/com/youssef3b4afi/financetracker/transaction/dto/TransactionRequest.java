package com.youssef3b4afi.financetracker.transaction.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Body of POST and PUT /api/transactions. No "type" field: income/expense comes from the category.
 * Jackson reads "12.50" straight into a BigDecimal, so no precision is lost on the way in.
 */
public record TransactionRequest(
        @NotNull Long categoryId,
        // @Digits matches NUMERIC(12,2): at most 10 digits before the point and 2 after (12.345 is rejected).
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull LocalDate date,
        @Size(max = 255) String note) {

    /** An empty or whitespace-only note is stored as "no note" (null). */
    public TransactionRequest {
        note = note == null || note.isBlank() ? null : note.trim();
    }
}
