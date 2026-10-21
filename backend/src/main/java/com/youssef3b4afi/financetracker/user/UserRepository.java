package com.youssef3b4afi.financetracker.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data writes the implementation at startup by reading the method names:
 * findByEmail -> "SELECT ... FROM users WHERE email = ?".
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
