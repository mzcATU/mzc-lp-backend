package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.domain.dashboard.dto.response.OwnerStatsResponse;

/**
 * 내 강의 통계 서비스 인터페이스 (Course Designer용)
 */
public interface OwnerStatsService {

    /**
     * 내 강의 통계 조회
     *
     * @param userId 사용자 ID
     * @return 내 강의 통계
     */
    OwnerStatsResponse getMyStats(Long userId);
}
