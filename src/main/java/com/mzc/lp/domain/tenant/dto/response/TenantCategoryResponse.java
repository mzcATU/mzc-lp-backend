package com.mzc.lp.domain.tenant.dto.response;

import com.mzc.lp.domain.tenant.entity.TenantCategory;

import java.time.Instant;

/**
 * 테넌트 카테고리 응답 DTO
 */
public record TenantCategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String icon,
        Integer displayOrder,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public static TenantCategoryResponse from(TenantCategory category) {
        return new TenantCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIcon(),
                category.getDisplayOrder(),
                category.getEnabled(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
