package com.mzc.lp.domain.notice.dto.request;

import com.mzc.lp.domain.notice.constant.NoticeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateNoticeRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        String content,

        @NotNull
        NoticeType type,

        Boolean isPinned,

        Instant expiredAt
) {
}
