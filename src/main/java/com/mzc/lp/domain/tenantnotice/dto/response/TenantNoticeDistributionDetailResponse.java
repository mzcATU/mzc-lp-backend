package com.mzc.lp.domain.tenantnotice.dto.response;

import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeType;

import java.time.Instant;
import java.util.List;

/**
 * 특정 공지사항의 상세 배포 현황 응답
 */
public record TenantNoticeDistributionDetailResponse(
        Long noticeId,
        String noticeTitle,
        TenantNoticeType noticeType,
        NoticeTargetAudience targetAudience,
        Instant publishedAt,
        Integer sentCount,
        Integer readCount,
        List<UserDistributionInfoResponse> userDistributions
) {
    public static TenantNoticeDistributionDetailResponse of(
            Long noticeId,
            String noticeTitle,
            TenantNoticeType noticeType,
            NoticeTargetAudience targetAudience,
            Instant publishedAt,
            int sentCount,
            int readCount,
            List<UserDistributionInfoResponse> userDistributions
    ) {
        return new TenantNoticeDistributionDetailResponse(
                noticeId,
                noticeTitle,
                noticeType,
                targetAudience,
                publishedAt,
                sentCount,
                readCount,
                userDistributions
        );
    }
}
