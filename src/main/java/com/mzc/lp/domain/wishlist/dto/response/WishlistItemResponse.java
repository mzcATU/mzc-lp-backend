package com.mzc.lp.domain.wishlist.dto.response;

import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.wishlist.entity.WishlistItem;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class WishlistItemResponse {

    private Long id;
    private Long courseTimeId;
    private String courseTimeTitle;
    private String thumbnailUrl;
    private String level;
    private Integer estimatedHours;
    private Boolean isFree;
    private String price;
    private Instant addedAt;

    public static WishlistItemResponse from(WishlistItem item) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .courseTimeId(item.getCourseTimeId())
                .addedAt(item.getCreatedAt())
                .build();
    }

    public static WishlistItemResponse of(WishlistItem item, CourseTime courseTime, Course course) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .courseTimeId(item.getCourseTimeId())
                .courseTimeTitle(courseTime.getTitle())
                .thumbnailUrl(course != null ? course.getThumbnailUrl() : null)
                .level(course != null && course.getLevel() != null ? course.getLevel().name() : null)
                .estimatedHours(course != null ? course.getEstimatedHours() : null)
                .isFree(courseTime.isFree())
                .price(courseTime.getPrice() != null ? courseTime.getPrice().toString() : null)
                .addedAt(item.getCreatedAt())
                .build();
    }
}
