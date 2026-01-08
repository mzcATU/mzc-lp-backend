package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.stats.BooleanCountProjection;
import com.mzc.lp.common.dto.stats.DailyCountProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.domain.dashboard.constant.DashboardPeriod;
import com.mzc.lp.domain.dashboard.dto.response.OperatorTasksResponse;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperatorDashboardServiceImpl implements OperatorDashboardService {

    private final ProgramRepository programRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final int DEFAULT_DAILY_TREND_DAYS = 30;

    @Override
    public OperatorTasksResponse getOperatorTasks(DashboardPeriod period) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 기간 필터 설정
        Instant startDate = period != null ? period.getStartInstant() : null;
        Instant endDate = period != null ? period.getEndInstant() : null;

        // 대기 중인 작업
        long programsPendingApproval = getProgramsPendingApproval(tenantId, startDate, endDate);
        long courseTimesNeedingInstructor = getCourseTimesNeedingInstructor(tenantId, startDate, endDate);

        // 차수 통계
        List<StatusCountProjection> courseTimeStatusProjections =
                getCourseTimeStatusProjections(tenantId, startDate, endDate);
        List<TypeCountProjection> courseTimeDeliveryTypeProjections =
                getCourseTimeDeliveryTypeProjections(tenantId, startDate, endDate);
        List<BooleanCountProjection> courseTimeFreeProjections =
                getCourseTimeFreeProjections(tenantId, startDate, endDate);
        long totalCourseTimes = getTotalCourseTimes(tenantId, startDate, endDate);

        // 수강 통계
        long totalEnrollments = getTotalEnrollments(tenantId, startDate, endDate);
        List<StatusCountProjection> enrollmentStatusProjections =
                getEnrollmentStatusProjections(tenantId, startDate, endDate);
        List<TypeCountProjection> enrollmentTypeProjections =
                getEnrollmentTypeProjections(tenantId, startDate, endDate);
        Double completionRate = getCompletionRate(tenantId, startDate, endDate);
        Double averageCapacityUtilization = getAverageCapacityUtilization(tenantId, startDate, endDate);

        // 일별 수강신청 추이
        List<DailyCountProjection> dailyEnrollments = getDailyEnrollments(tenantId, period);

        log.debug("운영 대시보드 조회 - 테넌트 ID: {}, 기간: {}, 승인 대기: {}, 강사 미배정: {}, 전체 차수: {}, 전체 수강: {}",
                tenantId, period != null ? period.getCode() : "전체",
                programsPendingApproval, courseTimesNeedingInstructor,
                totalCourseTimes, totalEnrollments);

        return OperatorTasksResponse.of(
                programsPendingApproval,
                courseTimesNeedingInstructor,
                courseTimeStatusProjections,
                courseTimeDeliveryTypeProjections,
                courseTimeFreeProjections,
                totalCourseTimes,
                totalEnrollments,
                enrollmentStatusProjections,
                enrollmentTypeProjections,
                completionRate,
                averageCapacityUtilization,
                dailyEnrollments
        );
    }

    private long getProgramsPendingApproval(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return programRepository.countPendingProgramsWithPeriod(tenantId, startDate, endDate);
        }
        return programRepository.countPendingPrograms(tenantId);
    }

    private long getCourseTimesNeedingInstructor(Long tenantId, Instant startDate, Instant endDate) {
        List<CourseTimeStatus> statuses = List.of(CourseTimeStatus.RECRUITING, CourseTimeStatus.ONGOING);
        if (startDate != null && endDate != null) {
            return courseTimeRepository.countCourseTimesNeedingInstructorWithPeriod(
                    tenantId, statuses, InstructorRole.MAIN, AssignmentStatus.ACTIVE, startDate, endDate);
        }
        return courseTimeRepository.countCourseTimesNeedingInstructor(
                tenantId, statuses, InstructorRole.MAIN, AssignmentStatus.ACTIVE);
    }

    private List<StatusCountProjection> getCourseTimeStatusProjections(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return courseTimeRepository.countByTenantIdGroupByStatusWithPeriod(tenantId, startDate, endDate);
        }
        return courseTimeRepository.countByTenantIdGroupByStatus(tenantId);
    }

    private List<TypeCountProjection> getCourseTimeDeliveryTypeProjections(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return courseTimeRepository.countByTenantIdGroupByDeliveryTypeWithPeriod(tenantId, startDate, endDate);
        }
        return courseTimeRepository.countByTenantIdGroupByDeliveryType(tenantId);
    }

    private List<BooleanCountProjection> getCourseTimeFreeProjections(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return courseTimeRepository.countByTenantIdGroupByFreeWithPeriod(tenantId, startDate, endDate);
        }
        return courseTimeRepository.countByTenantIdGroupByFree(tenantId);
    }

    private long getTotalCourseTimes(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return courseTimeRepository.countByTenantIdWithPeriod(tenantId, startDate, endDate);
        }
        return courseTimeRepository.countByTenantId(tenantId);
    }

    private long getTotalEnrollments(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return enrollmentRepository.countByTenantIdWithPeriod(tenantId, startDate, endDate);
        }
        return enrollmentRepository.countByTenantId(tenantId);
    }

    private List<StatusCountProjection> getEnrollmentStatusProjections(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return enrollmentRepository.countByTenantIdGroupByStatusWithPeriod(tenantId, startDate, endDate);
        }
        return enrollmentRepository.countByTenantIdGroupByStatus(tenantId);
    }

    private List<TypeCountProjection> getEnrollmentTypeProjections(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return enrollmentRepository.countByTenantIdGroupByTypeWithPeriod(tenantId, startDate, endDate);
        }
        return enrollmentRepository.countByTenantIdGroupByType(tenantId);
    }

    private Double getCompletionRate(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return enrollmentRepository.getCompletionRateByTenantIdWithPeriod(tenantId, startDate, endDate);
        }
        return enrollmentRepository.getCompletionRateByTenantId(tenantId);
    }

    private Double getAverageCapacityUtilization(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return courseTimeRepository.getAverageCapacityUtilizationWithPeriod(tenantId, startDate, endDate);
        }
        return courseTimeRepository.getAverageCapacityUtilization(tenantId);
    }

    private List<DailyCountProjection> getDailyEnrollments(Long tenantId, DashboardPeriod period) {
        int days = period != null ? period.getDays() : DEFAULT_DAILY_TREND_DAYS;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<DailyCountProjection> dailyEnrollments =
                enrollmentRepository.countDailyEnrollments(tenantId, startInstant, endInstant);

        return dailyEnrollments != null ? dailyEnrollments : Collections.emptyList();
    }
}
