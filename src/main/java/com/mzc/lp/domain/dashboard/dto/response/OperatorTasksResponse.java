package com.mzc.lp.domain.dashboard.dto.response;

import com.mzc.lp.common.dto.stats.BooleanCountProjection;
import com.mzc.lp.common.dto.stats.DailyCountProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OPERATOR 운영 대시보드 Response
 */
public record OperatorTasksResponse(
        PendingTasks pendingTasks,
        CourseTimeStats courseTimeStats,
        EnrollmentStats enrollmentStats,
        List<DailyEnrollment> dailyTrend
) {
    /**
     * 대기 중인 작업 통계
     */
    public record PendingTasks(
            Long programsPendingApproval,
            Long courseTimesNeedingInstructor
    ) {
        public static PendingTasks of(Long programsPendingApproval, Long courseTimesNeedingInstructor) {
            return new PendingTasks(
                    programsPendingApproval != null ? programsPendingApproval : 0L,
                    courseTimesNeedingInstructor != null ? courseTimesNeedingInstructor : 0L
            );
        }
    }

    /**
     * 차수 통계
     */
    public record CourseTimeStats(
            ByStatus byStatus,
            ByDeliveryType byDeliveryType,
            FreeVsPaid freeVsPaid,
            Long total
    ) {
        /**
         * 상태별 통계
         */
        public record ByStatus(
                Long draft,
                Long recruiting,
                Long ongoing,
                Long closed,
                Long archived
        ) {
            public static ByStatus from(List<StatusCountProjection> projections) {
                Map<String, Long> statusMap = projections.stream()
                        .collect(Collectors.toMap(
                                StatusCountProjection::getStatus,
                                StatusCountProjection::getCount
                        ));

                return new ByStatus(
                        statusMap.getOrDefault(CourseTimeStatus.DRAFT.name(), 0L),
                        statusMap.getOrDefault(CourseTimeStatus.RECRUITING.name(), 0L),
                        statusMap.getOrDefault(CourseTimeStatus.ONGOING.name(), 0L),
                        statusMap.getOrDefault(CourseTimeStatus.CLOSED.name(), 0L),
                        statusMap.getOrDefault(CourseTimeStatus.ARCHIVED.name(), 0L)
                );
            }
        }

        /**
         * 운영 방식별 통계
         */
        public record ByDeliveryType(
                Long online,
                Long offline,
                Long blended,
                Long live
        ) {
            public static ByDeliveryType from(List<TypeCountProjection> projections) {
                Map<String, Long> typeMap = projections.stream()
                        .collect(Collectors.toMap(
                                TypeCountProjection::getType,
                                TypeCountProjection::getCount
                        ));

                return new ByDeliveryType(
                        typeMap.getOrDefault(DeliveryType.ONLINE.name(), 0L),
                        typeMap.getOrDefault(DeliveryType.OFFLINE.name(), 0L),
                        typeMap.getOrDefault(DeliveryType.BLENDED.name(), 0L),
                        typeMap.getOrDefault(DeliveryType.LIVE.name(), 0L)
                );
            }
        }

        /**
         * 무료/유료 통계
         */
        public record FreeVsPaid(
                Long free,
                Long paid
        ) {
            public static FreeVsPaid from(List<BooleanCountProjection> projections) {
                Map<Boolean, Long> freeMap = projections.stream()
                        .collect(Collectors.toMap(
                                BooleanCountProjection::getValue,
                                BooleanCountProjection::getCount
                        ));

                return new FreeVsPaid(
                        freeMap.getOrDefault(true, 0L),
                        freeMap.getOrDefault(false, 0L)
                );
            }
        }

        public static CourseTimeStats of(
                List<StatusCountProjection> statusProjections,
                List<TypeCountProjection> deliveryTypeProjections,
                List<BooleanCountProjection> freeProjections,
                Long total
        ) {
            return new CourseTimeStats(
                    ByStatus.from(statusProjections),
                    ByDeliveryType.from(deliveryTypeProjections),
                    FreeVsPaid.from(freeProjections),
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
            ByType byType,
            BigDecimal completionRate,
            BigDecimal averageCapacityUtilization
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

        /**
         * 유형별 통계
         */
        public record ByType(
                Long voluntary,
                Long mandatory
        ) {
            public static ByType from(List<TypeCountProjection> projections) {
                Map<String, Long> typeMap = projections.stream()
                        .collect(Collectors.toMap(
                                TypeCountProjection::getType,
                                TypeCountProjection::getCount
                        ));

                return new ByType(
                        typeMap.getOrDefault("VOLUNTARY", 0L),
                        typeMap.getOrDefault("MANDATORY", 0L)
                );
            }
        }

        public static EnrollmentStats of(
                Long totalEnrollments,
                List<StatusCountProjection> statusProjections,
                List<TypeCountProjection> typeProjections,
                Double completionRate,
                Double averageCapacityUtilization
        ) {
            BigDecimal completionRateBd = completionRate != null
                    ? BigDecimal.valueOf(completionRate).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal capacityUtilBd = averageCapacityUtilization != null
                    ? BigDecimal.valueOf(averageCapacityUtilization).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return new EnrollmentStats(
                    totalEnrollments != null ? totalEnrollments : 0L,
                    ByStatus.from(statusProjections),
                    ByType.from(typeProjections),
                    completionRateBd,
                    capacityUtilBd
            );
        }
    }

    /**
     * 일별 수강신청 추이
     */
    public record DailyEnrollment(
            LocalDate date,
            Long enrollments
    ) {
        public static DailyEnrollment from(DailyCountProjection projection) {
            return new DailyEnrollment(
                    projection.getDate(),
                    projection.getCount() != null ? projection.getCount() : 0L
            );
        }

        public static List<DailyEnrollment> fromList(List<DailyCountProjection> projections) {
            return projections.stream()
                    .map(DailyEnrollment::from)
                    .toList();
        }
    }

    public static OperatorTasksResponse of(
            Long programsPendingApproval,
            Long courseTimesNeedingInstructor,
            List<StatusCountProjection> courseTimeStatusProjections,
            List<TypeCountProjection> courseTimeDeliveryTypeProjections,
            List<BooleanCountProjection> courseTimeFreeProjections,
            Long totalCourseTimes,
            Long totalEnrollments,
            List<StatusCountProjection> enrollmentStatusProjections,
            List<TypeCountProjection> enrollmentTypeProjections,
            Double completionRate,
            Double averageCapacityUtilization,
            List<DailyCountProjection> dailyEnrollments
    ) {
        return new OperatorTasksResponse(
                PendingTasks.of(programsPendingApproval, courseTimesNeedingInstructor),
                CourseTimeStats.of(
                        courseTimeStatusProjections,
                        courseTimeDeliveryTypeProjections,
                        courseTimeFreeProjections,
                        totalCourseTimes
                ),
                EnrollmentStats.of(
                        totalEnrollments,
                        enrollmentStatusProjections,
                        enrollmentTypeProjections,
                        completionRate,
                        averageCapacityUtilization
                ),
                DailyEnrollment.fromList(dailyEnrollments)
        );
    }
}
