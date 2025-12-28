package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.stats.BooleanCountProjection;
import com.mzc.lp.common.dto.stats.DailyCountProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
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

    private static final int DAILY_TREND_DAYS = 30;

    @Override
    public OperatorTasksResponse getOperatorTasks() {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 대기 중인 작업
        long programsPendingApproval = programRepository.countPendingPrograms(tenantId);
        long courseTimesNeedingInstructor = courseTimeRepository.countCourseTimesNeedingInstructor(
                tenantId,
                List.of(CourseTimeStatus.RECRUITING, CourseTimeStatus.ONGOING),
                InstructorRole.MAIN,
                AssignmentStatus.ACTIVE
        );

        // 차수 통계
        List<StatusCountProjection> courseTimeStatusProjections =
                courseTimeRepository.countByTenantIdGroupByStatus(tenantId);
        List<TypeCountProjection> courseTimeDeliveryTypeProjections =
                courseTimeRepository.countByTenantIdGroupByDeliveryType(tenantId);
        List<BooleanCountProjection> courseTimeFreeProjections =
                courseTimeRepository.countByTenantIdGroupByFree(tenantId);
        long totalCourseTimes = courseTimeRepository.countByTenantId(tenantId);

        // 수강 통계
        long totalEnrollments = enrollmentRepository.countByTenantId(tenantId);
        List<StatusCountProjection> enrollmentStatusProjections =
                enrollmentRepository.countByTenantIdGroupByStatus(tenantId);
        List<TypeCountProjection> enrollmentTypeProjections =
                enrollmentRepository.countByTenantIdGroupByType(tenantId);
        Double completionRate = enrollmentRepository.getCompletionRateByTenantId(tenantId);
        Double averageCapacityUtilization = courseTimeRepository.getAverageCapacityUtilization(tenantId);

        // 일별 수강신청 추이 (최근 30일)
        List<DailyCountProjection> dailyEnrollments = getDailyEnrollments(tenantId);

        log.debug("운영 대시보드 조회 - 테넌트 ID: {}, 승인 대기: {}, 강사 미배정: {}, 전체 차수: {}, 전체 수강: {}",
                tenantId, programsPendingApproval, courseTimesNeedingInstructor,
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

    private List<DailyCountProjection> getDailyEnrollments(Long tenantId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(DAILY_TREND_DAYS - 1);

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<DailyCountProjection> dailyEnrollments =
                enrollmentRepository.countDailyEnrollments(tenantId, startInstant, endInstant);

        return dailyEnrollments != null ? dailyEnrollments : Collections.emptyList();
    }
}
