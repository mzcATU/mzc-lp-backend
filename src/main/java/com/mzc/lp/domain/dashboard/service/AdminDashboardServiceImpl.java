package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.stats.DailyEnrollmentStatsProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.dashboard.constant.DashboardPeriod;
import com.mzc.lp.domain.dashboard.dto.response.AdminKpiResponse;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
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
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final int DEFAULT_DAILY_TREND_DAYS = 30;

    @Override
    public AdminKpiResponse getKpiStats(DashboardPeriod period) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 기간 필터 설정
        Instant startDate = period != null ? period.getStartInstant() : null;
        Instant endDate = period != null ? period.getEndInstant() : null;

        // 사용자 통계
        List<StatusCountProjection> userStatusProjections = getUserStatusProjections(tenantId, startDate, endDate);
        long totalUsers = getTotalUsers(tenantId, startDate, endDate);
        long newUsersInPeriod = getNewUsersInPeriod(tenantId, period);

        // 강의 통계 (Program 대신 Course 사용)
        List<StatusCountProjection> programStatusProjections = Collections.emptyList(); // Course에는 상태가 없음
        long totalPrograms = getTotalCourses(tenantId);

        // 수강 통계
        long totalEnrollments = getTotalEnrollments(tenantId, startDate, endDate);
        List<StatusCountProjection> enrollmentStatusProjections =
                getEnrollmentStatusProjections(tenantId, startDate, endDate);
        Double completionRate = getCompletionRate(tenantId, startDate, endDate);

        // 일별 추이
        List<DailyEnrollmentStatsProjection> dailyStats = getDailyStats(tenantId, period);

        log.debug("관리자 KPI 대시보드 조회 - 테넌트 ID: {}, 기간: {}, 전체 사용자: {}, 기간 내 신규: {}, 전체 강의: {}, 전체 수강: {}",
                tenantId, period != null ? period.getCode() : "전체",
                totalUsers, newUsersInPeriod, totalPrograms, totalEnrollments);

        return AdminKpiResponse.of(
                userStatusProjections,
                totalUsers,
                newUsersInPeriod,
                programStatusProjections,
                totalPrograms,
                totalEnrollments,
                enrollmentStatusProjections,
                completionRate,
                dailyStats
        );
    }

    private List<StatusCountProjection> getUserStatusProjections(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return userRepository.countByTenantIdGroupByStatusWithPeriod(tenantId, startDate, endDate);
        }
        return userRepository.countByTenantIdGroupByStatus(tenantId);
    }

    private long getTotalUsers(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return userRepository.countByTenantIdWithPeriod(tenantId, startDate, endDate);
        }
        return userRepository.countByTenantId(tenantId);
    }

    private long getNewUsersInPeriod(Long tenantId, DashboardPeriod period) {
        Instant since;
        if (period != null) {
            since = period.getStartInstant();
        } else {
            // 기간 미지정 시 이번 달 신규 사용자
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            since = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        return userRepository.countNewUsersSince(tenantId, since);
    }

    private long getTotalCourses(Long tenantId) {
        return courseRepository.countByTenantId(tenantId);
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

    private Double getCompletionRate(Long tenantId, Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null) {
            return enrollmentRepository.getCompletionRateByTenantIdWithPeriod(tenantId, startDate, endDate);
        }
        return enrollmentRepository.getCompletionRateByTenantId(tenantId);
    }

    private List<DailyEnrollmentStatsProjection> getDailyStats(Long tenantId, DashboardPeriod period) {
        int days = period != null ? period.getDays() : DEFAULT_DAILY_TREND_DAYS;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<DailyEnrollmentStatsProjection> dailyStats =
                enrollmentRepository.countDailyEnrollmentStats(tenantId, startInstant, endInstant);

        return dailyStats != null ? dailyStats : Collections.emptyList();
    }
}
