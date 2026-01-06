package com.mzc.lp.domain.roadmap.dto.response;

public record RoadmapStatisticsResponse(
        Integer totalRoadmaps,
        Integer totalEnrollments,
        Double averageCourseCount
) {
    public static RoadmapStatisticsResponse of(
            long totalRoadmaps,
            long totalEnrollments,
            Double averageCourseCount
    ) {
        return new RoadmapStatisticsResponse(
                (int) totalRoadmaps,
                (int) totalEnrollments,
                averageCourseCount != null ? averageCourseCount : 0.0
        );
    }
}
