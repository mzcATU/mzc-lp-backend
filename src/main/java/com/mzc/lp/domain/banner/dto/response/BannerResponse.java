package com.mzc.lp.domain.banner.dto.response;

import com.mzc.lp.domain.banner.constant.BannerPosition;
import com.mzc.lp.domain.banner.entity.Banner;

import java.time.Instant;
import java.time.LocalDate;

public record BannerResponse(
        Long id,
        String title,
        String imageUrl,
        String linkUrl,
        String linkTarget,
        BannerPosition position,
        Integer sortOrder,
        Boolean isActive,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        Boolean isDisplayable,
        Instant createdAt,
        Instant updatedAt
) {
    public static BannerResponse from(Banner banner) {
        return new BannerResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getImageUrl(),
                banner.getLinkUrl(),
                banner.getLinkTarget(),
                banner.getPosition(),
                banner.getSortOrder(),
                banner.getIsActive(),
                banner.getStartDate(),
                banner.getEndDate(),
                banner.getDescription(),
                banner.isDisplayable(),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }
}
