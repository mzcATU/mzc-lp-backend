package com.mzc.lp.domain.analytics.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.analytics.constant.ActivityType;
import com.mzc.lp.domain.analytics.dto.response.ActivityLogResponse;
import com.mzc.lp.domain.analytics.dto.response.ActivityStatsResponse;
import com.mzc.lp.domain.analytics.entity.ActivityLog;
import com.mzc.lp.domain.analytics.service.ActivityLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 테넌트 관리자용 활동 분석 API
 */
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final ActivityLogService activityLogService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

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

    /**
     * 활동 로그 검색 (유저별, 유형별, 기간별 필터)
     * GET /api/admin/analytics/logs/search
     */
    @GetMapping("/logs/search")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> searchLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Page<ActivityLogResponse> logs = activityLogService.searchLogs(
                tenantId, userId, type, startDate, endDate, keyword, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * 특정 사용자의 활동 로그 조회
     * GET /api/admin/analytics/logs/users/{userId}
     */
    @GetMapping("/logs/users/{userId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getLogsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Page<ActivityLogResponse> logs = activityLogService.getActivityLogsByUser(tenantId, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * 활동 유형 목록 조회
     * GET /api/admin/analytics/types
     */
    @GetMapping("/types")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<ActivityTypeInfo>>> getActivityTypes() {
        List<ActivityTypeInfo> types = Arrays.stream(ActivityType.values())
                .map(type -> new ActivityTypeInfo(type.name(), getActivityTypeDescription(type)))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    /**
     * 활동 로그 CSV 내보내기
     * GET /api/admin/analytics/logs/export
     */
    @GetMapping("/logs/export")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public void exportLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            HttpServletResponse response
    ) throws IOException {
        Long tenantId = TenantContext.getCurrentTenantId();
        List<ActivityLog> logs = activityLogService.getLogsForExport(tenantId, userId, type, startDate, endDate);

        // CSV 응답 설정
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String filename = "activity_logs_" + Instant.now().toEpochMilli() + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        // BOM for Excel UTF-8 support
        response.getOutputStream().write(0xEF);
        response.getOutputStream().write(0xBB);
        response.getOutputStream().write(0xBF);

        PrintWriter writer = response.getWriter();

        // CSV 헤더
        writer.println("일시,사용자ID,사용자이메일,사용자명,활동유형,상세내용,대상유형,대상ID,대상명,IP주소");

        // CSV 데이터
        for (ActivityLog log : logs) {
            writer.println(String.join(",",
                    escapeCSV(log.getCreatedAt() != null ? DATE_FORMATTER.format(log.getCreatedAt()) : ""),
                    escapeCSV(log.getUserId() != null ? log.getUserId().toString() : ""),
                    escapeCSV(log.getUserEmail()),
                    escapeCSV(log.getUserName()),
                    escapeCSV(log.getActivityType() != null ? log.getActivityType().name() : ""),
                    escapeCSV(log.getDescription()),
                    escapeCSV(log.getTargetType()),
                    escapeCSV(log.getTargetId() != null ? log.getTargetId().toString() : ""),
                    escapeCSV(log.getTargetName()),
                    escapeCSV(log.getIpAddress())
            ));
        }

        writer.flush();
    }

    // CSV 값 이스케이프
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // 활동 유형 설명 (한국어)
    private String getActivityTypeDescription(ActivityType type) {
        return switch (type) {
            case LOGIN -> "로그인";
            case LOGOUT -> "로그아웃";
            case LOGIN_FAILED -> "로그인 실패";
            case PASSWORD_CHANGE -> "비밀번호 변경";
            case USER_CREATE -> "사용자 생성";
            case USER_UPDATE -> "사용자 수정";
            case USER_DELETE -> "사용자 삭제";
            case ROLE_CHANGE -> "역할 변경";
            case COURSE_VIEW -> "과정 조회";
            case COURSE_CREATE -> "과정 생성";
            case COURSE_UPDATE -> "과정 수정";
            case COURSE_DELETE -> "과정 삭제";
            case PROGRAM_CREATE -> "프로그램 생성";
            case PROGRAM_UPDATE -> "프로그램 수정";
            case PROGRAM_APPROVE -> "프로그램 승인";
            case PROGRAM_REJECT -> "프로그램 거절";
            case ENROLLMENT_CREATE -> "수강 신청";
            case ENROLLMENT_COMPLETE -> "수강 완료";
            case ENROLLMENT_DROP -> "수강 취소";
            case CONTENT_VIEW -> "콘텐츠 조회";
            case CONTENT_COMPLETE -> "콘텐츠 완료";
            case SETTINGS_UPDATE -> "설정 변경";
            case TENANT_CREATE -> "테넌트 생성";
            case TENANT_UPDATE -> "테넌트 수정";
            case OTHER -> "기타";
        };
    }

    // 활동 유형 정보 DTO
    record ActivityTypeInfo(String type, String description) {}
}
