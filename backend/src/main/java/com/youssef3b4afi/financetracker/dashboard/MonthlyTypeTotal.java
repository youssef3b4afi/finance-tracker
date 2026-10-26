package com.youssef3b4afi.financetracker.dashboard;

import com.youssef3b4afi.financetracker.category.CategoryType;

import java.math.BigDecimal;

/**
 * One row of DashboardRepository.monthlyTotalsByType. Integer (not int) because that's what
 * Hibernate's extract() returns, and the constructor must match the selected types.
 */
public record MonthlyTypeTotal(Integer year, Integer month, CategoryType type, BigDecimal total) {}
