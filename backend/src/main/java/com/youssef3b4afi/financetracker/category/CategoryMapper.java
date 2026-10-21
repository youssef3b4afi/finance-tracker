package com.youssef3b4afi.financetracker.category;

import com.youssef3b4afi.financetracker.category.dto.CategoryResponse;
import org.springframework.stereotype.Component;

/** Entity -> DTO by hand, so you can see exactly which fields leave the server. */
@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType());
    }
}
