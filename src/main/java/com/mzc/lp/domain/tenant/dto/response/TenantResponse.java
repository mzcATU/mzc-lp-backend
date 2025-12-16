package com.mzc.lp.domain.tenant.dto.response;

import com.mzc.lp.domain.tenant.constant.PlanType;
import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.constant.TenantType;
import com.mzc.lp.domain.tenant.entity.Tenant;

import java.time.Instant;

public record TenantResponse(
        Long tenantId,
        String code,
        String name,
        TenantType type,
        TenantStatus status,
        PlanType plan,
        String subdomain,
        String customDomain,
        Instant createdAt,
        Instant updatedAt
) {
    public static TenantResponse from(Tenant entity) {
        return new TenantResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getType(),
                entity.getStatus(),
                entity.getPlan(),
                entity.getSubdomain(),
                entity.getCustomDomain(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
