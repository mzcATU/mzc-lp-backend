package com.mzc.lp.domain.analytics.service;

import com.mzc.lp.domain.analytics.constant.ActivityType;
import com.mzc.lp.domain.analytics.dto.response.ActivityLogResponse;
import com.mzc.lp.domain.analytics.dto.response.ActivityStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}
