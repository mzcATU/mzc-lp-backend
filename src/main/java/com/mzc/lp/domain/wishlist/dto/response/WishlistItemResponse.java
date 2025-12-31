package com.mzc.lp.domain.wishlist.dto.response;

import com.mzc.lp.domain.wishlist.entity.WishlistItem;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class WishlistItemResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseThumbnailUrl;
    private String courseLevel;
    private String courseType;
    private Integer courseEstimatedHours;
    private Instant addedAt;

    public static WishlistItemResponse from(WishlistItem item) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .courseId(item.getCourseId())
                .addedAt(item.getCreatedAt())
                .build();
    }

    public static WishlistItemResponse of(
            WishlistItem item,
            String courseTitle,
            String courseThumbnailUrl,
            String courseLevel,
            String courseType,
            Integer courseEstimatedHours
    ) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .courseId(item.getCourseId())
                .courseTitle(courseTitle)
                .courseThumbnailUrl(courseThumbnailUrl)
                .courseLevel(courseLevel)
                .courseType(courseType)
                .courseEstimatedHours(courseEstimatedHours)
                .addedAt(item.getCreatedAt())
                .build();
    }
}
