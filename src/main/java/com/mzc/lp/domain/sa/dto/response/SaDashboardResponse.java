package com.mzc.lp.domain.sa.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class SaDashboardResponse {

    private TenantStats tenantStats;
    private UserStats userStats;
    private List<RecentTenant> recentTenants;

    @Getter
    @Builder
    public static class TenantStats {
        private long total;
        private long active;
        private long pending;
        private long suspended;
        private long terminated;
        private Map<String, Long> byPlan;
    }

    @Getter
    @Builder
    public static class UserStats {
        private long total;
        private long active;
        private long suspended;
        private long withdrawn;
    }

    @Getter
    @Builder
    public static class RecentTenant {
        private Long id;
        private String code;
        private String name;
        private String status;
        private String plan;
        private String createdAt;
    }
}
