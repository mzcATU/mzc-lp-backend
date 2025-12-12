package com.mzc.lp.domain.ts.constant;

public enum CourseTimeStatus {
    DRAFT,      // 초안 (수정 가능, 수강 신청 불가)
    RECRUITING, // 모집 중 (수강 신청 가능)
    ONGOING,    // 진행 중 (학습 진행)
    CLOSED,     // 종료 (신규 신청 불가, 기존 수강생 접근 가능)
    ARCHIVED    // 보관 (관리자 전용)
}
