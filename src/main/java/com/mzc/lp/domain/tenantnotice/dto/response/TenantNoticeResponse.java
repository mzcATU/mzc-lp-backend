package com.mzc.lp.domain.tenantnotice.dto.response;

import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeStatus;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeType;
import com.mzc.lp.domain.tenantnotice.entity.TenantNotice;

import java.time.Instant;

/**
 * 테넌트 공지사항 응답
 */
public record TenantNoticeResponse(
        Long id,
        Long tenantId,
        String title,
        String content,
        TenantNoticeType type,
        TenantNoticeStatus status,
        NoticeTargetAudience targetAudience,
        String creatorRole,
        Boolean isPinned,
        Instant publishedAt,
        Instant expiredAt,
        Long createdBy,
        Integer viewCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static TenantNoticeResponse from(TenantNotice notice) {
        return new TenantNoticeResponse(
                notice.getId(),
                notice.getTenantId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getType(),
                notice.getStatus(),
                notice.getTargetAudience(),
                notice.getCreatorRole(),
                notice.getIsPinned(),
                notice.getPublishedAt(),
                notice.getExpiredAt(),
                notice.getCreatedBy(),
                notice.getViewCount(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
