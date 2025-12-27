package com.mzc.lp.common.dto.stats;

/**
 * 상태별 카운트 통계 Projection
 * GROUP BY status 쿼리 결과 매핑용
 */
public interface StatusCountProjection {

    String getStatus();

    Long getCount();
}
