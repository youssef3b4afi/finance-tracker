package com.youssef3b4afi.financetracker.dashboard.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

/** US-10 cards. YearMonth is written to JSON as "2026-10". balance = income - expenses (can be negative). */
public record SummaryResponse(YearMonth month, BigDecimal income, BigDecimal expenses, BigDecimal balance) {}
