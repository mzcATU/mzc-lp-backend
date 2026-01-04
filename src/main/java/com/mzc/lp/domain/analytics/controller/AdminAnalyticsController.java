package com.mzc.lp.domain.analytics.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.analytics.constant.ActivityType;
import com.mzc.lp.domain.analytics.dto.response.ActivityLogResponse;
import com.mzc.lp.domain.analytics.dto.response.ActivityStatsResponse;
import com.mzc.lp.domain.analytics.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 테넌트 관리자용 활동 분석 API
 */
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final ActivityLogService activityLogService;

    /**
     * 활동 로그 목록 조회
     * GET /api/admin/analytics/logs
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getActivityLogs(
            @RequestParam(required = false) ActivityType type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Page<ActivityLogResponse> logs = activityLogService.getActivityLogs(tenantId, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * 활동 통계 조회
     * GET /api/admin/analytics/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<ActivityStatsResponse>> getActivityStats(
            @RequestParam(defaultValue = "30") int days
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        ActivityStatsResponse stats = activityLogService.getActivityStats(tenantId, days);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 최근 활동 목록 조회 (간단 버전)
     * GET /api/admin/analytics/recent
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getRecentActivities() {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<ActivityLogResponse> activities = activityLogService.getRecentActivities(tenantId);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
