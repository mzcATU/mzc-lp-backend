package com.mzc.lp.domain.notice.dto.response;

import com.mzc.lp.domain.notice.constant.NoticeStatus;
import com.mzc.lp.domain.notice.constant.NoticeType;
import com.mzc.lp.domain.notice.entity.Notice;

import java.time.Instant;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        NoticeType type,
        NoticeStatus status,
        Boolean isPinned,
        Instant publishedAt,
        Instant expiredAt,
        Long createdBy,
        Long distributionCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getType(),
                notice.getStatus(),
                notice.getIsPinned(),
                notice.getPublishedAt(),
                notice.getExpiredAt(),
                notice.getCreatedBy(),
                null,
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

    public static NoticeResponse from(Notice notice, Long distributionCount) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getType(),
                notice.getStatus(),
                notice.getIsPinned(),
                notice.getPublishedAt(),
                notice.getExpiredAt(),
                notice.getCreatedBy(),
                distributionCount,
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
