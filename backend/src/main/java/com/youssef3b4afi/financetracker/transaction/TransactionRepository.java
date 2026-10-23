package com.youssef3b4afi.financetracker.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Like CategoryRepository: every lookup is scoped by userId, never the plain findById. */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /** Used by CategoryService before deleting a category (US-6). */
    boolean existsByCategoryId(Long categoryId);
}
