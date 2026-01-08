package com.mzc.lp.domain.tenantnotice.dto.request;

import com.mzc.lp.domain.tenantnotice.constant.NoticeTargetAudience;
import com.mzc.lp.domain.tenantnotice.constant.TenantNoticeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * 테넌트 공지사항 생성 요청
 */
public record CreateTenantNoticeRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content,

        @NotNull(message = "공지 유형은 필수입니다")
        TenantNoticeType type,

        @NotNull(message = "대상자는 필수입니다")
        NoticeTargetAudience targetAudience,

        Boolean isPinned,

        Instant expiredAt
) {
    public CreateTenantNoticeRequest {
        if (isPinned == null) {
            isPinned = false;
        }
    }
}
