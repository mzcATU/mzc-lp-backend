package com.mzc.lp.domain.course.dto.response;

import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.entity.Course;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CourseDetailResponse(
        Long courseId,
        String title,
        String description,
        String thumbnailUrl,
        CourseLevel level,
        CourseType type,
        Integer estimatedHours,
        Long categoryId,
        LocalDate startDate,
        LocalDate endDate,
        List<String> tags,
        List<CourseItemResponse> items,
        int itemCount,
        boolean isComplete,
        Double averageRating,
        long reviewCount,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * 완성도 판단 기준:
     * - title (필수, DB에서 NOT NULL)
     * - description (필수)
     * - categoryId (필수)
     * - 커리큘럼(items) 1개 이상
     */
    private static boolean checkCompleteness(Course course, int itemCount) {
        return course.getTitle() != null && !course.getTitle().isBlank()
                && course.getDescription() != null && !course.getDescription().isBlank()
                && course.getCategoryId() != null
                && itemCount > 0;
    }

    public static CourseDetailResponse from(Course course, List<CourseItemResponse> items) {
        int count = items != null ? items.size() : 0;
        return from(course, items, count, null, 0L);
    }

    public static CourseDetailResponse from(Course course, List<CourseItemResponse> items, int itemCount) {
        return from(course, items, itemCount, null, 0L);
    }

    public static CourseDetailResponse from(Course course, List<CourseItemResponse> items, int itemCount,
                                             Double averageRating, long reviewCount) {
        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getLevel(),
                course.getType(),
                course.getEstimatedHours(),
                course.getCategoryId(),
                course.getStartDate(),
                course.getEndDate(),
                course.getTags(),
                items,
                itemCount,
                checkCompleteness(course, itemCount),
                averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0,
                reviewCount,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
