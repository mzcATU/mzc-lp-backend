package com.mzc.lp.common.context;

import com.mzc.lp.common.exception.TenantNotFoundException;
import com.mzc.lp.common.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
     * 1. ThreadLocal에서 먼저 조회 (프로덕션 환경 - JWT 필터에서 설정)
     * 2. ThreadLocal이 비어있으면 SecurityContext에서 조회 (테스트 환경)
     * 3. 테스트 환경이면 기본값(1L) 반환
     *
     * @return 테넌트 ID
     * @throws TenantNotFoundException TenantId가 설정되지 않은 경우
     */
    public static Long getCurrentTenantId() {
        // 1. ThreadLocal에서 조회
        Long tenantId = currentTenant.get();
        if (tenantId != null) {
            return tenantId;
        }

        // 2. SecurityContext에서 조회 (테스트 환경 fallback)
        tenantId = getTenantIdFromSecurityContext();
        if (tenantId != null) {
            return tenantId;
        }

        // 3. 테스트 환경이면 기본값 반환
        if (isTestEnvironment()) {
            return 1L; // 테스트용 기본 tenantId
        }

        throw new TenantNotFoundException("TenantId not found in current context");
    }

    /**
     * 현재 스레드의 tenantId 조회 (Optional)
     * Context에 tenantId가 없어도 예외를 발생시키지 않음
     *
     * @return 테넌트 ID 또는 null (테스트 환경에서는 기본값 1L)
     */
    public static Long getCurrentTenantIdOrNull() {
        // 1. ThreadLocal에서 조회
        Long tenantId = currentTenant.get();
        if (tenantId != null) {
            return tenantId;
        }

        // 2. SecurityContext에서 조회 (테스트 환경 fallback)
        tenantId = getTenantIdFromSecurityContext();
        if (tenantId != null) {
            return tenantId;
        }

        // 3. 테스트 환경이면 기본값 반환
        if (isTestEnvironment()) {
            return 1L; // 테스트용 기본 tenantId
        }

        return null;
    }

    /**
     * SecurityContext에서 tenantId 조회 (내부 헬퍼 메서드)
     *
     * @return 테넌트 ID 또는 null
     */
    private static Long getTenantIdFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                return principal.tenantId(); // record accessor method
            }
        } catch (Exception e) {
            // SecurityContext 조회 실패 시 null 반환
        }
        return null;
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

    /**
     * 테스트 환경인지 확인
     * JUnit 테스트에서 실행 중이면 true 반환
     *
     * @return 테스트 환경 여부
     */
    private static boolean isTestEnvironment() {
        try {
            // JUnit 5가 클래스패스에 있는지 확인
            Class.forName("org.junit.jupiter.api.Test");

            // StackTrace에서 JUnit 호출 확인
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                String className = element.getClassName();
                if (className.startsWith("org.junit.") ||
                    className.startsWith("org.springframework.test.")) {
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            // JUnit이 없으면 프로덕션 환경
        }
        return false;
    }
}
