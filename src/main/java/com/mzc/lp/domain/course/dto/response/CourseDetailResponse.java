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
        long itemCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static CourseDetailResponse from(Course course, List<CourseItemResponse> items) {
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
                items != null ? items.size() : 0,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
