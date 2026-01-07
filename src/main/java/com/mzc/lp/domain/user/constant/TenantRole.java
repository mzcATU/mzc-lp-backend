package com.mzc.lp.domain.user.constant;

public enum TenantRole {
    SYSTEM_ADMIN,   // 시스템 최고 관리자 (테넌트 관리)
    TENANT_ADMIN,   // 테넌트 최고 관리자
    OPERATOR,       // 운영자 (강의 검토, 차수 생성, 역할 부여)
    DESIGNER,       // 설계자 (강의 개설 신청)
    INSTRUCTOR,     // 강사 (강의 진행)
    USER            // 일반 사용자 (수강)
}
