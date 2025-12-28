package com.mzc.lp.domain.dashboard.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.dashboard.dto.response.AdminKpiResponse;
import com.mzc.lp.domain.dashboard.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * TENANT_ADMIN KPI 대시보드 조회
     */
    @GetMapping("/api/admin/dashboard/kpi")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<AdminKpiResponse>> getKpiStats() {
        AdminKpiResponse response = adminDashboardService.getKpiStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
