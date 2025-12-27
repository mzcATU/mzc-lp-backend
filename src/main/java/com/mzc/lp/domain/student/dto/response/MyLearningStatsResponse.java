package com.mzc.lp.domain.student.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 내 학습 통계 Response (마이페이지용)
 */
public record MyLearningStatsResponse(
        Overview overview,
        Progress progress
) {
    /**
     * 학습 개요 통계
     */
    public record Overview(
            Long totalCourses,
            Long inProgress,
            Long completed,
            Long dropped,
            Long failed,
            BigDecimal completionRate,
            ByType byType
    ) {
        /**
         * 수강 유형별 통계
         */
        public record ByType(
                Long voluntary,
                Long mandatory
        ) {
            public static ByType of(Long voluntary, Long mandatory) {
                return new ByType(
                        voluntary != null ? voluntary : 0L,
                        mandatory != null ? mandatory : 0L
                );
            }
        }

        public static Overview of(
                Long totalCourses,
                Long inProgress,
                Long completed,
                Long dropped,
                Long failed,
                Long voluntary,
                Long mandatory
        ) {
            BigDecimal completionRate = totalCourses > 0
                    ? BigDecimal.valueOf(completed * 100.0 / totalCourses)
                        .setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return new Overview(
                    totalCourses,
                    inProgress,
                    completed,
                    dropped,
                    failed,
                    completionRate,
                    ByType.of(voluntary, mandatory)
            );
        }
    }

    /**
     * 학습 진행 통계
     */
    public record Progress(
            BigDecimal averageProgress,
            BigDecimal averageScore
    ) {
        public static Progress of(Double averageProgress, Double averageScore) {
            BigDecimal avgProgress = averageProgress != null
                    ? BigDecimal.valueOf(averageProgress).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal avgScore = averageScore != null
                    ? BigDecimal.valueOf(averageScore).setScale(1, RoundingMode.HALF_UP)
                    : null;

            return new Progress(avgProgress, avgScore);
        }
    }

    public static MyLearningStatsResponse of(
            Long totalCourses,
            Long inProgress,
            Long completed,
            Long dropped,
            Long failed,
            Long voluntary,
            Long mandatory,
            Double averageProgress,
            Double averageScore
    ) {
        return new MyLearningStatsResponse(
                Overview.of(totalCourses, inProgress, completed, dropped, failed, voluntary, mandatory),
                Progress.of(averageProgress, averageScore)
        );
    }
}
