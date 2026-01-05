package com.mzc.lp.domain.analytics.constant;

/**
 * 활동 로그 유형
 */
public enum ActivityType {
    // 인증
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    PASSWORD_CHANGE,

    // 사용자
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    ROLE_CHANGE,

    // 강좌
    COURSE_VIEW,
    COURSE_CREATE,
    COURSE_UPDATE,
    COURSE_DELETE,

    // 프로그램
    PROGRAM_CREATE,
    PROGRAM_UPDATE,
    PROGRAM_APPROVE,
    PROGRAM_REJECT,

    // 수강
    ENROLLMENT_CREATE,
    ENROLLMENT_COMPLETE,
    ENROLLMENT_DROP,

    // 학습
    CONTENT_VIEW,
    CONTENT_COMPLETE,

    // 시스템
    SETTINGS_UPDATE,
    TENANT_CREATE,
    TENANT_UPDATE,

    // 기타
    OTHER
}
