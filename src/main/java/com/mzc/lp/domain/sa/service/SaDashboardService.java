package com.mzc.lp.domain.sa.service;

import com.mzc.lp.domain.sa.dto.response.SaDashboardResponse;

public interface SaDashboardService {

    /**
     * SA 대시보드 통계 조회
     */
    SaDashboardResponse getDashboard();
}
