package com.youssef3b4afi.financetracker.dashboard.dto;

import java.math.BigDecimal;

/** One donut slice (US-11). The chart computes percentages itself from the totals. */
public record CategoryExpenseResponse(Long categoryId, String categoryName, BigDecimal total) {}
