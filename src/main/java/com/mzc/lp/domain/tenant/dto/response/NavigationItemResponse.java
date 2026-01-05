package com.mzc.lp.domain.tenant.dto.response;

import com.mzc.lp.domain.tenant.entity.NavigationItem;

import java.time.Instant;

/**
 * 네비게이션 아이템 응답 DTO
 */
public record NavigationItemResponse(
        Long id,
        String label,
        String icon,
        String path,
        Boolean enabled,
        Integer displayOrder,
        String target,
        Instant createdAt,
        Instant updatedAt
) {
    public static NavigationItemResponse from(NavigationItem item) {
        return new NavigationItemResponse(
                item.getId(),
                item.getLabel(),
                item.getIcon(),
                item.getPath(),
                item.getEnabled(),
                item.getDisplayOrder(),
                item.getTarget(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
