package com.youssef3b4afi.financetracker.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of PUT /api/categories/{id}. Only the name is editable (see Category.rename). */
public record CategoryRenameRequest(
        @NotBlank @Size(max = 50) String name) {

    public CategoryRenameRequest {
        name = name == null ? null : name.trim();
    }
}
