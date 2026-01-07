package com.mzc.lp.domain.course.dto.response;

import com.mzc.lp.domain.course.entity.CourseReview;

import java.time.Instant;

public record CourseReviewResponse(
        Long reviewId,
        Long courseId,
        Long userId,
        String userName,
        Integer rating,
        String content,
        Integer completionRate,  // 0-100 (%)
        Instant createdAt,
        Instant updatedAt
) {
    public static CourseReviewResponse from(CourseReview review, String userName) {
        return new CourseReviewResponse(
                review.getId(),
                review.getCourseId(),
                review.getUserId(),
                userName,
                review.getRating(),
                review.getContent(),
                review.getCompletionRate(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
