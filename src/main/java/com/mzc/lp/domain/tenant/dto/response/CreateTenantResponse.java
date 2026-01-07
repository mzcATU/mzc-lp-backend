package com.mzc.lp.domain.tenant.dto.response;

import com.mzc.lp.domain.tenant.constant.PlanType;
import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.constant.TenantType;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

/**
 * 테넌트 생성 응답 DTO
 * 테넌트 정보와 함께 생성된 관리자 계정 정보 포함
 */
public record CreateTenantResponse(
        Long tenantId,
        String code,
        String name,
        TenantType type,
        TenantStatus status,
        PlanType plan,
        String subdomain,
        String customDomain,
        Instant createdAt,
        // 관리자 정보
        AdminInfo admin
) {
    public record AdminInfo(
            Long userId,
            String email,
            String name,
            String tempPassword
    ) {
        public static AdminInfo from(User user, String tempPassword) {
            return new AdminInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    tempPassword
            );
        }
    }

    public static CreateTenantResponse from(Tenant tenant, User admin, String tempPassword) {
        return new CreateTenantResponse(
                tenant.getId(),
                tenant.getCode(),
                tenant.getName(),
                tenant.getType(),
                tenant.getStatus(),
                tenant.getPlan(),
                tenant.getSubdomain(),
                tenant.getCustomDomain(),
                tenant.getCreatedAt(),
                AdminInfo.from(admin, tempPassword)
        );
    }
}
