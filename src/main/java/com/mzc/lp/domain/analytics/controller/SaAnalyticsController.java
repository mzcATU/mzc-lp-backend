package com.mzc.lp.domain.analytics.controller;

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
 * 시스템 관리자용 활동 분석 API (전체 시스템)
 */
@RestController
@RequestMapping("/api/sa/analytics")
@RequiredArgsConstructor
public class SaAnalyticsController {

    private final ActivityLogService activityLogService;

    /**
     * 전체 시스템 활동 로그 목록 조회
     * GET /api/sa/analytics/logs
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getAllActivityLogs(
            @RequestParam(required = false) ActivityType type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ActivityLogResponse> logs = activityLogService.getAllActivityLogs(type, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * 전체 시스템 활동 통계 조회
     * GET /api/sa/analytics/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<ActivityStatsResponse>> getAllActivityStats(
            @RequestParam(defaultValue = "30") int days
    ) {
        ActivityStatsResponse stats = activityLogService.getAllActivityStats(days);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 최근 활동 목록 조회 (전체 시스템)
     * GET /api/sa/analytics/recent
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getAllRecentActivities() {
        List<ActivityLogResponse> activities = activityLogService.getAllRecentActivities();
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
