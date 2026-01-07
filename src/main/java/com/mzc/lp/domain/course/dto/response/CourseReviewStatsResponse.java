package com.mzc.lp.domain.course.dto.response;

public record CourseReviewStatsResponse(
        Long courseTimeId,
        Double averageRating,
        long reviewCount
) {
    public static CourseReviewStatsResponse of(Long courseTimeId, Double averageRating, long reviewCount) {
        return new CourseReviewStatsResponse(
                courseTimeId,
                averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0,
                reviewCount
        );
    }
}
