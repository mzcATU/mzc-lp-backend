package com.mzc.lp.domain.dashboard.dto.response;

import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OWNER 내 강의 통계 Response
 */
public record OwnerStatsResponse(
        Overview overview,
        EnrollmentStats enrollmentStats,
        List<ProgramStat> programStats
) {
    /**
     * 전체 개요
     */
    public record Overview(
            Long totalPrograms,
            Long totalCourseTimes,
            Long totalStudents
    ) {
        public static Overview of(Long totalPrograms, Long totalCourseTimes, Long totalStudents) {
            return new Overview(
                    totalPrograms != null ? totalPrograms : 0L,
                    totalCourseTimes != null ? totalCourseTimes : 0L,
                    totalStudents != null ? totalStudents : 0L
            );
        }
    }

    /**
     * 수강 통계
     */
    public record EnrollmentStats(
            Long totalEnrollments,
            ByStatus byStatus,
            BigDecimal completionRate
    ) {
        /**
         * 상태별 통계
         */
        public record ByStatus(
                Long enrolled,
                Long completed,
                Long dropped,
                Long failed
        ) {
            public static ByStatus from(List<StatusCountProjection> projections) {
                Map<String, Long> statusMap = projections.stream()
                        .collect(Collectors.toMap(
                                StatusCountProjection::getStatus,
                                StatusCountProjection::getCount
                        ));

                return new ByStatus(
                        statusMap.getOrDefault(EnrollmentStatus.ENROLLED.name(), 0L),
                        statusMap.getOrDefault(EnrollmentStatus.COMPLETED.name(), 0L),
                        statusMap.getOrDefault(EnrollmentStatus.DROPPED.name(), 0L),
                        statusMap.getOrDefault(EnrollmentStatus.FAILED.name(), 0L)
                );
            }
        }

        public static EnrollmentStats of(
                Long totalEnrollments,
                List<StatusCountProjection> statusProjections,
                Double completionRate
        ) {
            BigDecimal completionRateBd = completionRate != null
                    ? BigDecimal.valueOf(completionRate).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return new EnrollmentStats(
                    totalEnrollments != null ? totalEnrollments : 0L,
                    ByStatus.from(statusProjections),
                    completionRateBd
            );
        }
    }

    /**
     * 프로그램별 통계
     */
    public record ProgramStat(
            Long programId,
            String title,
            Long courseTimeCount,
            Long totalStudents,
            BigDecimal completionRate
    ) {
        public static ProgramStat of(
                Long programId,
                String title,
                Long courseTimeCount,
                Long totalStudents,
                Double completionRate
        ) {
            BigDecimal completionRateBd = completionRate != null
                    ? BigDecimal.valueOf(completionRate).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return new ProgramStat(
                    programId,
                    title,
                    courseTimeCount != null ? courseTimeCount : 0L,
                    totalStudents != null ? totalStudents : 0L,
                    completionRateBd
            );
        }
    }

    public static OwnerStatsResponse of(
            Long totalPrograms,
            Long totalCourseTimes,
            Long totalStudents,
            Long totalEnrollments,
            List<StatusCountProjection> enrollmentStatusProjections,
            Double completionRate,
            List<ProgramStat> programStats
    ) {
        return new OwnerStatsResponse(
                Overview.of(totalPrograms, totalCourseTimes, totalStudents),
                EnrollmentStats.of(totalEnrollments, enrollmentStatusProjections, completionRate),
                programStats
        );
    }
}
