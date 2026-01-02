package com.mzc.lp.domain.tu.dto.response;

import java.util.List;

/**
 * TU 강의 탐색 목록 응답 DTO
 */
public record CourseExploreResponse(
        List<CourseExploreItemResponse> courses,
        long totalCount,
        int page,
        int pageSize,
        int totalPages
) {
    public static CourseExploreResponse of(
            List<CourseExploreItemResponse> courses,
            long totalCount,
            int page,
            int pageSize
    ) {
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        return new CourseExploreResponse(courses, totalCount, page, pageSize, totalPages);
    }
}
