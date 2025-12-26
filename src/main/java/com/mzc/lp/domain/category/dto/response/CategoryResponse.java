package com.mzc.lp.domain.category.dto.response;

import com.mzc.lp.domain.category.entity.Category;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String code,
        Integer sortOrder,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getSortOrder(),
                category.getActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
