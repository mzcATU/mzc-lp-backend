package com.mzc.lp.domain.user.constant;

public enum TenantRole {
    TENANT_ADMIN,   // 테넌트 최고 관리자
    OPERATOR,       // 운영자 (강의 검토, 차수 생성, 역할 부여)
    USER            // 일반 사용자 (수강)
}
