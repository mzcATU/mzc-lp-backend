package com.mzc.lp.domain.student.dto.response;

import java.math.BigDecimal;

/**
 * 사용자별 수강 통계 Response
 */
public record UserEnrollmentStatsResponse(
        Long userId,
        Long totalEnrollments,          // 총 수강 횟수
        Long completedCount,            // 수료 횟수
        Long inProgressCount,           // 진행 중
        Long droppedCount,              // 중도 탈락
        Long failedCount,               // 미수료
        BigDecimal completionRate,      // 수료율 (%)
        BigDecimal averageScore,        // 평균 점수 (수료한 과정만)
        BigDecimal averageProgress      // 평균 진도율
) {
    public static UserEnrollmentStatsResponse of(
            Long userId,
            Long totalEnrollments,
            Long completedCount,
            Long inProgressCount,
            Long droppedCount,
            Long failedCount,
            Double averageScore,
            Double averageProgress
    ) {
        BigDecimal completionRate = totalEnrollments > 0
                ? BigDecimal.valueOf(completedCount * 100.0 / totalEnrollments).setScale(2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgScore = averageScore != null
                ? BigDecimal.valueOf(averageScore).setScale(2, BigDecimal.ROUND_HALF_UP)
                : null;

        BigDecimal avgProgress = averageProgress != null
                ? BigDecimal.valueOf(averageProgress).setScale(2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        return new UserEnrollmentStatsResponse(
                userId,
                totalEnrollments,
                completedCount,
                inProgressCount,
                droppedCount,
                failedCount,
                completionRate,
                avgScore,
                avgProgress
        );
    }
}
