package com.youssef3b4afi.financetracker.dashboard;

import com.youssef3b4afi.financetracker.common.CurrentUser;
import com.youssef3b4afi.financetracker.dashboard.dto.CategoryExpenseResponse;
import com.youssef3b4afi.financetracker.dashboard.dto.MonthlyTotalResponse;
import com.youssef3b4afi.financetracker.dashboard.dto.SummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

/**
 * Read-only endpoints for the dashboard. "month" is optional everywhere and means the current month
 * (in the server's time zone) when left out, e.g. GET /api/dashboard/summary?month=2026-10.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final int MAX_MONTHS = 24;

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /** US-10: income, expenses and balance for one month. */
    @GetMapping("/summary")
    public SummaryResponse summary(@AuthenticationPrincipal Jwt jwt,
                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.summary(CurrentUser.id(jwt), orCurrent(month));
    }

    /** US-11: expense totals per category for one month (donut chart). */
    @GetMapping("/expenses-by-category")
    public List<CategoryExpenseResponse> expensesByCategory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.expensesByCategory(CurrentUser.id(jwt), orCurrent(month));
    }

    /** US-12: income vs expenses for the last N months ending at "month" (bar chart). */
    @GetMapping("/monthly")
    public List<MonthlyTotalResponse> monthly(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(defaultValue = "12") int months) {
        // Clamp like the transactions page size: months=0 becomes 1, months=1000 becomes 24.
        return service.monthly(CurrentUser.id(jwt), orCurrent(month), Math.clamp(months, 1, MAX_MONTHS));
    }

    private static YearMonth orCurrent(YearMonth month) {
        return month != null ? month : YearMonth.now();
    }
}
