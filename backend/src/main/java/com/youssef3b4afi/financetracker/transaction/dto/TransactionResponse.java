package com.youssef3b4afi.financetracker.transaction.dto;

import com.youssef3b4afi.financetracker.category.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Includes the category's name and type so the list screen needs no extra request per row. */
public record TransactionResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String note,
        Long categoryId,
        String categoryName,
        CategoryType type) {}
