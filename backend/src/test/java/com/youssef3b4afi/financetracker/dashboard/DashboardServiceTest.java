package com.youssef3b4afi.financetracker.dashboard;

import com.youssef3b4afi.financetracker.category.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Unit test for the zero-filling and arithmetic; the SQL itself is covered by DashboardIntegrationTest. */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final Long USER_ID = 1L;
    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    @Mock
    DashboardRepository dashboard;

    DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(dashboard);
    }

    @Test
    void summaryBalanceCanBeNegativeAndMissingTypeIsZero() {
        when(dashboard.totalsByType(USER_ID, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(new TypeTotal(CategoryType.EXPENSE, new BigDecimal("40.00"))));

        var summary = service.summary(USER_ID, OCTOBER);

        assertThat(summary.income()).isEqualTo(new BigDecimal("0.00"));
        assertThat(summary.balance()).isEqualTo(new BigDecimal("-40.00"));
    }

    @Test
    void monthlyQueriesWholeRangeAndFillsMissingMonths() {
        when(dashboard.monthlyTotalsByType(USER_ID, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(new MonthlyTypeTotal(2026, 9, CategoryType.INCOME, new BigDecimal("5.00"))));

        var months = service.monthly(USER_ID, OCTOBER, 3);

        assertThat(months).extracting(m -> m.month().toString()).containsExactly("2026-08", "2026-09", "2026-10");
        assertThat(months.get(1).income()).isEqualTo(new BigDecimal("5.00"));
        assertThat(months.get(1).expenses()).isEqualTo(new BigDecimal("0.00"));
        assertThat(months.get(2).income()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void monthlyRangeCanCrossYearBoundary() {
        var months = service.monthly(USER_ID, YearMonth.of(2026, 1), 2);

        assertThat(months).extracting(m -> m.month().toString()).containsExactly("2025-12", "2026-01");
    }
}
