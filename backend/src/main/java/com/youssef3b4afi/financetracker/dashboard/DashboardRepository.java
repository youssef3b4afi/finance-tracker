package com.youssef3b4afi.financetracker.dashboard;

import com.youssef3b4afi.financetracker.transaction.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Read-only totals over the transactions table for the dashboard (US-10..12). There is no dashboard table:
 * every number is a SUM computed by Postgres, so it can never drift out of date.
 * Extends the bare Repository (not JpaRepository) so it has no save/delete methods: only the queries below.
 * "SELECT new ...(...)" builds the result records directly, so no entities are loaded just to be added up.
 * Every query is scoped by userId and a date range, which the (user_id, date) index from V3 serves.
 */
public interface DashboardRepository extends Repository<Transaction, Long> {

    /** US-10: at most two rows, one for INCOME and one for EXPENSE (a type with no transactions is missing). */
    @Query("""
            SELECT new com.youssef3b4afi.financetracker.dashboard.TypeTotal(c.type, sum(t.amount))
            FROM Transaction t JOIN t.category c
            WHERE t.user.id = :userId AND t.date BETWEEN :from AND :to
            GROUP BY c.type
            """)
    List<TypeTotal> totalsByType(Long userId, LocalDate from, LocalDate to);

    /** US-11: one row per expense category that has transactions in the range, biggest first. */
    @Query("""
            SELECT new com.youssef3b4afi.financetracker.dashboard.CategoryTotal(c.id, c.name, sum(t.amount))
            FROM Transaction t JOIN t.category c
            WHERE t.user.id = :userId AND t.date BETWEEN :from AND :to
              AND c.type = com.youssef3b4afi.financetracker.category.CategoryType.EXPENSE
            GROUP BY c.id, c.name
            ORDER BY sum(t.amount) DESC, c.name
            """)
    List<CategoryTotal> expensesByCategory(Long userId, LocalDate from, LocalDate to);

    /** US-12: one row per (year, month, type) that has transactions; months with none are missing. */
    @Query("""
            SELECT new com.youssef3b4afi.financetracker.dashboard.MonthlyTypeTotal(
                       extract(year from t.date), extract(month from t.date), c.type, sum(t.amount))
            FROM Transaction t JOIN t.category c
            WHERE t.user.id = :userId AND t.date BETWEEN :from AND :to
            GROUP BY extract(year from t.date), extract(month from t.date), c.type
            """)
    List<MonthlyTypeTotal> monthlyTotalsByType(Long userId, LocalDate from, LocalDate to);
}
