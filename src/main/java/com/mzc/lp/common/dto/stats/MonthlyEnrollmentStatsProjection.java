package com.mzc.lp.common.dto.stats;

/**
 * 월별 수강/수료 통계 Projection
 * GROUP BY year, month 쿼리 결과 매핑용
 */
public interface MonthlyEnrollmentStatsProjection {

    Integer getYear();

    Integer getMonth();

    Long getEnrollments();

    Long getCompletions();
}
