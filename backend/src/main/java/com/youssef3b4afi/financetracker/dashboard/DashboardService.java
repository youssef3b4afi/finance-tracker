package com.youssef3b4afi.financetracker.dashboard;

import com.youssef3b4afi.financetracker.category.CategoryType;
import com.youssef3b4afi.financetracker.dashboard.dto.CategoryExpenseResponse;
import com.youssef3b4afi.financetracker.dashboard.dto.MonthlyTotalResponse;
import com.youssef3b4afi.financetracker.dashboard.dto.SummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns the repository's raw sums into chart-ready responses. The one real rule here: the database only
 * returns rows that exist, so missing types and months are filled in as 0.00 for the charts.
 * No mapper class: each response is built in one place below, from a query result rather than an entity.
 */
@Service
public class DashboardService {

    private final DashboardRepository dashboard;

    public DashboardService(DashboardRepository dashboard) {
        this.dashboard = dashboard;
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary(Long userId, YearMonth month) {
        Map<CategoryType, BigDecimal> totals = new HashMap<>();
        dashboard.totalsByType(userId, month.atDay(1), month.atEndOfMonth())
                .forEach(row -> totals.put(row.type(), row.total()));

        BigDecimal income = totals.getOrDefault(CategoryType.INCOME, zero());
        BigDecimal expenses = totals.getOrDefault(CategoryType.EXPENSE, zero());
        return new SummaryResponse(month, income, expenses, income.subtract(expenses));
    }

    @Transactional(readOnly = true)
    public List<CategoryExpenseResponse> expensesByCategory(Long userId, YearMonth month) {
        return dashboard.expensesByCategory(userId, month.atDay(1), month.atEndOfMonth()).stream()
                .map(row -> new CategoryExpenseResponse(row.categoryId(), row.categoryName(), row.total()))
                .toList();
    }

    /** The last {@code months} months up to and including {@code last}, oldest first (left to right on the chart). */
    @Transactional(readOnly = true)
    public List<MonthlyTotalResponse> monthly(Long userId, YearMonth last, int months) {
        YearMonth first = last.minusMonths(months - 1L);

        // Index the sparse query rows by month so each month in the range can look up its two numbers.
        Map<YearMonth, Map<CategoryType, BigDecimal>> byMonth = new HashMap<>();
        dashboard.monthlyTotalsByType(userId, first.atDay(1), last.atEndOfMonth()).forEach(row ->
                byMonth.computeIfAbsent(YearMonth.of(row.year(), row.month()), m -> new HashMap<>())
                        .put(row.type(), row.total()));

        List<MonthlyTotalResponse> result = new ArrayList<>(months);
        for (YearMonth month = first; !month.isAfter(last); month = month.plusMonths(1)) {
            Map<CategoryType, BigDecimal> totals = byMonth.getOrDefault(month, Map.of());
            result.add(new MonthlyTotalResponse(month,
                    totals.getOrDefault(CategoryType.INCOME, zero()),
                    totals.getOrDefault(CategoryType.EXPENSE, zero())));
        }
        return result;
    }

    /** 0.00 rather than BigDecimal.ZERO (which is 0), so every amount in the JSON has the same 2-decimal shape. */
    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2);
    }
}
