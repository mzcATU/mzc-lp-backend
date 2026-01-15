package com.mzc.lp.domain.tenantnotice.constant;

/**
 * 공지사항 대상자
 * TA가 CO, TU, DESIGNER, INSTRUCTOR에게 공지 가능
 * CO가 TU에게 공지 가능
 */
public enum NoticeTargetAudience {
    ALL,         // 전체 대상 (모든 역할)
    OPERATOR,    // 운영자 대상 (TA → CO)
    USER,        // 일반 사용자 대상 (TA/CO → TU)
    DESIGNER,    // 설계자 대상 (TA → DESIGNER)
    INSTRUCTOR   // 강사 대상 (TA → INSTRUCTOR)
}
