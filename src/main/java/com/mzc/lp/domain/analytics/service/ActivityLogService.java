package com.mzc.lp.domain.analytics.service;

import com.mzc.lp.domain.analytics.constant.ActivityType;
import com.mzc.lp.domain.analytics.dto.response.ActivityLogResponse;
import com.mzc.lp.domain.analytics.dto.response.ActivityStatsResponse;
import com.mzc.lp.domain.analytics.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * 활동 로그 서비스 인터페이스
 */
public interface ActivityLogService {

    /**
     * 활동 로그 기록
     */
    void log(ActivityType type, String description, String targetType, Long targetId, String targetName);

    /**
     * 활동 로그 기록 (상세)
     */
    void log(Long userId, String userName, String userEmail, ActivityType type,
             String description, String targetType, Long targetId, String targetName,
             String ipAddress, String userAgent);

    /**
     * 테넌트 활동 로그 조회 (TA용)
     */
    Page<ActivityLogResponse> getActivityLogs(Long tenantId, ActivityType type, Pageable pageable);

    /**
     * 전체 시스템 활동 로그 조회 (SA용)
     */
    Page<ActivityLogResponse> getAllActivityLogs(ActivityType type, Pageable pageable);

    /**
     * 테넌트 활동 통계 조회 (TA용)
     */
    ActivityStatsResponse getActivityStats(Long tenantId, int days);

    /**
     * 전체 시스템 활동 통계 조회 (SA용)
     */
    ActivityStatsResponse getAllActivityStats(int days);

    /**
     * 최근 활동 조회 (TA용)
     */
    List<ActivityLogResponse> getRecentActivities(Long tenantId);

    /**
     * 최근 활동 조회 (SA용)
     */
    List<ActivityLogResponse> getAllRecentActivities();

    /**
     * 활동 로그 검색 (유저별, 유형별, 기간별 필터)
     */
    Page<ActivityLogResponse> searchLogs(
            Long tenantId,
            Long userId,
            ActivityType activityType,
            Instant startDate,
            Instant endDate,
            String keyword,
            Pageable pageable
    );

    /**
     * 내보내기용 로그 조회 (페이징 없이)
     */
    List<ActivityLog> getLogsForExport(
            Long tenantId,
            Long userId,
            ActivityType activityType,
            Instant startDate,
            Instant endDate
    );

    /**
     * 특정 사용자의 활동 로그 조회
     */
    Page<ActivityLogResponse> getActivityLogsByUser(Long tenantId, Long userId, Pageable pageable);
}
