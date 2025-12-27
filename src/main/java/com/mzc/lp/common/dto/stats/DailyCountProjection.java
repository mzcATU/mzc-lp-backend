package com.mzc.lp.common.dto.stats;

import java.time.LocalDate;

/**
 * 일별 카운트 통계 Projection
 * GROUP BY date 쿼리 결과 매핑용
 */
public interface DailyCountProjection {

    LocalDate getDate();

    Long getCount();
}
