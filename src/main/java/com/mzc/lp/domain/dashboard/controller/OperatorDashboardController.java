package com.mzc.lp.domain.dashboard.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.dashboard.constant.DashboardPeriod;
import com.mzc.lp.domain.dashboard.dto.response.OperatorTasksResponse;
import com.mzc.lp.domain.dashboard.service.OperatorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OperatorDashboardController {

    private final OperatorDashboardService operatorDashboardService;

    /**
     * OPERATOR 운영 대시보드 조회
     *
     * @param period 기간 필터 ("7d", "30d", 미전송 시 전체 기간)
     */
    @GetMapping("/api/operator/dashboard/tasks")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<OperatorTasksResponse>> getOperatorTasks(
            @RequestParam(required = false) String period
    ) {
        DashboardPeriod dashboardPeriod = DashboardPeriod.fromCode(period);
        OperatorTasksResponse response = operatorDashboardService.getOperatorTasks(dashboardPeriod);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
