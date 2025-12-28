package com.mzc.lp.domain.dashboard.dto.response;

import com.mzc.lp.common.dto.stats.MonthlyEnrollmentStatsProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.user.constant.UserStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TENANT_ADMIN KPI 대시보드 Response
 */
public record AdminKpiResponse(
        UserStats userStats,
        ProgramStats programStats,
        EnrollmentStats enrollmentStats,
        List<MonthlyTrend> monthlyTrend
) {
    /**
     * 사용자 통계
     */
    public record UserStats(
            Long active,
            Long inactive,
            Long suspended,
            Long withdrawn,
            Long total,
            Long newThisMonth
    ) {
        public static UserStats of(
                List<StatusCountProjection> statusProjections,
                Long total,
                Long newThisMonth
        ) {
            Map<String, Long> statusMap = statusProjections.stream()
                    .collect(Collectors.toMap(
                            StatusCountProjection::getStatus,
                            StatusCountProjection::getCount
                    ));

            return new UserStats(
                    statusMap.getOrDefault(UserStatus.ACTIVE.name(), 0L),
                    statusMap.getOrDefault(UserStatus.INACTIVE.name(), 0L),
                    statusMap.getOrDefault(UserStatus.SUSPENDED.name(), 0L),
                    statusMap.getOrDefault(UserStatus.WITHDRAWN.name(), 0L),
                    total != null ? total : 0L,
                    newThisMonth != null ? newThisMonth : 0L
            );
        }
    }

    /**
     * 프로그램 통계
     */
    public record ProgramStats(
            Long draft,
            Long pending,
            Long approved,
            Long rejected,
            Long closed,
            Long total
    ) {
        public static ProgramStats of(
                List<StatusCountProjection> statusProjections,
                Long total
        ) {
            Map<String, Long> statusMap = statusProjections.stream()
                    .collect(Collectors.toMap(
                            StatusCountProjection::getStatus,
                            StatusCountProjection::getCount
                    ));

            return new ProgramStats(
                    statusMap.getOrDefault(ProgramStatus.DRAFT.name(), 0L),
                    statusMap.getOrDefault(ProgramStatus.PENDING.name(), 0L),
                    statusMap.getOrDefault(ProgramStatus.APPROVED.name(), 0L),
                    statusMap.getOrDefault(ProgramStatus.REJECTED.name(), 0L),
                    statusMap.getOrDefault(ProgramStatus.CLOSED.name(), 0L),
                    total != null ? total : 0L
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
     * 월별 추이
     */
    public record MonthlyTrend(
            String month,
            Long enrollments,
            Long completions
    ) {
        public static MonthlyTrend from(MonthlyEnrollmentStatsProjection projection) {
            String monthStr = String.format("%d-%02d", projection.getYear(), projection.getMonth());
            return new MonthlyTrend(
                    monthStr,
                    projection.getEnrollments() != null ? projection.getEnrollments() : 0L,
                    projection.getCompletions() != null ? projection.getCompletions() : 0L
            );
        }

        public static List<MonthlyTrend> fromList(List<MonthlyEnrollmentStatsProjection> projections) {
            return projections.stream()
                    .map(MonthlyTrend::from)
                    .toList();
        }
    }

    public static AdminKpiResponse of(
            List<StatusCountProjection> userStatusProjections,
            Long totalUsers,
            Long newUsersThisMonth,
            List<StatusCountProjection> programStatusProjections,
            Long totalPrograms,
            Long totalEnrollments,
            List<StatusCountProjection> enrollmentStatusProjections,
            Double completionRate,
            List<MonthlyEnrollmentStatsProjection> monthlyStats
    ) {
        return new AdminKpiResponse(
                UserStats.of(userStatusProjections, totalUsers, newUsersThisMonth),
                ProgramStats.of(programStatusProjections, totalPrograms),
                EnrollmentStats.of(totalEnrollments, enrollmentStatusProjections, completionRate),
                MonthlyTrend.fromList(monthlyStats)
        );
    }
}
