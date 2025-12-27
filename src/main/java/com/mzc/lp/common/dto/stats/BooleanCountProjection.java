package com.mzc.lp.common.dto.stats;

/**
 * Boolean 값별 카운트 통계 Projection
 * GROUP BY boolean_column 쿼리 결과 매핑용 (예: 무료/유료 구분)
 */
public interface BooleanCountProjection {

    Boolean getValue();

    Long getCount();
}
