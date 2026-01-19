package com.mzc.lp.domain.tenantnotice.dto.response;

import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeStatus;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeType;
import com.mzc.lp.domain.tenantnotice.entity.TenantNotice;

import java.time.Instant;

/**
 * 공지사항별 배포 통계 응답
 */
public record TenantNoticeDistributionStatsResponse(
        Long noticeId,
        String noticeTitle,
        TenantNoticeType noticeType,
        NoticeTargetAudience targetAudience,
        Boolean isPinned,
        Instant publishedAt,
        Integer totalUsers,    // 대상 사용자 수
        Integer sentCount,     // 발송된 수 (발행된 경우 = totalUsers)
        Integer readCount      // 열람한 수 (viewCount 기반)
) {
    public static TenantNoticeDistributionStatsResponse from(TenantNotice notice, int totalTargetUsers) {
        boolean isDistributed = notice.getStatus() == TenantNoticeStatus.PUBLISHED
                || notice.getStatus() == TenantNoticeStatus.ARCHIVED;
        return new TenantNoticeDistributionStatsResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getType(),
                notice.getTargetAudience(),
                notice.getIsPinned(),
                notice.getPublishedAt(),
                totalTargetUsers,
                isDistributed ? totalTargetUsers : 0,  // 발행된 경우 전체 대상에게 발송됨
                notice.getViewCount() != null ? notice.getViewCount() : 0
        );
    }
}
