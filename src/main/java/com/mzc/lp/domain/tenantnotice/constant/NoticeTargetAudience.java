package com.mzc.lp.domain.tenantnotice.constant;

/**
 * 공지사항 대상자
 */
public enum NoticeTargetAudience {
    OPERATOR,    // 운영자 대상 (TA → TO)
    USER         // 사용자 대상 (TA → TU, TO → TU)
}
