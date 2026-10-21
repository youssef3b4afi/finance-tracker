package com.youssef3b4afi.financetracker.category.dto;

import com.youssef3b4afi.financetracker.category.CategoryType;

public record CategoryResponse(Long id, String name, CategoryType type) {}
