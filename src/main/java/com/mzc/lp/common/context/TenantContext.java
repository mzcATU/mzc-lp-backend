package com.mzc.lp.common.context;

import com.mzc.lp.common.exception.TenantNotFoundException;

/**
 * ThreadLocal 기반 Tenant Context 관리
 * 현재 요청의 tenantId를 저장하고 조회하는 유틸리티 클래스
 */
public class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    private TenantContext() {
        // Utility class - 인스턴스화 방지
    }

    /**
     * 현재 스레드의 tenantId 설정
     *
     * @param tenantId 테넌트 ID (null 불가)
     * @throws IllegalArgumentException tenantId가 null인 경우
     */
    public static void setTenantId(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        currentTenant.set(tenantId);
    }

    /**
     * 현재 스레드의 tenantId 조회
     *
     * @return 테넌트 ID
     * @throws TenantNotFoundException TenantId가 설정되지 않은 경우
     */
    public static Long getCurrentTenantId() {
        Long tenantId = currentTenant.get();
        if (tenantId == null) {
            throw new TenantNotFoundException("TenantId not found in current context");
        }
        return tenantId;
    }

    /**
     * 현재 스레드의 tenantId 조회 (Optional)
     * Context에 tenantId가 없어도 예외를 발생시키지 않음
     *
     * @return 테넌트 ID 또는 null
     */
    public static Long getCurrentTenantIdOrNull() {
        return currentTenant.get();
    }

    /**
     * 현재 스레드의 tenantId가 설정되어 있는지 확인
     *
     * @return tenantId 설정 여부
     */
    public static boolean isSet() {
        return currentTenant.get() != null;
    }

    /**
     * 현재 스레드의 tenantId 제거
     * 메모리 릭 방지를 위해 요청 종료 시 반드시 호출 필요
     */
    public static void clear() {
        currentTenant.remove();
    }
}
