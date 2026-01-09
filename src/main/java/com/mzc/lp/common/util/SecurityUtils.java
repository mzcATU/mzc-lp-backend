package com.mzc.lp.common.util;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.exception.TenantNotFoundException;
import com.mzc.lp.common.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 관련 유틸리티 메서드
 */
public class SecurityUtils {

    private SecurityUtils() {
        // Utility class - 인스턴스화 방지
    }

    /**
     * 현재 인증된 사용자의 tenantId 조회
     *
     * 1. TenantContext에서 먼저 조회 (JWT 필터에서 설정됨)
     * 2. TenantContext가 비어있으면 SecurityContext의 UserPrincipal에서 조회 (테스트 환경)
     *
     * @return 테넌트 ID
     * @throws TenantNotFoundException TenantId를 찾을 수 없는 경우
     */
    public static Long getCurrentTenantId() {
        // 1. TenantContext에서 먼저 조회 (프로덕션 환경)
        if (TenantContext.isSet()) {
            return TenantContext.getCurrentTenantId();
        }

        // 2. SecurityContext에서 조회 (테스트 환경)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long tenantId = principal.tenantId(); // record accessor method
            if (tenantId != null) {
                return tenantId;
            }
        }

        throw new TenantNotFoundException("TenantId not found in current context");
    }

    /**
     * 현재 인증된 사용자의 tenantId 조회 (Optional)
     * Context에 tenantId가 없어도 예외를 발생시키지 않음
     *
     * @return 테넌트 ID 또는 null
     */
    public static Long getCurrentTenantIdOrNull() {
        // 1. TenantContext에서 먼저 조회
        if (TenantContext.isSet()) {
            return TenantContext.getCurrentTenantIdOrNull();
        }

        // 2. SecurityContext에서 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            return principal.tenantId(); // record accessor method
        }

        return null;
    }

    /**
     * 현재 인증된 사용자의 userId 조회
     *
     * @return 사용자 ID
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            return principal.id(); // record accessor method
        }
        throw new IllegalStateException("User not authenticated");
    }

    /**
     * 현재 인증된 사용자의 UserPrincipal 조회
     *
     * @return UserPrincipal
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        throw new IllegalStateException("User not authenticated");
    }
}
