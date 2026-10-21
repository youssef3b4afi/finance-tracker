package com.youssef3b4afi.financetracker.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Every query takes a userId: that's how "users only see their own data" (US-3) is enforced.
 * Never use the inherited findById(id) for categories — it would find anyone's category.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUserIdOrderByNameAsc(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
