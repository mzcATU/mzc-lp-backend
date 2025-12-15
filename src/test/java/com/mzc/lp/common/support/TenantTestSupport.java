package com.mzc.lp.common.support;

import com.mzc.lp.common.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Tenant Context를 자동으로 설정/해제하는 테스트 Base 클래스
 * 모든 통합 테스트는 이 클래스를 상속받아야 함
 */
public abstract class TenantTestSupport {

    /**
     * 기본 테넌트 ID (테스트용)
     */
    protected static final Long DEFAULT_TENANT_ID = 1L;

    /**
     * 각 테스트 시작 전 TenantContext 설정
     */
    @BeforeEach
    void setUpTenantContext() {
        TenantContext.setTenantId(DEFAULT_TENANT_ID);
    }

    /**
     * 각 테스트 종료 후 TenantContext 초기화
     * ThreadLocal 메모리 릭 방지
     */
    @AfterEach
    void tearDownTenantContext() {
        TenantContext.clear();
    }

    /**
     * 테스트 중 다른 테넌트로 변경 (멀티테넌시 테스트용)
     */
    protected void switchTenant(Long tenantId) {
        TenantContext.clear();
        TenantContext.setTenantId(tenantId);
    }
}
