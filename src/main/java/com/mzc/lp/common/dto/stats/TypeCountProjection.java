package com.mzc.lp.common.dto.stats;

/**
 * 타입별 카운트 통계 Projection
 * GROUP BY type 쿼리 결과 매핑용
 */
public interface TypeCountProjection {

    String getType();

    Long getCount();
}
