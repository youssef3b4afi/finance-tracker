package com.youssef3b4afi.financetracker.transaction.dto;

import com.youssef3b4afi.financetracker.category.CategoryType;

import java.time.LocalDate;

/** Optional filters for the list (US-9). A null field means "don't filter on this". */
public record TransactionFilter(LocalDate from, LocalDate to, Long categoryId, CategoryType type) {}
