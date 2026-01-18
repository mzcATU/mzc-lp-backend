package com.mzc.lp.domain.ts.constant;

/**
 * 차수 생성 조합 품질 등급
 */
public enum QualityRating {
    BEST,       // 해당 진행 방식에 최적화된 조합
    GOOD,       // 권장 조합
    COMMON,     // 일반 조합
    CAUTION     // 허용하지만 운영 주의 필요
}
