package com.youssef3b4afi.financetracker.dashboard.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

/** One bar group (US-12): income and expenses for one month, 0.00 when there were none. */
public record MonthlyTotalResponse(YearMonth month, BigDecimal income, BigDecimal expenses) {}
