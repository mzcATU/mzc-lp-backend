package com.mzc.lp.domain.tenantnotice.constant;

/**
 * 공지사항 대상자
 */
public enum NoticeTargetAudience {
    ALL,         // 전체 대상 (운영자 + 사용자)
    OPERATOR,    // 운영자 대상 (TA → CO)
    USER         // 사용자 대상 (TA → TU, CO → TU)
}
