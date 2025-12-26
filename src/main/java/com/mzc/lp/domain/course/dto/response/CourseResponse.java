package com.mzc.lp.domain.course.dto.response;

import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.entity.Course;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CourseResponse(
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
        Instant createdAt,
        Instant updatedAt
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
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
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
