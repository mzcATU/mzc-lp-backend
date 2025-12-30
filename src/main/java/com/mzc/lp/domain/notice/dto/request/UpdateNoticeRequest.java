package com.mzc.lp.domain.notice.dto.request;

import com.mzc.lp.domain.notice.constant.NoticeType;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateNoticeRequest(
        @Size(max = 200)
        String title,

        String content,

        NoticeType type,

        Boolean isPinned,

        Instant expiredAt
) {
}
