package com.youssef3b4afi.financetracker.dashboard;

import java.math.BigDecimal;

/** One row of DashboardRepository.expensesByCategory. Internal query result, not an API response. */
public record CategoryTotal(Long categoryId, String categoryName, BigDecimal total) {}
