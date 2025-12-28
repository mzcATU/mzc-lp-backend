package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.domain.dashboard.dto.response.AdminKpiResponse;

/**
 * TENANT_ADMIN 대시보드 서비스 인터페이스
 */
public interface AdminDashboardService {

    /**
     * KPI 대시보드 통계 조회
     *
     * @return KPI 대시보드 통계
     */
    AdminKpiResponse getKpiStats();
}
