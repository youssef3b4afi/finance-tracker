package com.youssef3b4afi.financetracker.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * JSON shape for any paged list. We copy the fields we want instead of returning Spring's Page,
 * whose JSON layout is an internal detail that can change between Spring versions.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
