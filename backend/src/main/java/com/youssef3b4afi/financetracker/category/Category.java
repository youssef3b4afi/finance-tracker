package com.youssef3b4afi.financetracker.category;

import com.youssef3b4afi.financetracker.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A user's own category, e.g. "Food" (EXPENSE) or "Salary" (INCOME). */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Many categories belong to one user. LAZY: don't load the user row unless we ask for it. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    /** Stored as the text "INCOME"/"EXPENSE", not as 0/1, so reordering the enum can't corrupt data. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryType type;

    protected Category() {}

    public Category(User user, String name, CategoryType type) {
        this.user = user;
        this.name = name;
        this.type = type;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }

    /** Only the name can change: switching INCOME/EXPENSE would flip the meaning of existing transactions. */
    public void rename(String name) {
        this.name = name;
    }
}
