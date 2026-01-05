package com.mzc.lp.domain.wishlist.dto.response;

import com.mzc.lp.domain.program.entity.Program;
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
    private String courseThumbnailUrl;
    private String courseLevel;
    private Integer courseEstimatedHours;
    private Instant addedAt;

    public static WishlistItemResponse from(WishlistItem item) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .courseTimeId(item.getCourseTimeId())
                .addedAt(item.getCreatedAt())
                .build();
    }

    public static WishlistItemResponse of(WishlistItem item, CourseTime courseTime, Program program) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .courseTimeId(item.getCourseTimeId())
                .courseTimeTitle(courseTime.getTitle())
                .courseThumbnailUrl(program != null ? program.getThumbnailUrl() : null)
                .courseLevel(program != null && program.getLevel() != null ? program.getLevel().name() : null)
                .courseEstimatedHours(program != null ? program.getEstimatedHours() : null)
                .addedAt(item.getCreatedAt())
                .build();
    }
}
