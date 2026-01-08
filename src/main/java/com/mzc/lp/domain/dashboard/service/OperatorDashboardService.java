package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.domain.dashboard.constant.DashboardPeriod;
import com.mzc.lp.domain.dashboard.dto.response.OperatorTasksResponse;

/**
 * OPERATOR 운영 대시보드 서비스 인터페이스
 */
public interface OperatorDashboardService {

    /**
     * 운영 대시보드 통계 조회
     *
     * @param period 기간 필터 (null이면 전체 기간)
     * @return 운영 대시보드 통계
     */
    OperatorTasksResponse getOperatorTasks(DashboardPeriod period);
}
