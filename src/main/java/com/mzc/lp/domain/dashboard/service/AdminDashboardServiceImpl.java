package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.stats.MonthlyEnrollmentStatsProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.domain.dashboard.dto.response.AdminKpiResponse;
import com.mzc.lp.domain.program.repository.ProgramRepository;
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
    private final ProgramRepository programRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final int MONTHLY_TREND_MONTHS = 12;

    @Override
    public AdminKpiResponse getKpiStats() {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 사용자 통계
        List<StatusCountProjection> userStatusProjections =
                userRepository.countByTenantIdGroupByStatus(tenantId);
        long totalUsers = userRepository.countByTenantId(tenantId);
        long newUsersThisMonth = getNewUsersThisMonth(tenantId);

        // 프로그램 통계
        List<StatusCountProjection> programStatusProjections =
                programRepository.countByTenantIdGroupByStatus(tenantId);
        long totalPrograms = programRepository.countByTenantId(tenantId);

        // 수강 통계
        long totalEnrollments = enrollmentRepository.countByTenantId(tenantId);
        List<StatusCountProjection> enrollmentStatusProjections =
                enrollmentRepository.countByTenantIdGroupByStatus(tenantId);
        Double completionRate = enrollmentRepository.getCompletionRateByTenantId(tenantId);

        // 월별 추이 (최근 12개월)
        List<MonthlyEnrollmentStatsProjection> monthlyStats = getMonthlyStats(tenantId);

        log.debug("관리자 KPI 대시보드 조회 - 테넌트 ID: {}, 전체 사용자: {}, 이번 달 신규: {}, 전체 프로그램: {}, 전체 수강: {}",
                tenantId, totalUsers, newUsersThisMonth, totalPrograms, totalEnrollments);

        return AdminKpiResponse.of(
                userStatusProjections,
                totalUsers,
                newUsersThisMonth,
                programStatusProjections,
                totalPrograms,
                totalEnrollments,
                enrollmentStatusProjections,
                completionRate,
                monthlyStats
        );
    }

    private long getNewUsersThisMonth(Long tenantId) {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        Instant since = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return userRepository.countNewUsersSince(tenantId, since);
    }

    private List<MonthlyEnrollmentStatsProjection> getMonthlyStats(Long tenantId) {
        LocalDate endDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        LocalDate startDate = endDate.minusMonths(MONTHLY_TREND_MONTHS);

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<MonthlyEnrollmentStatsProjection> monthlyStats =
                enrollmentRepository.countMonthlyEnrollmentStats(tenantId, startInstant, endInstant);

        return monthlyStats != null ? monthlyStats : Collections.emptyList();
    }
}
