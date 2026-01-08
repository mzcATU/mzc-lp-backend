package com.mzc.lp.common.dto.stats;

import java.time.LocalDate;

/**
 * 일별 수강/수료 통계 Projection
 * GROUP BY date 쿼리 결과 매핑용
 */
public interface DailyEnrollmentStatsProjection {

    LocalDate getDate();

    Long getEnrollments();

    Long getCompletions();
}
