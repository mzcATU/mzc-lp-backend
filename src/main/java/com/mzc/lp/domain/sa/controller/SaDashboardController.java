package com.mzc.lp.domain.sa.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.dashboard.constant.DashboardPeriod;
import com.mzc.lp.domain.sa.dto.response.SaDashboardResponse;
import com.mzc.lp.domain.sa.service.SaDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sa/dashboard")
@RequiredArgsConstructor
public class SaDashboardController {

    private final SaDashboardService saDashboardService;

    /**
     * SA 대시보드 통계 조회
     * GET /api/sa/dashboard
     *
     * @param period 기간 필터 ("7d", "30d", 미전송 시 전체 기간)
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<SaDashboardResponse>> getDashboard(
            @RequestParam(required = false) String period
    ) {
        DashboardPeriod dashboardPeriod = DashboardPeriod.fromCode(period);
        SaDashboardResponse response = saDashboardService.getDashboard(dashboardPeriod);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
