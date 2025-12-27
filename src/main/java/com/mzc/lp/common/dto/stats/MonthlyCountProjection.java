package com.mzc.lp.common.dto.stats;

/**
 * 월별 카운트 통계 Projection
 * GROUP BY year, month 쿼리 결과 매핑용
 */
public interface MonthlyCountProjection {

    Integer getYear();

    Integer getMonth();

    Long getCount();
}
