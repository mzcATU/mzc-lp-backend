package com.mzc.lp.domain.notice.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * 공지사항 배포 통계 응답 DTO
 */
public record NoticeDistributionStatsResponse(
        Long noticeId,
        String noticeTitle,
        String noticeType,
        String noticeStatus,
        Boolean isPinned,
        Integer totalTenants,
        Integer sentCount,
        Integer readCount,
        Instant publishedAt,
        Instant createdAt,
        List<TenantDistributionInfo> tenantDistributions
) {
    public record TenantDistributionInfo(
            Long tenantId,
            String tenantName,
            String tenantCode,
            Boolean isRead,
            Instant distributedAt,
            Instant readAt
    ) {}

    public static NoticeDistributionStatsResponse of(
            Long noticeId,
            String noticeTitle,
            String noticeType,
            String noticeStatus,
            Boolean isPinned,
            Integer totalTenants,
            Integer sentCount,
            Integer readCount,
            Instant publishedAt,
            Instant createdAt,
            List<TenantDistributionInfo> tenantDistributions
    ) {
        return new NoticeDistributionStatsResponse(
                noticeId,
                noticeTitle,
                noticeType,
                noticeStatus,
                isPinned,
                totalTenants,
                sentCount,
                readCount,
                publishedAt,
                createdAt,
                tenantDistributions
        );
    }
}
