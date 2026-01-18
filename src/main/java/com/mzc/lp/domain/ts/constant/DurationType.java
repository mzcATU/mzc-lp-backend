package com.mzc.lp.domain.ts.constant;

/**
 * 학습 기간 유형
 */
public enum DurationType {
    FIXED,      // 고정 날짜 (classStartDate ~ classEndDate)
    RELATIVE,   // 상대 기간 (등록일 기준 durationDays일)
    UNLIMITED   // 무제한 (종료일 없음)
}
