package com.mzc.lp.domain.sa.service;

import com.mzc.lp.domain.dashboard.constant.DashboardPeriod;
import com.mzc.lp.domain.sa.dto.response.SaDashboardResponse;

public interface SaDashboardService {

    /**
     * SA 대시보드 통계 조회
     *
     * @param period 기간 필터 (null이면 전체 기간)
     */
    SaDashboardResponse getDashboard(DashboardPeriod period);
}
