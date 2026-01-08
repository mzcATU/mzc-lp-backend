package com.mzc.lp.domain.tenant.dto.response;

import java.util.List;

public record TenantUserStatsResponse(
        List<TenantUserCount> tenantUserCounts,
        long totalUsers
) {
    public record TenantUserCount(
            Long tenantId,
            String tenantCode,
            String tenantName,
            long userCount
    ) {}

    public static TenantUserStatsResponse of(List<TenantUserCount> counts, long total) {
        return new TenantUserStatsResponse(counts, total);
    }
}
