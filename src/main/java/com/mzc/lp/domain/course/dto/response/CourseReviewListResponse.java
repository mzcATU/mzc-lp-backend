package com.mzc.lp.domain.course.dto.response;

import java.util.List;

public record CourseReviewListResponse(
        List<CourseReviewResponse> reviews,
        int totalPages,
        long totalElements,
        int currentPage,
        int pageSize,
        Double averageRating,
        long reviewCount
) {
    public static CourseReviewListResponse of(
            List<CourseReviewResponse> reviews,
            int totalPages,
            long totalElements,
            int currentPage,
            int pageSize,
            Double averageRating,
            long reviewCount
    ) {
        return new CourseReviewListResponse(
                reviews,
                totalPages,
                totalElements,
                currentPage,
                pageSize,
                averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0,
                reviewCount
        );
    }
}
