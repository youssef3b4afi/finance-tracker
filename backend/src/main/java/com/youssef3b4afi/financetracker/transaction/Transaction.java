package com.youssef3b4afi.financetracker.transaction;

import com.youssef3b4afi.financetracker.category.Category;
import com.youssef3b4afi.financetracker.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * One income or expense. There is no "type" column: whether it's income or expense
 * comes from its category, so the two can never disagree.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    /** BigDecimal, never double: 0.1 + 0.2 is 0.30000000000000004 as a double, exactly 0.30 as a BigDecimal. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** LocalDate = a calendar day with no time or time zone, which is what "the date I paid" means. */
    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Transaction() {}

    public Transaction(User user, Category category, BigDecimal amount, LocalDate date, String note) {
        this.user = user;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Category getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }

    /** Everything the user typed can be edited (US-8); the owner and creation time can't. */
    public void update(Category category, BigDecimal amount, LocalDate date, String note) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }
}
