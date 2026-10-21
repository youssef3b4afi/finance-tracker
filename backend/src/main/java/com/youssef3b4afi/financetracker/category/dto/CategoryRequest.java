package com.youssef3b4afi.financetracker.category.dto;

import com.youssef3b4afi.financetracker.category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body of POST /api/categories. No id and no userId: the server decides both. */
public record CategoryRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull CategoryType type) {

    public CategoryRequest {
        name = name == null ? null : name.trim();
    }
}
