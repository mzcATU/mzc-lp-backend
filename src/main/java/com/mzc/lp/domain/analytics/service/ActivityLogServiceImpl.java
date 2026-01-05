package com.mzc.lp.domain.analytics.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.analytics.constant.ActivityType;
import com.mzc.lp.domain.analytics.dto.response.ActivityLogResponse;
import com.mzc.lp.domain.analytics.dto.response.ActivityStatsResponse;
import com.mzc.lp.domain.analytics.entity.ActivityLog;
import com.mzc.lp.domain.analytics.repository.ActivityLogRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @Async
    public void log(ActivityType type, String description, String targetType, Long targetId, String targetName) {
        try {
            // 현재 인증 정보에서 사용자 정보 추출
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
                log.warn("Cannot log activity: No authenticated user found");
                return;
            }

            Long tenantId = TenantContext.getCurrentTenantIdOrNull();
            Long userId = principal.id();
            String userEmail = principal.email();

            // 사용자 이름 조회
            String userName = userRepository.findById(userId)
                    .map(User::getName)
                    .orElse("Unknown");

            ActivityLog activityLog = ActivityLog.create(
                    tenantId,
                    userId,
                    userName,
                    userEmail,
                    type,
                    description,
                    targetType,
                    targetId,
                    targetName,
                    null,  // IP address - 컨트롤러에서 전달받아야 함
                    null   // User agent - 컨트롤러에서 전달받아야 함
            );

            activityLogRepository.save(activityLog);
            log.debug("Activity logged: type={}, userId={}, targetType={}, targetId={}",
                    type, userId, targetType, targetId);

        } catch (Exception e) {
            log.error("Failed to log activity: type={}, error={}", type, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void log(Long userId, String userName, String userEmail, ActivityType type,
                    String description, String targetType, Long targetId, String targetName,
                    String ipAddress, String userAgent) {
        try {
            Long tenantId = TenantContext.getCurrentTenantIdOrNull();

            ActivityLog activityLog = ActivityLog.create(
                    tenantId,
                    userId,
                    userName,
                    userEmail,
                    type,
                    description,
                    targetType,
                    targetId,
                    targetName,
                    ipAddress,
                    userAgent
            );

            activityLogRepository.save(activityLog);
            log.debug("Activity logged (detailed): type={}, userId={}, ip={}",
                    type, userId, ipAddress);

        } catch (Exception e) {
            log.error("Failed to log activity: type={}, userId={}, error={}",
                    type, userId, e.getMessage());
        }
    }

    @Override
    public Page<ActivityLogResponse> getActivityLogs(Long tenantId, ActivityType type, Pageable pageable) {
        Page<ActivityLog> logs;
        if (type != null) {
            logs = activityLogRepository.findByTenantIdAndActivityTypeOrderByCreatedAtDesc(
                    tenantId, type, pageable);
        } else {
            logs = activityLogRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
        }
        return logs.map(ActivityLogResponse::from);
    }

    @Override
    public Page<ActivityLogResponse> getAllActivityLogs(ActivityType type, Pageable pageable) {
        Page<ActivityLog> logs;
        if (type != null) {
            // 특정 타입 필터 - 전체 시스템 대상으로 별도 쿼리 필요
            logs = activityLogRepository.findAll(pageable);
            // TODO: 타입 필터링을 위한 별도 쿼리 추가 필요
        } else {
            logs = activityLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return logs.map(ActivityLogResponse::from);
    }

    @Override
    public ActivityStatsResponse getActivityStats(Long tenantId, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);

        // 전체 활동 수
        List<Object[]> typeCounts = activityLogRepository.countByActivityTypeSince(tenantId, since);
        long totalActivities = typeCounts.stream()
                .mapToLong(arr -> (Long) arr[1])
                .sum();

        // 오늘 활동 수
        List<Object[]> todayCounts = activityLogRepository.countByActivityTypeSince(tenantId, today);
        long todayActivities = todayCounts.stream()
                .mapToLong(arr -> (Long) arr[1])
                .sum();

        // 활동한 유저 수 (unique users)
        Set<Long> activeUserIds = activityLogRepository.findTop50ByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(ActivityLog::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        long activeUsers = activeUserIds.size();

        // 활동 유형별 카운트
        Map<String, Long> byActivityType = typeCounts.stream()
                .collect(Collectors.toMap(
                        arr -> ((ActivityType) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));

        // 일별 트렌드
        List<Object[]> dailyData = activityLogRepository.countDailyActivities(tenantId, since);
        List<ActivityStatsResponse.DailyActivityCount> dailyTrend = dailyData.stream()
                .map(arr -> new ActivityStatsResponse.DailyActivityCount(
                        arr[0].toString(),
                        ((Number) arr[1]).longValue()
                ))
                .collect(Collectors.toList());

        // 시간대별 트렌드
        List<Object[]> hourlyData = activityLogRepository.countHourlyActivitiesToday(tenantId);
        List<ActivityStatsResponse.HourlyActivityCount> hourlyTrend = hourlyData.stream()
                .map(arr -> new ActivityStatsResponse.HourlyActivityCount(
                        ((Number) arr[0]).intValue(),
                        ((Number) arr[1]).longValue()
                ))
                .collect(Collectors.toList());

        return new ActivityStatsResponse(
                totalActivities,
                todayActivities,
                activeUsers,
                byActivityType,
                dailyTrend,
                hourlyTrend
        );
    }

    @Override
    public ActivityStatsResponse getAllActivityStats(int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);

        // 전체 활동 수
        List<Object[]> typeCounts = activityLogRepository.countAllByActivityTypeSince(since);
        long totalActivities = typeCounts.stream()
                .mapToLong(arr -> (Long) arr[1])
                .sum();

        // 오늘 활동 수
        List<Object[]> todayCounts = activityLogRepository.countAllByActivityTypeSince(today);
        long todayActivities = todayCounts.stream()
                .mapToLong(arr -> (Long) arr[1])
                .sum();

        // 활동한 유저 수
        Set<Long> activeUserIds = activityLogRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(ActivityLog::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        long activeUsers = activeUserIds.size();

        // 활동 유형별 카운트
        Map<String, Long> byActivityType = typeCounts.stream()
                .collect(Collectors.toMap(
                        arr -> ((ActivityType) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));

        // SA 레벨에서는 전체 테넌트 합산이므로 일별/시간별 트렌드는 빈 값으로
        // (또는 별도 쿼리 추가 필요)
        List<ActivityStatsResponse.DailyActivityCount> dailyTrend = List.of();
        List<ActivityStatsResponse.HourlyActivityCount> hourlyTrend = List.of();

        return new ActivityStatsResponse(
                totalActivities,
                todayActivities,
                activeUsers,
                byActivityType,
                dailyTrend,
                hourlyTrend
        );
    }

    @Override
    public List<ActivityLogResponse> getRecentActivities(Long tenantId) {
        return activityLogRepository.findTop50ByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(ActivityLogResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogResponse> getAllRecentActivities() {
        return activityLogRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(ActivityLogResponse::from)
                .collect(Collectors.toList());
    }
}
