package com.mzc.lp.domain.student.dto.response;

import java.math.BigDecimal;

/**
 * 차수별 수강 통계 Response
 */
public record CourseTimeEnrollmentStatsResponse(
        Long courseTimeId,
        Long totalEnrollments,          // 총 수강생 수
        Long enrolledCount,             // 수강 중
        Long completedCount,            // 수료
        Long droppedCount,              // 중도 탈락
        Long failedCount,               // 미수료
        BigDecimal averageProgress,     // 평균 진도율 (0-100)
        BigDecimal completionRate       // 수료율 (%)
) {
    public static CourseTimeEnrollmentStatsResponse of(
            Long courseTimeId,
            Long totalEnrollments,
            Long enrolledCount,
            Long completedCount,
            Long droppedCount,
            Long failedCount,
            Double averageProgress
    ) {
        BigDecimal avgProgress = averageProgress != null
                ? BigDecimal.valueOf(averageProgress).setScale(2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal completionRate = totalEnrollments > 0
                ? BigDecimal.valueOf(completedCount * 100.0 / totalEnrollments).setScale(2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        return new CourseTimeEnrollmentStatsResponse(
                courseTimeId,
                totalEnrollments,
                enrolledCount,
                completedCount,
                droppedCount,
                failedCount,
                avgProgress,
                completionRate
        );
    }
}
