package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.category.entity.Category;
import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.entity.Course;

/**
 * 학습자용 Course 요약 응답 DTO
 */
public record CourseSummaryResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        CourseLevel level,
        CourseType type,
        Integer estimatedHours,
        Long categoryId,
        String categoryName
) {
    public static CourseSummaryResponse from(Course course) {
        if (course == null) {
            return null;
        }
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getLevel(),
                course.getType(),
                course.getEstimatedHours(),
                course.getCategoryId(),
                null
        );
    }

    /**
     * 목록용 요약 응답 (description 제외)
     */
    public static CourseSummaryResponse forList(Course course) {
        if (course == null) {
            return null;
        }
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                null,
                course.getThumbnailUrl(),
                course.getLevel(),
                course.getType(),
                course.getEstimatedHours(),
                course.getCategoryId(),
                null
        );
    }

    /**
     * 목록용 요약 응답 (카테고리 이름 포함)
     */
    public static CourseSummaryResponse forListWithCategory(Course course, Category category) {
        if (course == null) {
            return null;
        }
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                null,
                course.getThumbnailUrl(),
                course.getLevel(),
                course.getType(),
                course.getEstimatedHours(),
                course.getCategoryId(),
                category != null ? category.getName() : null
        );
    }
}
