package com.youssef3b4afi.financetracker.transaction;

import com.youssef3b4afi.financetracker.category.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

/** Like CategoryRepository: every lookup is scoped by userId, never the plain findById. */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /** Used by CategoryService before deleting a category (US-6). */
    boolean existsByCategoryId(Long categoryId);

    /**
     * US-9 list. JPQL talks about entities and fields (Transaction, t.date), not tables and columns.
     * Each filter is "(:param IS NULL OR condition)": when the caller passes null, that line is always true.
     * Dates are cast because the Postgres driver sends them untyped, and "? IS NULL" alone gives Postgres
     * nothing to infer the type from ("could not determine data type of parameter").
     * JOIN FETCH loads each row's category in the same query (otherwise: 1 extra query per row).
     * Paging needs a total count; a fetch join can't be counted, so the count query is written separately.
     */
    @Query(value = """
            SELECT t FROM Transaction t JOIN FETCH t.category c
            WHERE t.user.id = :userId
              AND (cast(:from as LocalDate) IS NULL OR t.date >= :from)
              AND (cast(:to as LocalDate) IS NULL OR t.date <= :to)
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:type IS NULL OR c.type = :type)
            ORDER BY t.date DESC, t.id DESC
            """,
            countQuery = """
            SELECT count(t) FROM Transaction t JOIN t.category c
            WHERE t.user.id = :userId
              AND (cast(:from as LocalDate) IS NULL OR t.date >= :from)
              AND (cast(:to as LocalDate) IS NULL OR t.date <= :to)
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:type IS NULL OR c.type = :type)
            """)
    Page<Transaction> search(Long userId, LocalDate from, LocalDate to, Long categoryId, CategoryType type,
                             Pageable pageable);
}
