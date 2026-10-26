package com.youssef3b4afi.financetracker.dashboard;

import com.youssef3b4afi.financetracker.category.CategoryType;

import java.math.BigDecimal;

/** One row of DashboardRepository.totalsByType. Internal query result, not an API response. */
public record TypeTotal(CategoryType type, BigDecimal total) {}
