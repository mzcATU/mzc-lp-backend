package com.mzc.lp.domain.dashboard.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.dashboard.dto.response.OwnerStatsResponse;
import com.mzc.lp.domain.dashboard.service.OwnerStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owners/me")
@RequiredArgsConstructor
public class OwnerStatsController {

    private final OwnerStatsService ownerStatsService;

    /**
     * 내 강의 통계 조회 (Course Designer)
     * - DESIGNER 역할이 있거나, OPERATOR/TENANT_ADMIN 권한이 있으면 접근 가능
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('DESIGNER') or hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<OwnerStatsResponse>> getMyStats(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        OwnerStatsResponse response = ownerStatsService.getMyStats(principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
