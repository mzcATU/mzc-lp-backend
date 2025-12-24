package com.mzc.lp.domain.iis.dto.response;

import java.util.List;

/**
 * 강사 개인 상세 통계 Response (차수별 통계 포함)
 */
public record InstructorDetailStatResponse(
        Long userId,
        String userName,
        Long totalCount,
        Long mainCount,
        Long subCount,
        List<CourseTimeStatResponse> courseTimeStats
) {
    public static InstructorDetailStatResponse of(
            Long userId,
            String userName,
            Long totalCount,
            Long mainCount,
            Long subCount,
            List<CourseTimeStatResponse> courseTimeStats
    ) {
        return new InstructorDetailStatResponse(
                userId,
                userName,
                totalCount,
                mainCount,
                subCount,
                courseTimeStats != null ? courseTimeStats : List.of()
        );
    }

    public static InstructorDetailStatResponse from(InstructorStatResponse stat, List<CourseTimeStatResponse> courseTimeStats) {
        return new InstructorDetailStatResponse(
                stat.userId(),
                stat.userName(),
                stat.totalCount(),
                stat.mainCount(),
                stat.subCount(),
                courseTimeStats != null ? courseTimeStats : List.of()
        );
    }
}
